package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles potion effect resistance from Survival skills
 */
public class PotionEffectListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;

    public PotionEffectListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Handle potion effects being applied (Poison/Wither Resistance, Unstoppable)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPotionEffectApply(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) {
            return;
        }

        PotionEffectType type = event.getNewEffect().getType();
        int baseDuration = event.getNewEffect().getDuration();
        int amplifier = event.getNewEffect().getAmplifier();

        // Poison/Wither Resistance: 90% reduced duration
        if ((type.equals(PotionEffectType.POISON) || type.equals(PotionEffectType.WITHER)) &&
            effectManager.hasSkillByName(player, "poison_wither_resistance")) {

            int reducedDuration = effectManager.getReducedPotionDuration(player, baseDuration);

            // Apply reduced duration effect
            event.setCancelled(true);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                type,
                reducedDuration,
                amplifier,
                false,
                true,
                true
            ));
        }

        // Unstoppable: 75% reduced duration of debuffs
        if ((type.equals(PotionEffectType.SLOW) ||
             type.equals(PotionEffectType.WEAKNESS) ||
             type.equals(PotionEffectType.SLOW_DIGGING)) &&
            effectManager.hasSkillByName(player, "unstoppable")) {

            int reducedDuration = effectManager.getUnstoppableDebuffDuration(player, baseDuration);

            // Apply reduced duration effect
            event.setCancelled(true);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                type,
                reducedDuration,
                amplifier,
                false,
                true,
                true
            ));
        }
    }

    /**
     * Handle poison/wither damage (75% damage reduction)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPoisonWitherDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Poison/Wither Resistance: 75% damage reduction
        if ((cause == EntityDamageEvent.DamageCause.POISON ||
             cause == EntityDamageEvent.DamageCause.WITHER) &&
            effectManager.hasSkillByName(player, "poison_wither_resistance")) {

            double damage = event.getDamage();
            double reducedDamage = effectManager.applyPoisonWitherResistance(player, damage);

            if (reducedDamage != damage) {
                event.setDamage(reducedDamage);

                // Visual: Immunity particles
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.VILLAGER_HAPPY,
                    player.getLocation().add(0, 1, 0),
                    8,
                    0.3, 0.3, 0.3,
                    0.05
                );
            }
        }
    }
}
