package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Updates active world event effects every 5 seconds
 */
public class WorldEventUpdateTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public WorldEventUpdateTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getWorldEventManager().updateActiveEvent();
    }
}
