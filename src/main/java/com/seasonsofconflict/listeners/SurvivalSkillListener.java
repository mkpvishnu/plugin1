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

        // Fire Immunity: Negate fire/lava damage
        if ((cause == EntityDamageEvent.DamageCause.FIRE ||
             cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
             cause == EntityDamageEvent.DamageCause.LAVA) &&
            effectManager.hasFireImmunity(player)) {

            event.setCancelled(true);
            player.setFireTicks(0);

            // Visual: Fire resistance particles
            player.getWorld().spawnParticle(
                Particle.DRIP_WATER,
                player.getLocation().add(0, 1, 0),
                15,
                0.3, 0.5, 0.3,
                0.05
            );
            return;
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
