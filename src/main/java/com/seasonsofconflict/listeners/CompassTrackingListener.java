package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles compass tracking functionality
 * Right-click compass to cycle through tracking modes
 */
public class CompassTrackingListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, CompassMode> compassModes;

    public enum CompassMode {
        HOME_TERRITORY("Home Territory"),
        NEAREST_ENEMY_TERRITORY("Nearest Enemy Territory"),
        NEAREST_TEAMMATE("Nearest Teammate");

        private final String displayName;

        CompassMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public CompassMode next() {
            CompassMode[] modes = values();
            return modes[(this.ordinal() + 1) % modes.length];
        }
    }

    public CompassTrackingListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.compassModes = new HashMap<>();
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if right-clicking
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if holding a compass
        Material item = player.getInventory().getItemInMainHand().getType();
        if (item != Material.COMPASS) {
            return;
        }

        event.setCancelled(true); // Prevent compass from being used normally

        // Cycle tracking mode
        CompassMode currentMode = compassModes.getOrDefault(player.getUniqueId(), CompassMode.HOME_TERRITORY);
        CompassMode nextMode = currentMode.next();
        compassModes.put(player.getUniqueId(), nextMode);

        // Update compass target
        updateCompassTarget(player, nextMode);

        MessageUtils.sendMessage(player, "&eCompass now tracking: &6" + nextMode.getDisplayName());
    }

    /**
     * Update compass target based on mode
     */
    public void updateCompassTarget(Player player, CompassMode mode) {
        PlayerData data = plugin.getGameManager().getPlayerData(player);
        Location target = null;

        switch (mode) {
            case HOME_TERRITORY:
                target = getHomeTerritory(data.getTeamId());
                break;

            case NEAREST_ENEMY_TERRITORY:
                target = getNearestEnemyTerritory(player, data.getTeamId());
                break;

            case NEAREST_TEAMMATE:
                target = getNearestTeammate(player, data.getTeamId());
                break;
        }

        if (target != null) {
            player.setCompassTarget(target);
        } else {
            MessageUtils.sendMessage(player, "&cNo target found for this tracking mode");
        }
    }

    /**
     * Get home territory beacon location
     */
    private Location getHomeTerritory(int teamId) {
        TerritoryData territory = plugin.getTerritoryManager().getTerritory(teamId);
        if (territory != null) {
            return territory.getBeaconLocation(plugin.getServer().getWorlds().get(0).getName());
        }
        return null;
    }

    /**
     * Get nearest enemy territory beacon location
     */
    private Location getNearestEnemyTerritory(Player player, int playerTeamId) {
        Location playerLoc = player.getLocation();
        Location nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int territoryId = 1; territoryId <= 5; territoryId++) {
            TerritoryData territory = plugin.getTerritoryManager().getTerritory(territoryId);
            if (territory == null) continue;

            // Skip territories owned by player's team or neutral territories
            if (territory.getOwnerTeamId() == playerTeamId || territory.getOwnerTeamId() == 0) {
                continue;
            }

            Location beaconLoc = territory.getBeaconLocation(player.getWorld().getName());
            double distance = playerLoc.distance(beaconLoc);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = beaconLoc;
            }
        }

        return nearest;
    }

    /**
     * Get nearest living teammate location
     */
    private Location getNearestTeammate(Player player, int teamId) {
        Location playerLoc = player.getLocation();
        Location nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) continue; // Skip self

            PlayerData data = plugin.getGameManager().getPlayerData(online);
            if (data.getTeamId() == teamId && data.isAlive()) {
                double distance = playerLoc.distance(online.getLocation());

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = online.getLocation();
                }
            }
        }

        return nearest;
    }

    /**
     * Update all compass targets (called periodically)
     */
    public void updateAllCompassTargets() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getInventory().contains(Material.COMPASS)) {
                CompassMode mode = compassModes.getOrDefault(player.getUniqueId(), CompassMode.HOME_TERRITORY);
                updateCompassTarget(player, mode);
            }
        }
    }

    /**
     * Remove player data on quit
     */
    public void removePlayer(UUID uuid) {
        compassModes.remove(uuid);
    }
}
