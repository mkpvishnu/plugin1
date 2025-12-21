package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.listeners.GatheringSkillListener;
import com.seasonsofconflict.listeners.SurvivalSkillListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles continuous passive skill effects that need periodic application
 */
public class PassiveEffectsTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;
    private final GatheringSkillListener gatheringListener;
    private final SurvivalSkillListener survivalListener;

    public PassiveEffectsTask(SeasonsOfConflict plugin,
                             GatheringSkillListener gatheringListener,
                             SurvivalSkillListener survivalListener) {
        this.plugin = plugin;
        this.gatheringListener = gatheringListener;
        this.survivalListener = survivalListener;
    }

    @Override
    public void run() {
        // Apply effects to all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Swift Hands: Continuous Haste effect (renewed every 10s)
            gatheringListener.applySwiftHandsEffect(player);

            // Regeneration: 0.5 HP every 5s when out of combat
            // (This task runs every 5s, so perfect timing)
            survivalListener.applyRegeneration(player);
        }
    }
}
