package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final SeasonsOfConflict plugin;

    public PlayerRespawnListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerData data = plugin.getGameManager().getPlayerData(event.getPlayer());
        int teamId = data.getTeamId();
        
        if (teamId > 0) {
            event.setRespawnLocation(plugin.getTerritoryManager().getTerritory(teamId)
                .getBeaconLocation(event.getPlayer().getWorld().getName()));
        }
    }
}
