package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles Teamwork Tree passive skill effects
 */
public class TeamworkSkillListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;

    public TeamworkSkillListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Guardian Angel: Teleport to teammate taking fatal damage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        // Check if damage would be fatal
        double damageAfter = victim.getHealth() - event.getFinalDamage();
        if (damageAfter > 0) {
            return; // Not fatal, no need for guardian angel
        }

        // Find nearby teammates with Guardian Angel skill within 10 blocks
        Location victimLoc = victim.getLocation();
        int victimTeam = plugin.getGameManager().getPlayerData(victim).getTeamId();

        for (Player nearbyPlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer.getUniqueId().equals(victim.getUniqueId())) {
                continue; // Skip the victim
            }

            // Check if within 10 blocks
            if (!nearbyPlayer.getWorld().equals(victimLoc.getWorld()) ||
                nearbyPlayer.getLocation().distance(victimLoc) > 10.0) {
                continue;
            }

            // Check if same team
            int nearbyTeam = plugin.getGameManager().getPlayerData(nearbyPlayer).getTeamId();

            if (victimTeam == nearbyTeam && effectManager.hasSkillByName(nearbyPlayer, "guardian_angel")) {
                // Try to activate Guardian Angel (5 min cooldown = 300 seconds)
                if (plugin.getCooldownManager().tryActivateSkill(nearbyPlayer, "guardian_angel_passive", 300)) {
                    // Teleport to victim
                    nearbyPlayer.teleport(victimLoc);

                    // Visual: Angelic particles
                    nearbyPlayer.getWorld().spawnParticle(
                        Particle.TOTEM,
                        victimLoc.clone().add(0, 1, 0),
                        50,
                        1.0, 1.0, 1.0,
                        0.2
                    );
                    nearbyPlayer.getWorld().spawnParticle(
                        Particle.END_ROD,
                        nearbyPlayer.getLocation().add(0, 1, 0),
                        30,
                        0.5, 1.0, 0.5,
                        0.1
                    );

                    // Sound: Angel arrival
                    nearbyPlayer.getWorld().playSound(
                        victimLoc,
                        Sound.BLOCK_BEACON_ACTIVATE,
                        1.0f,
                        1.5f
                    );

                    // Messages
                    MessageUtils.sendMessage(nearbyPlayer,
                        "&e&l✦ &6GUARDIAN ANGEL! &eTeleported to save " + victim.getName());
                    MessageUtils.sendMessage(victim,
                        "&e&l✦ &6" + nearbyPlayer.getName() + " has arrived to help!");

                    // Only one guardian angel can respond
                    break;
                }
            }
        }
    }

    /**
     * Handle combat damage with Pack Tactics
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        double baseDamage = event.getDamage();

        // Pack Tactics: +5% damage per nearby teammate (max 20%)
        double packDamage = effectManager.applyPackTactics(attacker, baseDamage);

        if (packDamage > baseDamage) {
            event.setDamage(packDamage);

            // Visual: Link particles to nearby allies
            int nearbyAllies = (int) ((packDamage / baseDamage - 1.0) * 20); // Calculate # of allies
            if (nearbyAllies > 0) {
                attacker.getWorld().spawnParticle(
                    Particle.END_ROD,
                    attacker.getLocation().add(0, 1.5, 0),
                    nearbyAllies * 2,
                    0.5, 0.5, 0.5,
                    0.05
                );
            }
        }
    }

    /**
     * Handle block breaking with Resource Sharing
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        // Resource Sharing: 10% chance to duplicate drops to teammate
        if (effectManager.applyResourceSharing(player)) {
            Player teammate = effectManager.getRandomNearbyTeammate(player);

            if (teammate != null) {
                // Give copies of drops to teammate
                for (ItemStack drop : event.getBlock().getDrops(player.getInventory().getItemInMainHand())) {
                    teammate.getInventory().addItem(drop.clone());
                }

                // Visual: Item copy particles from player to teammate
                drawParticleLine(player.getLocation().add(0, 1, 0),
                                teammate.getLocation().add(0, 1, 0),
                                player.getWorld());

                // Notify both players
                MessageUtils.sendMessage(player,
                    "&e&l⚡ &7Resource Sharing! &aShared with " + teammate.getName());
                MessageUtils.sendMessage(teammate,
                    "&e&l⚡ &7Resource Sharing! &aReceived from " + player.getName());

                // Sound effect
                player.getWorld().playSound(
                    teammate.getLocation(),
                    Sound.ENTITY_ITEM_PICKUP,
                    0.7f,
                    1.2f
                );
            }
        }
    }

    /**
     * Last Stand Protocol: Buff nearby teammates when player dies
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Location deathLocation = deadPlayer.getLocation();

        int deadTeam = plugin.getGameManager().getPlayerData(deadPlayer).getTeamId();

        // Find nearby teammates within 20 blocks who have Last Stand Protocol
        for (Player nearbyPlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer.getUniqueId().equals(deadPlayer.getUniqueId())) {
                continue; // Skip the dead player
            }

            // Check if within 20 blocks
            if (!nearbyPlayer.getWorld().equals(deathLocation.getWorld()) ||
                nearbyPlayer.getLocation().distance(deathLocation) > 20.0) {
                continue;
            }

            // Check if same team
            int nearbyTeam = plugin.getGameManager().getPlayerData(nearbyPlayer).getTeamId();

            if (deadTeam == nearbyTeam && effectManager.hasSkillByName(nearbyPlayer, "last_stand_protocol")) {
                // Apply buffs: +40% damage and +25% damage resistance for 15 seconds
                nearbyPlayer.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    300, // 15 seconds
                    1,   // Level II (+40% damage - stronger than regular strength)
                    false,
                    true,
                    true
                ));
                nearbyPlayer.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    300, // 15 seconds
                    1,   // Level II (+25% resistance)
                    false,
                    true,
                    true
                ));

                // Visual: Dramatic red/orange flame particles
                nearbyPlayer.getWorld().spawnParticle(
                    Particle.FLAME,
                    nearbyPlayer.getLocation().add(0, 1, 0),
                    40,
                    0.5, 1.0, 0.5,
                    0.15
                );
                nearbyPlayer.getWorld().spawnParticle(
                    Particle.LAVA,
                    nearbyPlayer.getLocation().add(0, 1, 0),
                    20,
                    0.3, 0.5, 0.3,
                    0.1
                );

                // Glowing effect
                nearbyPlayer.setGlowing(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (nearbyPlayer.isOnline()) {
                        nearbyPlayer.setGlowing(false);
                    }
                }, 300L); // Remove after 15 seconds

                // Sound effect - dramatic dragon growl
                nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.2f);

                // Message
                MessageUtils.sendMessage(nearbyPlayer,
                    "&c&l⚔ &4LAST STAND PROTOCOL! &c" + deadPlayer.getName() + " has fallen! " +
                    "&e+40% damage, +25% resistance (15s)");
            }
        }
    }

    /**
     * Apply Shared Victory bonus to quest points (called from QuestManager)
     */
    public int applySharedVictoryBonus(Player player, int basePoints) {
        int bonusPoints = effectManager.applySharedVictory(player, basePoints);

        if (bonusPoints > basePoints) {
            // Visual: Gold coin particles
            player.getWorld().spawnParticle(
                Particle.CRIT_MAGIC,
                player.getLocation().add(0, 2, 0),
                15,
                0.3, 0.3, 0.3,
                0.5
            );
        }

        return bonusPoints;
    }

    /**
     * Draw a particle line between two locations
     */
    private void drawParticleLine(Location start, Location end, World world) {
        double distance = start.distance(end);
        int particles = (int) (distance * 5);

        for (int i = 0; i < particles; i++) {
            double t = i / (double) particles;
            Location point = start.clone().add(
                end.clone().subtract(start).toVector().multiply(t)
            );

            world.spawnParticle(
                Particle.ENCHANTMENT_TABLE,
                point,
                1,
                0, 0, 0,
                0
            );
        }
    }
}
