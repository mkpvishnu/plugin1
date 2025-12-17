package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardUpdateTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public ScoreboardUpdateTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Scoreboard update logic (to be implemented)
    }
}
