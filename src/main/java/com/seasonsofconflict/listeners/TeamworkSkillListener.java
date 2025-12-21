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
import org.bukkit.inventory.ItemStack;

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
