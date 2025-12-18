package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {

    private final SeasonsOfConflict plugin;

    public BlockBreakListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        PlayerData data = plugin.getGameManager().getPlayerData(player);
        Season season = plugin.getGameManager().getGameState().getCurrentSeason();

        if (isOre(type)) {
            double bonus = plugin.getTerritoryManager().getTerritoryBonus(player, BonusType.ORE);
            if (bonus > 1.0 && Math.random() < (bonus - 1.0)) {
                block.getWorld().dropItemNaturally(block.getLocation(), event.getBlock().getDrops().iterator().next());
            }

            // Summer: +50% ore drops
            if (season == Season.SUMMER) {
                double oreMultiplier = plugin.getConfig().getDouble("seasons.summer.ore_drop_multiplier", 1.5);
                if (Math.random() < (oreMultiplier - 1.0)) {
                    for (ItemStack drop : event.getBlock().getDrops()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                }
            }

            // Fall: +25% all drops
            if (season == Season.FALL) {
                double fallMultiplier = plugin.getConfig().getDouble("seasons.fall.all_drops_multiplier", 1.25);
                if (Math.random() < (fallMultiplier - 1.0)) {
                    for (ItemStack drop : event.getBlock().getDrops()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                }
            }

            if (type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE) {
                plugin.getQuestManager().updateQuestProgress(player, "iron_mined", 1);
            } else if (type == Material.COAL_ORE || type == Material.DEEPSLATE_COAL_ORE) {
                plugin.getQuestManager().updateQuestProgress(player, "coal_mined", 1);
            } else if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
                // Read diamond drop chance from config (default 10%)
                double dropChance = plugin.getConfig().getDouble("diamond.drop_chance", 0.10);
                if (Math.random() > dropChance) {
                    event.setDropItems(false);
                }
            }
            data.incrementDailyOresMined();
        } else if (isLog(type)) {
            double bonus = plugin.getTerritoryManager().getTerritoryBonus(player, BonusType.WOOD);
            if (bonus > 1.0 && Math.random() < (bonus - 1.0)) {
                block.getWorld().dropItemNaturally(block.getLocation(), event.getBlock().getDrops().iterator().next());
            }

            // Fall: +25% all drops
            if (season == Season.FALL) {
                double fallMultiplier = plugin.getConfig().getDouble("seasons.fall.all_drops_multiplier", 1.25);
                if (Math.random() < (fallMultiplier - 1.0)) {
                    for (ItemStack drop : event.getBlock().getDrops()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                }
            }

            plugin.getQuestManager().updateQuestProgress(player, "logs_chopped", 1);
        } else if (isCrop(type)) {
            // Winter: -50% food production
            if (season == Season.WINTER) {
                double foodMultiplier = plugin.getConfig().getDouble("seasons.winter.food_production_multiplier", 0.5);
                if (Math.random() > foodMultiplier) {
                    event.setDropItems(false);
                }
            }

            // Fall: +25% all drops (including crops)
            if (season == Season.FALL) {
                double fallMultiplier = plugin.getConfig().getDouble("seasons.fall.all_drops_multiplier", 1.25);
                if (Math.random() < (fallMultiplier - 1.0)) {
                    for (ItemStack drop : event.getBlock().getDrops()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                }
            }
        }
    }

    private boolean isOre(Material type) {
        return type.name().endsWith("_ORE");
    }

    private boolean isLog(Material type) {
        return type.name().endsWith("_LOG") || type.name().endsWith("_WOOD");
    }

    private boolean isCrop(Material type) {
        return type == Material.WHEAT ||
               type == Material.CARROTS ||
               type == Material.POTATOES ||
               type == Material.BEETROOTS ||
               type == Material.SWEET_BERRY_BUSH ||
               type == Material.COCOA ||
               type == Material.NETHER_WART;
    }
}
