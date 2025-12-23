package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.Season;
import org.bukkit.*;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishingListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;
    private final Random random = new Random();

    public FishingListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Efficient Fishing: -50% fishing time
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFishingStart(PlayerFishEvent event) {
        if (event.getState() != State.FISHING) return;

        Player player = event.getPlayer();

        // Efficient Fishing: Reduce fishing wait time by 50%
        if (effectManager.hasSkillByName(player, "efficient_fishing")) {
            if (event.getHook() != null) {
                FishHook hook = event.getHook();

                // Reduce both min and max wait times by 50%
                int minWait = hook.getMinWaitTime();
                int maxWait = hook.getMaxWaitTime();

                hook.setMinWaitTime((int) (minWait * 0.5));
                hook.setMaxWaitTime((int) (maxWait * 0.5));

                // Visual: Bubble particles around bobber
                hook.getWorld().spawnParticle(
                    Particle.BUBBLE_POP,
                    hook.getLocation(),
                    8,
                    0.3, 0.3, 0.3,
                    0.05
                );
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item)) return;

        Player player = event.getPlayer();
        Item caughtItem = (Item) event.getCaught();
        ItemStack item = caughtItem.getItemStack();
        Season season = plugin.getGameManager().getGameState().getCurrentSeason();

        // Efficient Fishing: +30% treasure chance
        if (effectManager.hasSkillByName(player, "efficient_fishing")) {
            if (isRegularFish(item.getType()) && random.nextDouble() < 0.30) {
                // Replace regular fish with treasure
                ItemStack treasure = getTreasureItem();
                caughtItem.setItemStack(treasure);

                // Visual: Gold sparkle particles
                player.getWorld().spawnParticle(
                    Particle.CRIT_MAGIC,
                    caughtItem.getLocation(),
                    20,
                    0.3, 0.3, 0.3,
                    0.15
                );

                // Sound: Level up sound
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

                // Message
                player.sendMessage(ChatColor.GOLD + "🎣 Efficient Fishing! Found treasure: " +
                    ChatColor.YELLOW + treasure.getType().name().toLowerCase().replace("_", " "));
            }
        }

        // Apply territory fishing bonus
        double fishBonus = plugin.getTerritoryManager().getTerritoryBonus(player, BonusType.FISH);
        if (fishBonus > 1.0 && Math.random() < (fishBonus - 1.0)) {
            // Drop extra fish
            player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
        }

        // Winter: -50% food production from fishing
        if (season == Season.WINTER && isFood(item.getType())) {
            double foodMultiplier = plugin.getConfig().getDouble("seasons.winter.food_production_multiplier", 0.5);
            if (Math.random() > foodMultiplier) {
                // Remove the caught item
                caughtItem.remove();
                event.setCancelled(true);
            }
        }

        // Fall: +25% all drops from fishing
        if (season == Season.FALL) {
            double fallMultiplier = plugin.getConfig().getDouble("seasons.fall.all_drops_multiplier", 1.25);
            if (Math.random() < (fallMultiplier - 1.0)) {
                // Drop extra item
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
            }
        }

        // Update quest progress
        plugin.getQuestManager().updateQuestProgress(player, "fish_caught", 1);
    }

    private boolean isFood(Material type) {
        return type == Material.COD ||
               type == Material.SALMON ||
               type == Material.TROPICAL_FISH ||
               type == Material.PUFFERFISH;
    }

    /**
     * Check if material is a regular fish (not treasure)
     */
    private boolean isRegularFish(Material type) {
        return type == Material.COD ||
               type == Material.SALMON ||
               type == Material.TROPICAL_FISH ||
               type == Material.PUFFERFISH;
    }

    /**
     * Get a random treasure item for fishing
     */
    private ItemStack getTreasureItem() {
        Material[] treasures = {
            Material.BOW,
            Material.ENCHANTED_BOOK,
            Material.NAME_TAG,
            Material.NAUTILUS_SHELL,
            Material.SADDLE,
            Material.LILY_PAD,
            Material.FISHING_ROD
        };

        Material treasure = treasures[random.nextInt(treasures.length)];
        return new ItemStack(treasure, 1);
    }
}
