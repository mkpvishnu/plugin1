package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Updates boss bars for all online players every 2 seconds
 */
public class BossBarUpdateTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public BossBarUpdateTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getBossBarManager().updateBossBarForPlayer(player);
        }
    }
}
