package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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

        if (isOre(type)) {
            double bonus = plugin.getTerritoryManager().getTerritoryBonus(player, BonusType.ORE);
            if (bonus > 1.0 && Math.random() < (bonus - 1.0)) {
                block.getWorld().dropItemNaturally(block.getLocation(), event.getBlock().getDrops().iterator().next());
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
            plugin.getQuestManager().updateQuestProgress(player, "logs_chopped", 1);
        }
    }

    private boolean isOre(Material type) {
        return type.name().endsWith("_ORE");
    }

    private boolean isLog(Material type) {
        return type.name().endsWith("_LOG") || type.name().endsWith("_WOOD");
    }
}
