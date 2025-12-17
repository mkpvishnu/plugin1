package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

public class SeasonCheckTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public SeasonCheckTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getSeasonManager().checkSeasonTransition();
    }
}
