package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.Season;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class MobSpawnListener implements Listener {

    private final SeasonsOfConflict plugin;

    public MobSpawnListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        // Only affect natural spawns
        if (event.getSpawnReason() != SpawnReason.NATURAL) return;
        if (!(event.getEntity() instanceof Monster)) return;

        Season season = plugin.getGameManager().getGameState().getCurrentSeason();

        // Spring: Fewer hostile mob spawns
        if (season == Season.SPRING) {
            double spawnMultiplier = plugin.getConfig().getDouble("seasons.spring.mob_spawn_multiplier", 0.5);

            // Cancel some spawns to reduce overall mob count
            if (Math.random() > spawnMultiplier) {
                event.setCancelled(true);
            }
        }
    }
}
