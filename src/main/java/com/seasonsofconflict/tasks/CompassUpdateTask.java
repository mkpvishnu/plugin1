package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Updates compass targets for all players every 5 seconds
 */
public class CompassUpdateTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public CompassUpdateTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getCompassTrackingListener().updateAllCompassTargets();
    }
}
