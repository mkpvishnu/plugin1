package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.TerritoryData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CaptureTickTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public CaptureTickTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Territory capture logic will be implemented by TerritoryManager
        // This task just triggers the check every second
    }
}
