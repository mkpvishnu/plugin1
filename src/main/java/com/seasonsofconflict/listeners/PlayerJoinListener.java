package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SeasonsOfConflict plugin;

    public PlayerJoinListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getGameManager().getPlayerData(player);

        data.setName(player.getName());

        if (data.getTeamId() == 0) {
            plugin.getTeamManager().assignPlayerToTeam(player);
            plugin.getHealthManager().setMaxHealth(player);
            
            int teamId = data.getTeamId();
            player.teleport(plugin.getTerritoryManager().getTerritory(teamId).getBeaconLocation(player.getWorld().getName()));

            MessageUtils.sendMessage(player, "&aWelcome to Seasons of Conflict!");
            MessageUtils.sendMessage(player, "&eYou have been assigned to team: " +
                plugin.getTeamManager().getTeam(teamId).getColoredName());
        } else {
            plugin.getHealthManager().setMaxHealth(player);

            if (!data.isAlive()) {
                player.setGameMode(GameMode.SPECTATOR);
                MessageUtils.sendMessage(player, "&cYou are dead. Your team can revive you with /revive");
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
}
