package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final SeasonsOfConflict plugin;

    public PlayerDeathListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        plugin.getHealthManager().handleDeath(victim);

        if (killer != null && killer != victim) {
            plugin.getCombatManager().handlePlayerKill(killer, victim);
        }

        plugin.getGameManager().checkWinCondition();
    }
}
