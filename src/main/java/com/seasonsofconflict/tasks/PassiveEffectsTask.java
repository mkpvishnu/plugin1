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

            // Strategic Mind: Make teammates glow to see through walls
            applyStrategicMind(player);
        }
    }

    /**
     * Strategic Mind: Apply glowing effect to teammates
     */
    private void applyStrategicMind(Player player) {
        if (!plugin.getSkillEffectManager().hasStrategicMind(player)) {
            return;
        }

        // Get player's team
        int playerTeam = plugin.getGameManager().getPlayerData(player).getTeamId();

        // Apply glowing to all teammates (no range limit, server-wide)
        for (Player teammate : Bukkit.getOnlinePlayers()) {
            if (teammate.getUniqueId().equals(player.getUniqueId())) {
                continue; // Skip self
            }

            int teammateTeam = plugin.getGameManager().getPlayerData(teammate).getTeamId();

            if (playerTeam == teammateTeam) {
                // Make teammate glow (visible through walls to this player)
                teammate.setGlowing(true);

                // Note: In Minecraft, glowing is global. For true per-player visibility,
                // we'd need to use packets/scoreboard teams. This is a simplified implementation.
            }
        }
    }
}
