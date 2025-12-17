package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final SeasonsOfConflict plugin;

    public PlayerMoveListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData data = plugin.getGameManager().getPlayerData(player);
        
        TerritoryData newTerritory = plugin.getTerritoryManager().getTerritoryAt(event.getTo());
        int newTerritoryId = newTerritory != null ? newTerritory.getTerritoryId() : 0;
        
        if (newTerritoryId != data.getCurrentTerritoryId()) {
            data.setCurrentTerritoryId(newTerritoryId);
            data.setLastTerritoryEnterTime(System.currentTimeMillis());
            
            if (newTerritory != null) {
                MessageUtils.sendMessage(player, "&eEntered territory: &f" + newTerritory.getName());
                if (newTerritory.getOwnerTeamId() != 0) {
                    MessageUtils.sendMessage(player, "&7Controlled by: " + 
                        plugin.getTeamManager().getTeam(newTerritory.getOwnerTeamId()).getColoredName());
                }
            }
        }
    }
}
