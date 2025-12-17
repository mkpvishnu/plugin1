package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class DailyResetTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;
    private int lastDay;

    public DailyResetTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.lastDay = LocalDateTime.now().getDayOfMonth();
    }

    @Override
    public void run() {
        int currentDay = LocalDateTime.now().getDayOfMonth();
        if (currentDay != lastDay) {
            lastDay = currentDay;
            plugin.getGameManager().performDailyReset();
        }
    }
}
