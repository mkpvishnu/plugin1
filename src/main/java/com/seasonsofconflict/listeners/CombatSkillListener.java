package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles Combat Tree passive skill effects
 */
public class CombatSkillListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;

    public CombatSkillListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Apply passive stat bonuses on player join (Swift Strikes, Iron Skin)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay by 1 tick to ensure player is fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyPassiveStatBonuses(player);
        }, 1L);
    }

    /**
     * Clear combat state on player quit
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        effectManager.clearPlayerState(event.getPlayer().getUniqueId());
    }

    /**
     * Handle combat damage with passive skill effects
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only handle player attackers
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        // Track combat time for regeneration
        effectManager.updateCombatTime(attacker);

        // Only modify damage to living entities
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        double baseDamage = event.getDamage();
        double finalDamage = baseDamage;

        // Apply Bloodlust (+5% damage per stack, max 3 stacks)
        if (effectManager.hasSkillByName(attacker, "bloodlust")) {
            double bloodlustDamage = effectManager.applyBloodlust(attacker, target.getUniqueId(), finalDamage);

            if (bloodlustDamage > finalDamage) {
                finalDamage = bloodlustDamage;

                // Visual: Red eyes at max stacks
                int stacks = effectManager.getBloodlustStacks(attacker, target.getUniqueId());
                if (stacks == 3) {
                    target.getWorld().spawnParticle(
                        Particle.DAMAGE_INDICATOR,
                        target.getEyeLocation(),
                        5,
                        0.3, 0.3, 0.3,
                        0.1
                    );
                }
            }
        }

        // Apply Critical Precision (20% chance for +50% damage)
        if (effectManager.hasSkillByName(attacker, "critical_precision")) {
            double critDamage = effectManager.applyCriticalPrecision(attacker, finalDamage);

            if (critDamage > finalDamage) {
                finalDamage = critDamage;

                // Visual: "CRIT!" particles + sound
                target.getWorld().spawnParticle(
                    Particle.CRIT_MAGIC,
                    target.getLocation().add(0, 1, 0),
                    15,
                    0.5, 0.5, 0.5,
                    0.3
                );
                target.getWorld().playSound(
                    target.getLocation(),
                    Sound.ENTITY_PLAYER_ATTACK_CRIT,
                    1.0f,
                    1.2f
                );

                // Display "CRIT!" message
                MessageUtils.sendMessage(attacker, "&c&lCRIT! &7+" +
                    String.format("%.1f", (critDamage - (finalDamage / 1.5))) + " damage");
            }
        }

        // Apply Execution (bonus damage to low HP targets)
        if (effectManager.hasSkillByName(attacker, "execution")) {
            double healthPercent = target.getHealth() / target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            double executeDamage = effectManager.applyExecution(attacker, finalDamage, healthPercent);

            if (executeDamage > finalDamage) {
                finalDamage = executeDamage;

                // Visual: Dark particles for execution
                target.getWorld().spawnParticle(
                    Particle.SMOKE_LARGE,
                    target.getLocation().add(0, 1, 0),
                    10,
                    0.3, 0.3, 0.3,
                    0.05
                );
                target.getWorld().playSound(
                    target.getLocation(),
                    Sound.ENTITY_WITHER_HURT,
                    0.5f,
                    0.8f
                );
            }
        }

        // Apply Armor Breaker (30% armor penetration)
        // Note: This is simplified - full implementation would require calculating armor values
        if (effectManager.hasSkillByName(attacker, "armor_breaker")) {
            // Add 30% of base damage as true damage (bypassing armor)
            finalDamage = finalDamage * 1.15; // Approximate armor penetration effect

            // Visual: Crack particles
            target.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                target.getLocation().add(0, 1, 0),
                20,
                0.5, 0.5, 0.5,
                0.1,
                Material.CRACKED_STONE_BRICKS.createBlockData()
            );
        }

        // Set final damage
        if (finalDamage != baseDamage) {
            event.setDamage(finalDamage);
        }
    }

    /**
     * Apply passive stat bonuses (Swift Strikes, Iron Skin)
     */
    private void applyPassiveStatBonuses(Player player) {
        // Reset to base stats first
        resetPlayerStats(player);

        // Apply Swift Strikes (+15% attack speed)
        effectManager.applySwiftStrikes(player);

        // Apply Iron Skin (+2 hearts max HP)
        effectManager.applyIronSkin(player);
    }

    /**
     * Reset player stats to base values
     */
    private void resetPlayerStats(Player player) {
        // Reset attack speed to base (4.0)
        var attackSpeed = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.setBaseValue(4.0);
        }

        // Reset max health to base (20.0 hearts = 40 HP)
        // Note: This will be modified by HealthManager for permadeath system
        var maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0);
        }
    }
}
