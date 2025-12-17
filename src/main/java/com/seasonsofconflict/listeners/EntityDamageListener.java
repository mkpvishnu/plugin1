package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {

    private final SeasonsOfConflict plugin;

    public EntityDamageListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Monster && event.getEntity() instanceof Player) {
            double multiplier = plugin.getGameManager().getGameState().getMobDamageMultiplier();
            if (plugin.getGameManager().getGameState().getCurrentSeason() == Season.SUMMER) {
                multiplier *= 1.5;
            }
            event.setDamage(event.getDamage() * multiplier);
        }

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData data = plugin.getGameManager().getPlayerData(player);
            data.setLastCombatTime(System.currentTimeMillis());
        }
        
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            PlayerData data = plugin.getGameManager().getPlayerData(damager);
            data.setLastCombatTime(System.currentTimeMillis());
        }
    }
}
