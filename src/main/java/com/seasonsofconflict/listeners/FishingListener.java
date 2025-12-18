package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.Season;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FishingListener implements Listener {

    private final SeasonsOfConflict plugin;

    public FishingListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item)) return;

        Player player = event.getPlayer();
        Item caughtItem = (Item) event.getCaught();
        ItemStack item = caughtItem.getItemStack();
        Season season = plugin.getGameManager().getGameState().getCurrentSeason();

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
}
