package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.scheduler.BukkitRunnable;

public class FreezingDamageTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public FreezingDamageTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getSeasonManager().applyFreezingDamage();
    }
}
