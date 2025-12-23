package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EntityDeathListener implements Listener {

    private final SeasonsOfConflict plugin;

    public EntityDeathListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Season season = plugin.getGameManager().getGameState().getCurrentSeason();
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        // Handle mob deaths (combat)
        if (entity instanceof Monster) {
            if (killer == null) return;

            PlayerData data = plugin.getGameManager().getPlayerData(killer);
            data.incrementDailyMobKills();

            String mobType = entity.getType().name().toLowerCase();
            plugin.getQuestManager().updateQuestProgress(killer, mobType + "_kills", 1);
            plugin.getQuestManager().updateQuestProgress(killer, "mob_kills", 1);

            double xpBonus = plugin.getTerritoryManager().getTerritoryBonus(killer, BonusType.XP);
            if (xpBonus > 1.0) {
                event.setDroppedExp((int) (event.getDroppedExp() * xpBonus));
            }

            // Relentless Assault: Each kill grants +20% attack speed for 10s (stacks 3x)
            if (plugin.getSkillEffectManager().hasSkillByName(killer, "relentless_assault")) {
                applyRelentlessAssault(killer);
            }

            // Fall: +25% all drops from mobs
            if (season == Season.FALL) {
                applyFallDropBonus(event);
            }
        }

        // Handle animal deaths (food production)
        if (isAnimal(entity)) {
            // Winter: -50% food production
            if (season == Season.WINTER) {
                applyWinterFoodReduction(event);
            }

            // Fall: +25% all drops from animals
            if (season == Season.FALL) {
                applyFallDropBonus(event);
            }
        }
    }

    private void applyFallDropBonus(EntityDeathEvent event) {
        double fallMultiplier = plugin.getConfig().getDouble("seasons.fall.all_drops_multiplier", 1.25);
        if (Math.random() < (fallMultiplier - 1.0)) {
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                if (drop != null && drop.getAmount() > 0) {
                    extraDrops.add(drop.clone());
                }
            }
            event.getDrops().addAll(extraDrops);
        }
    }

    private void applyWinterFoodReduction(EntityDeathEvent event) {
        double foodMultiplier = plugin.getConfig().getDouble("seasons.winter.food_production_multiplier", 0.5);
        List<ItemStack> toRemove = new ArrayList<>();

        for (ItemStack drop : event.getDrops()) {
            if (drop != null && isFood(drop.getType())) {
                if (Math.random() > foodMultiplier) {
                    toRemove.add(drop);
                }
            }
        }

        event.getDrops().removeAll(toRemove);
    }

    private boolean isAnimal(LivingEntity entity) {
        return entity instanceof Cow ||
               entity instanceof Pig ||
               entity instanceof Sheep ||
               entity instanceof Chicken ||
               entity instanceof Rabbit ||
               entity instanceof MushroomCow;
    }

    private boolean isFood(Material type) {
        return type == Material.BEEF ||
               type == Material.PORKCHOP ||
               type == Material.MUTTON ||
               type == Material.CHICKEN ||
               type == Material.RABBIT ||
               type == Material.COD ||
               type == Material.SALMON ||
               type == Material.TROPICAL_FISH ||
               type == Material.PUFFERFISH;
    }

    /**
     * Relentless Assault: Each kill grants +20% attack speed for 10s (stacks 3x)
     */
    private void applyRelentlessAssault(Player player) {
        // Apply Speed effect (simulates attack speed increase)
        int currentLevel = 0;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            currentLevel = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
        }

        // Max 3 stacks (levels 0, 1, 2)
        int newLevel = Math.min(currentLevel + 1, 2);

        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.SPEED,
            200, // 10 seconds
            newLevel,
            false,
            true,
            true
        ));

        // Visual: Speed lines particle effect
        player.getWorld().spawnParticle(
            org.bukkit.Particle.CRIT,
            player.getLocation().add(0, 1, 0),
            15,
            0.5, 0.5, 0.5,
            0.3
        );

        // Sound
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.5f);

        // Message with stack count
        player.sendMessage(org.bukkit.ChatColor.RED + "⚔ Relentless Assault! (" + (newLevel + 1) + "/3 stacks)");
    }
}
