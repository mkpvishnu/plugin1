package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodically checks if a new world event should start
 * Runs every 2 hours by default (configurable)
 */
public class WorldEventCheckTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public WorldEventCheckTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getWorldEventManager().checkForNewEvent();
    }
}
