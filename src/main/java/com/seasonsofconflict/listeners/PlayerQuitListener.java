package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SeasonsOfConflict plugin;

    public PlayerQuitListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Just save player data when they quit (combat logging no longer punished)
        plugin.getGameManager().savePlayerData(player.getUniqueId());
    }
}
