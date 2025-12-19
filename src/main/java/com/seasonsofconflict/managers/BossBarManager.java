package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.GameState;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.models.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages boss bars for capture progress, season timers, and apocalypse warnings
 */
public class BossBarManager {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, BossBar> captureBars;
    private final Map<UUID, BossBar> seasonBars;
    private final Map<UUID, BossBar> borderBars;

    public BossBarManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.captureBars = new HashMap<>();
        this.seasonBars = new HashMap<>();
        this.borderBars = new HashMap<>();
    }

    /**
     * Update all boss bars for a player
     */
    public void updateBossBarForPlayer(Player player) {
        updateCaptureBossBar(player);
        updateSeasonBossBar(player);
        updateBorderBossBar(player);
    }

    /**
     * Update capture progress boss bar
     * Shows when player is near an enemy beacon being captured
     */
    private void updateCaptureBossBar(Player player) {
        TerritoryData nearbyTerritory = getNearbyCapturingTerritory(player);

        if (nearbyTerritory != null && nearbyTerritory.getCaptureProgress() > 0) {
            // Show capture progress
            BossBar bar = captureBars.get(player.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar(
                    "Capturing Territory...",
                    BarColor.YELLOW,
                    BarStyle.SEGMENTED_10
                );
                bar.addPlayer(player);
                captureBars.put(player.getUniqueId(), bar);
            }

            double progress = nearbyTerritory.getCaptureProgress() / 300.0; // 300 seconds to capture
            bar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
            bar.setTitle("§eCapturing §6" + nearbyTerritory.getName() + " §7(" +
                         (int)(progress * 100) + "%)");

            // Change color based on progress
            if (progress < 0.33) {
                bar.setColor(BarColor.RED);
            } else if (progress < 0.66) {
                bar.setColor(BarColor.YELLOW);
            } else {
                bar.setColor(BarColor.GREEN);
            }
        } else {
            // Hide capture bar
            removeCaptureBar(player);
        }
    }

    /**
     * Update season timer boss bar
     */
    private void updateSeasonBossBar(Player player) {
        GameState gameState = plugin.getGameManager().getGameState();
        LocalDate seasonStart = gameState.getSeasonStartDate();
        LocalDate now = LocalDate.now();

        long daysPassed = ChronoUnit.DAYS.between(seasonStart, now);
        long daysRemaining = 30 - daysPassed;

        if (daysRemaining <= 7 && daysRemaining > 0) {
            // Show season timer when less than 7 days remain
            BossBar bar = seasonBars.get(player.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar(
                    "Season Timer",
                    BarColor.BLUE,
                    BarStyle.SOLID
                );
                bar.addPlayer(player);
                seasonBars.put(player.getUniqueId(), bar);
            }

            double progress = daysRemaining / 30.0;
            bar.setProgress(Math.min(1.0, Math.max(0.0, progress)));

            Season currentSeason = gameState.getCurrentSeason();
            Season nextSeason = currentSeason.getNext();
            bar.setTitle("§b" + currentSeason.name() + " §7→ §e" + nextSeason.name() +
                         " §7in §f" + daysRemaining + " day" + (daysRemaining == 1 ? "" : "s"));

            // Color based on urgency
            if (daysRemaining <= 2) {
                bar.setColor(BarColor.RED);
            } else if (daysRemaining <= 5) {
                bar.setColor(BarColor.YELLOW);
            } else {
                bar.setColor(BarColor.BLUE);
            }
        } else {
            // Hide season bar
            removeSeasonBar(player);
        }
    }

    /**
     * Update world border warning boss bar
     */
    private void updateBorderBossBar(Player player) {
        if (!plugin.getGameManager().getGameState().isApocalypse()) {
            removeBorderBar(player);
            return;
        }

        double distanceToBorder = getDistanceToBorder(player);

        if (distanceToBorder < 200) {
            // Show border warning when within 200 blocks
            BossBar bar = borderBars.get(player.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar(
                    "World Border Warning",
                    BarColor.RED,
                    BarStyle.SOLID
                );
                bar.addPlayer(player);
                borderBars.put(player.getUniqueId(), bar);
            }

            double progress = distanceToBorder / 200.0;
            bar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
            bar.setTitle("§c⚠ World Border: §f" + (int)distanceToBorder + " blocks");

            // Color based on danger
            if (distanceToBorder < 50) {
                bar.setColor(BarColor.RED);
            } else if (distanceToBorder < 100) {
                bar.setColor(BarColor.YELLOW);
            } else {
                bar.setColor(BarColor.BLUE);
            }
        } else {
            removeBorderBar(player);
        }
    }

    /**
     * Get nearby territory that is being captured
     */
    private TerritoryData getNearbyCapturingTerritory(Player player) {
        for (int territoryId = 1; territoryId <= 5; territoryId++) {
            TerritoryData territory = plugin.getTerritoryManager().getTerritory(territoryId);
            if (territory == null) continue;

            if (territory.getCaptureProgress() > 0 &&
                plugin.getTerritoryManager().isInCaptureRadius(player, territory)) {
                return territory;
            }
        }
        return null;
    }

    /**
     * Get distance to nearest world border
     */
    private double getDistanceToBorder(Player player) {
        double borderSize = plugin.getGameManager().getGameState().getWorldBorderSize() / 2.0;
        double x = Math.abs(player.getLocation().getX());
        double z = Math.abs(player.getLocation().getZ());

        double distanceX = borderSize - x;
        double distanceZ = borderSize - z;

        return Math.min(distanceX, distanceZ);
    }

    /**
     * Remove all boss bars for a player
     */
    public void removeAllBars(Player player) {
        removeCaptureBar(player);
        removeSeasonBar(player);
        removeBorderBar(player);
    }

    private void removeCaptureBar(Player player) {
        BossBar bar = captureBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void removeSeasonBar(Player player) {
        BossBar bar = seasonBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void removeBorderBar(Player player) {
        BossBar bar = borderBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    /**
     * Clean up all boss bars
     */
    public void cleanup() {
        for (BossBar bar : captureBars.values()) {
            bar.removeAll();
        }
        for (BossBar bar : seasonBars.values()) {
            bar.removeAll();
        }
        for (BossBar bar : borderBars.values()) {
            bar.removeAll();
        }

        captureBars.clear();
        seasonBars.clear();
        borderBars.clear();
    }
}
