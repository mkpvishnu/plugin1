package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles territory capture mechanics.
 * Runs every second to update capture progress.
 *
 * Capture Rules:
 * - Requires 3+ alive team members within capture radius (default: 10 blocks)
 * - Takes 300 seconds (5 minutes) to capture
 * - Progress decays when requirements not met
 * - Enemies within defense radius can contest capture
 */
public class CaptureTickTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public CaptureTickTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Process each territory
        for (TerritoryData territory : plugin.getTerritoryManager().getAllTerritories()) {
            processTerritoryCapture(territory);
        }
    }

    /**
     * Process capture progress for a single territory
     */
    private void processTerritoryCapture(TerritoryData territory) {
        // Count players from each team within capture radius
        Map<Integer, Integer> teamCounts = countPlayersInCaptureRadius(territory);

        // Find the team with most players (must have 3+)
        int capturingTeamId = 0;
        int maxPlayers = 0;

        for (Map.Entry<Integer, Integer> entry : teamCounts.entrySet()) {
            int teamId = entry.getKey();
            int playerCount = entry.getValue();

            // Must have minimum players and not already own the territory
            int minPlayers = plugin.getConfig().getInt("capture.min_players", 3);
            if (playerCount >= minPlayers && teamId != territory.getOwnerTeamId()) {
                if (playerCount > maxPlayers) {
                    maxPlayers = playerCount;
                    capturingTeamId = teamId;
                }
            }
        }

        // Check if enemies are contesting (within defense radius)
        boolean contested = false;
        if (capturingTeamId != 0) {
            contested = plugin.getTerritoryManager().hasEnemyInDefenseRadius(territory, capturingTeamId);
        }

        // Handle capture progress
        if (capturingTeamId != 0 && !contested) {
            // Team is capturing
            handleCaptureProgress(territory, capturingTeamId);
        } else {
            // No valid capture or contested - decay progress
            handleCaptureDecay(territory, contested);
        }
    }

    /**
     * Count alive players from each team within capture radius
     */
    private Map<Integer, Integer> countPlayersInCaptureRadius(TerritoryData territory) {
        Map<Integer, Integer> teamCounts = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = plugin.getGameManager().getPlayerData(player);

            // Only count alive players
            if (!playerData.isAlive()) continue;

            // Check if player is in capture radius
            if (plugin.getTerritoryManager().isInCaptureRadius(player, territory)) {
                int teamId = playerData.getTeamId();
                teamCounts.put(teamId, teamCounts.getOrDefault(teamId, 0) + 1);
            }
        }

        return teamCounts;
    }

    /**
     * Handle capture progress increase
     */
    private void handleCaptureProgress(TerritoryData territory, int capturingTeamId) {
        int currentCapturingTeam = territory.getCapturingTeamId();
        int currentProgress = territory.getCaptureProgress();

        // If different team started capturing, reset progress
        if (currentCapturingTeam != capturingTeamId && currentCapturingTeam != 0) {
            territory.setCapturingTeamId(capturingTeamId);
            territory.setCaptureProgress(0);
            plugin.getTerritoryManager().saveTerritory(territory);

            TeamData newTeam = plugin.getTeamManager().getTeam(capturingTeamId);
            if (newTeam != null) {
                notifyNearbyPlayers(territory, "&e" + newTeam.getColoredName() +
                    " &eis now capturing &f" + territory.getName() + "&e!");
            }
            return;
        }

        // Set capturing team if not set
        if (currentCapturingTeam == 0) {
            territory.setCapturingTeamId(capturingTeamId);
        }

        // Increment progress
        currentProgress++;
        territory.setCaptureProgress(currentProgress);

        // Check if capture is complete
        int captureTime = plugin.getConfig().getInt("capture.time_seconds", 300);
        if (currentProgress >= captureTime) {
            // Complete capture
            plugin.getTerritoryManager().captureTerritory(territory, capturingTeamId);
        } else {
            // Send progress updates at intervals
            if (currentProgress % 30 == 0) {
                int percentComplete = (currentProgress * 100) / captureTime;
                TeamData capturingTeam = plugin.getTeamManager().getTeam(capturingTeamId);
                if (capturingTeam != null) {
                    notifyNearbyPlayers(territory, "&a" + capturingTeam.getColoredName() +
                        " &ais capturing &f" + territory.getName() + " &7(" + percentComplete + "%)");
                }
            }

            // Save progress
            plugin.getTerritoryManager().saveTerritory(territory);
        }
    }

    /**
     * Handle capture progress decay
     */
    private void handleCaptureDecay(TerritoryData territory, boolean contested) {
        int currentProgress = territory.getCaptureProgress();

        if (currentProgress > 0) {
            // Decay progress
            int decayRate = plugin.getConfig().getInt("capture.decay_rate", 2);
            currentProgress = Math.max(0, currentProgress - decayRate);
            territory.setCaptureProgress(currentProgress);

            // If progress fully decayed, reset capturing team
            if (currentProgress == 0 && territory.getCapturingTeamId() != 0) {
                if (contested) {
                    notifyNearbyPlayers(territory, "&cCapture of &f" + territory.getName() +
                        " &chas been contested!");
                }
                territory.setCapturingTeamId(0);
            }

            plugin.getTerritoryManager().saveTerritory(territory);
        }
    }

    /**
     * Notify all players within a radius of the territory beacon
     */
    private void notifyNearbyPlayers(TerritoryData territory, String message) {
        String worldName = plugin.getConfig().getString("game.world_name", "world");
        double notifyRadius = plugin.getConfig().getDouble("capture.notify_radius", 100.0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(worldName)) {
                double distance = territory.getDistanceToBeacon(player.getLocation());
                if (distance <= notifyRadius) {
                    MessageUtils.sendMessage(player, message);
                }
            }
        }
    }
}
