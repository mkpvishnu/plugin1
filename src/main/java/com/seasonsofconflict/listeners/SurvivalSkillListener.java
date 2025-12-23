package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles Survival Tree passive skill effects
 */
public class SurvivalSkillListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;

    // Track last damage absorption time for each player (Damage Absorption skill)
    private final java.util.Map<java.util.UUID, Long> lastDamageAbsorptionTime = new java.util.HashMap<>();

    public SurvivalSkillListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Apply passive stat bonuses on player join (Hardy)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay by 2 ticks to ensure player is fully loaded (after CombatSkillListener)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyPassiveStatBonuses(player);
        }, 2L);
    }

    /**
     * Handle damage with survival passives
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double damage = event.getDamage();
        EntityDamageEvent.DamageCause cause = event.getCause();

        // Damage Absorption: First hit every 30s deals 50% less damage
        if (effectManager.hasSkillByName(player, "damage_absorption")) {
            long currentTime = System.currentTimeMillis();
            Long lastAbsorption = lastDamageAbsorptionTime.get(player.getUniqueId());

            // If 30 seconds have passed since last absorption (or first time)
            if (lastAbsorption == null || (currentTime - lastAbsorption) >= 30000) {
                // Reduce damage by 50%
                damage = damage * 0.50;
                lastDamageAbsorptionTime.put(player.getUniqueId(), currentTime);

                // Visual: Shield particles
                player.getWorld().spawnParticle(
                    Particle.CRIT_MAGIC,
                    player.getLocation().add(0, 1, 0),
                    25,
                    0.5, 0.5, 0.5,
                    0.2
                );
                player.getWorld().spawnParticle(
                    Particle.ENCHANTMENT_TABLE,
                    player.getLocation().add(0, 1, 0),
                    15,
                    0.4, 0.4, 0.4,
                    0.1
                );

                // Sound effect
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.5f);

                // Message
                player.sendMessage(org.bukkit.ChatColor.AQUA + "🛡 Damage Absorption! -50% damage");
            }
        }

        // Warrior's Resolve: Cannot drop below 1 HP for 5s after fatal damage (3 min cooldown)
        if (effectManager.hasSkillByName(player, "warriors_resolve")) {
            double playerHealth = player.getHealth();

            if (playerHealth - damage <= 0) {
                // Check if cooldown is ready
                if (plugin.getCooldownManager().tryActivateSkill(player, "warriors_resolve_passive", 180)) {
                    // Prevent death - set to 1 HP
                    event.setDamage(playerHealth - 1.0);

                    // Apply invulnerability for 5 seconds
                    player.setInvulnerable(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.setInvulnerable(false);
                    }, 100L); // 5 seconds

                    // Visual: Golden shield particles
                    player.getWorld().spawnParticle(
                        Particle.TOTEM,
                        player.getLocation().add(0, 1, 0),
                        50,
                        0.5, 0.5, 0.5,
                        0.3
                    );

                    // Sound
                    player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

                    player.sendMessage(org.bukkit.ChatColor.GOLD + "⚔ Warrior's Resolve activated!");
                    return;
                }
            }
        }

        // Thick Skin: -10% damage from all sources
        damage = effectManager.applyThickSkin(player, damage);

        if (damage != event.getDamage()) {
            // Visual: Iron particles when damage reduced
            player.getWorld().spawnParticle(
                Particle.ITEM_CRACK,
                player.getLocation().add(0, 1, 0),
                10,
                0.3, 0.3, 0.3,
                0.1,
                new ItemStack(Material.IRON_INGOT)
            );
        }

        // Fire Resistance: 80% reduction to fire/lava damage
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
            cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
            cause == EntityDamageEvent.DamageCause.LAVA) {

            double reducedDamage = effectManager.applyFireResistance(player, damage);

            if (reducedDamage != damage) {
                damage = reducedDamage;

                // Visual: Fire resistance particles
                player.getWorld().spawnParticle(
                    Particle.DRIP_WATER,
                    player.getLocation().add(0, 1, 0),
                    10,
                    0.3, 0.5, 0.3,
                    0.05
                );
            }
        }

        // Fall Damage Negation: Reduce/negate fall damage
        if (cause == EntityDamageEvent.DamageCause.FALL) {
            double fallDistance = player.getFallDistance();
            double reducedDamage = effectManager.applyFallDamageNegation(player, damage, fallDistance);

            if (reducedDamage != damage) {
                event.setDamage(reducedDamage);

                // Visual: Cloud particles on landing
                player.getWorld().spawnParticle(
                    Particle.CLOUD,
                    player.getLocation(),
                    20,
                    0.5, 0.1, 0.5,
                    0.05
                );
                player.getWorld().playSound(
                    player.getLocation(),
                    Sound.ENTITY_CHICKEN_EGG,
                    0.5f,
                    0.8f
                );
            }
            return;
        }

        // Apply modified damage
        if (damage != event.getDamage()) {
            event.setDamage(damage);
        }

        // Check for Second Wind and Last Stand AFTER damage is applied
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || !player.isValid()) return;

            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

            // Second Wind: Instantly heal 5 hearts when below 5 hearts (5 min cooldown)
            if (currentHealth <= 10.0 && effectManager.hasSkillByName(player, "second_wind")) {
                if (plugin.getCooldownManager().tryActivateSkill(player, "second_wind_passive", 300)) {
                    // Heal 5 hearts (10 HP)
                    double newHealth = Math.min(currentHealth + 10.0, maxHealth);
                    player.setHealth(newHealth);

                    // Visual: Burst of regeneration particles
                    player.getWorld().spawnParticle(
                        Particle.HEART,
                        player.getLocation().add(0, 2, 0),
                        20,
                        0.5, 0.5, 0.5,
                        0.1
                    );
                    player.getWorld().spawnParticle(
                        Particle.TOTEM,
                        player.getLocation().add(0, 1, 0),
                        30,
                        0.3, 0.5, 0.3,
                        0.2
                    );

                    // Sound
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

                    player.sendMessage(org.bukkit.ChatColor.GREEN + "💚 Second Wind activated! +5 hearts");
                }
            }

            // Last Stand: Below 10 hearts → +30% damage & +20% damage resist for 10s (2 min cooldown)
            if (currentHealth <= 20.0 && effectManager.hasSkillByName(player, "last_stand")) {
                if (plugin.getCooldownManager().tryActivateSkill(player, "last_stand_passive", 120)) {
                    // Apply potion effects
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE,
                        200, // 10 seconds
                        0,   // Level I (+30% damage from skill calculation)
                        false,
                        true,
                        true
                    ));
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE,
                        200, // 10 seconds
                        0,   // Level I (+20% resist from skill calculation)
                        false,
                        true,
                        true
                    ));

                    // Visual: Glowing + flame particles
                    player.setGlowing(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.setGlowing(false);
                    }, 200L);

                    player.getWorld().spawnParticle(
                        Particle.FLAME,
                        player.getLocation().add(0, 1, 0),
                        30,
                        0.3, 0.5, 0.3,
                        0.1
                    );

                    // Sound
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);

                    player.sendMessage(org.bukkit.ChatColor.RED + "⚔ Last Stand! +30% damage, +20% resistance!");
                }
            }
        });
    }

    /**
     * Handle food/hunger changes with Hunger Resistance
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Hunger Resistance: +30% food saturation
        if (event.getFoodLevel() > player.getFoodLevel()) {
            // Player is eating
            float baseSaturation = 5.0f; // Approximate base saturation
            float bonusSaturation = effectManager.applyHungerResistance(player, baseSaturation);

            if (bonusSaturation > baseSaturation) {
                player.setSaturation(player.getSaturation() + (bonusSaturation - baseSaturation));
            }
        }
    }

    /**
     * Apply regeneration effect (called from scheduled task)
     */
    public void applyRegeneration(Player player) {
        if (effectManager.canRegenerate(player)) {
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

            // Heal 0.5 HP (don't exceed max)
            double newHealth = Math.min(currentHealth + 0.5, maxHealth);
            player.setHealth(newHealth);

            // Visual: Green regeneration particles
            player.getWorld().spawnParticle(
                Particle.VILLAGER_HAPPY,
                player.getLocation().add(0, 1, 0),
                3,
                0.3, 0.3, 0.3,
                0.05
            );
        }
    }

    /**
     * Apply passive stat bonuses (Hardy)
     */
    private void applyPassiveStatBonuses(Player player) {
        // Apply Hardy (+3 hearts max HP)
        effectManager.applyHardy(player);
    }
}
