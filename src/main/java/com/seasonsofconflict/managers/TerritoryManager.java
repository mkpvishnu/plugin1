package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class TerritoryManager {

    private final SeasonsOfConflict plugin;
    private final Map<Integer, TerritoryData> territories;

    /**
     * Seasonal modifier table [territoryId][seasonOrdinal]
     * Rows: Territory IDs 1-5 (ORE, WOOD, XP, CROP, FISH)
     * Columns: Seasons (SPRING=0, SUMMER=1, FALL=2, WINTER=3)
     * Values: Multiplier (1.0 = 100%, 1.5 = 150%, 0.5 = 50%)
     */
    private static final double[][] SEASONAL_MODIFIERS = {
        //    SPRING  SUMMER  FALL    WINTER
        {},                                      // Index 0 (unused, territories start at 1)
        {1.0,    0.75,   1.0,    1.5},          // Territory 1: ORE
        {1.25,   1.0,    1.5,    0.5},          // Territory 2: WOOD
        {1.0,    1.0,    1.25,   1.0},          // Territory 3: XP
        {1.5,    1.75,   1.25,   0.5},          // Territory 4: CROP
        {1.0,    1.5,    1.0,    1.25}          // Territory 5: FISH
    };

    public TerritoryManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.territories = new HashMap<>();
    }

    /**
     * Load all territories from database and config
     */
    public void loadTerritories() {
        // Load territories from database (includes state from DB and config from config.yml)
        List<TerritoryData> loadedTerritories = plugin.getDataManager().loadAllTerritories();

        if (loadedTerritories.isEmpty()) {
            // Initialize territories from config only
            initializeTerritoriesFromConfig();
        } else {
            // Load existing territories
            for (TerritoryData territory : loadedTerritories) {
                territories.put(territory.getTerritoryId(), territory);
            }
            plugin.getLogger().info("Loaded " + territories.size() + " territories from database");
        }

        // Initialize beacon blocks
        initializeBeacons();
    }

    /**
     * Initialize territories from config.yml (first time setup)
     */
    private void initializeTerritoriesFromConfig() {
        for (int territoryId = 1; territoryId <= 5; territoryId++) {
            String basePath = "territories." + territoryId + ".";
            String name = plugin.getConfig().getString(basePath + "name");
            int minX = plugin.getConfig().getInt(basePath + "bounds.minX");
            int maxX = plugin.getConfig().getInt(basePath + "bounds.maxX");
            int minZ = plugin.getConfig().getInt(basePath + "bounds.minZ");
            int maxZ = plugin.getConfig().getInt(basePath + "bounds.maxZ");
            int beaconX = plugin.getConfig().getInt(basePath + "beacon.x");
            int beaconY = plugin.getConfig().getInt(basePath + "beacon.y");
            int beaconZ = plugin.getConfig().getInt(basePath + "beacon.z");
            String bonusTypeStr = plugin.getConfig().getString(basePath + "bonus_type");
            BonusType bonusType = BonusType.valueOf(bonusTypeStr);
            int baseBonusPercent = plugin.getConfig().getInt(basePath + "base_bonus");

            TerritoryData territory = new TerritoryData(territoryId, name, minX, maxX, minZ, maxZ,
                                                         beaconX, beaconY, beaconZ,
                                                         bonusType, baseBonusPercent);

            // Set initial owner based on config (territory 3 is neutral, others match team ID)
            int startingOwner = plugin.getConfig().getInt(basePath + "starting_owner", territoryId);
            if (territoryId == 3) {
                // Contested Badlands starts neutral
                territory.setOwnerTeamId(0);
            } else {
                // Other territories start owned by their corresponding team
                territory.setOwnerTeamId(startingOwner);
            }

            territories.put(territoryId, territory);

            // Save to database
            plugin.getDataManager().saveTerritory(territory);
        }
        plugin.getLogger().info("Initialized " + territories.size() + " territories from config");
    }

    /**
     * Get territory at a specific location (null if not in any territory)
     */
    public TerritoryData getTerritoryAt(Location location) {
        if (location == null) return null;

        for (TerritoryData territory : territories.values()) {
            if (territory.isInTerritory(location)) {
                return territory;
            }
        }
        return null;
    }

    /**
     * Get territory by ID
     */
    public TerritoryData getTerritory(int territoryId) {
        return territories.get(territoryId);
    }

    /**
     * Get all territories
     */
    public Collection<TerritoryData> getAllTerritories() {
        return territories.values();
    }

    /**
     * Calculate territory bonus multiplier for a player, considering season
     * @param player The player
     * @param bonusType The type of bonus to calculate
     * @return Bonus multiplier (1.0 = no bonus, 1.5 = 50% bonus, 2.0 = 100% bonus)
     */
    public double getTerritoryBonus(Player player, BonusType bonusType) {
        if (player == null || bonusType == null) return 1.0;

        TerritoryData territory = getTerritoryAt(player.getLocation());
        if (territory == null) return 1.0;

        // Check if territory's bonus type matches what we're looking for
        if (territory.getBonusType() != bonusType) return 1.0;

        // Check if player's team owns this territory
        TeamData playerTeam = plugin.getTeamManager().getTeam(player);
        if (playerTeam == null) return 1.0;

        if (territory.getOwnerTeamId() != playerTeam.getTeamId()) return 1.0;

        // Calculate bonus
        double baseBonus = territory.getBaseBonusPercent() / 100.0; // e.g., 50 -> 0.5
        Season currentSeason = plugin.getGameManager().getGameState().getCurrentSeason();
        double seasonalModifier = getSeasonalModifier(territory.getTerritoryId(), currentSeason);

        // Final multiplier = 1 + (base_bonus * seasonal_modifier)
        // Example: 1 + (0.5 * 1.5) = 1.75 (75% bonus)
        return 1.0 + (baseBonus * seasonalModifier);
    }

    /**
     * Get seasonal modifier for a territory
     * @param territoryId Territory ID (1-5)
     * @param season Current season
     * @return Seasonal multiplier (1.0 = normal, 1.5 = +50%, 0.5 = -50%)
     */
    public double getSeasonalModifier(int territoryId, Season season) {
        if (territoryId < 1 || territoryId > 5) return 1.0;
        if (season == null) return 1.0;

        int seasonIndex = season.ordinal(); // SPRING=0, SUMMER=1, FALL=2, WINTER=3
        return SEASONAL_MODIFIERS[territoryId][seasonIndex];
    }

    /**
     * Initialize beacon blocks at each territory (indestructible)
     */
    public void initializeBeacons() {
        String worldName = plugin.getConfig().getString("game.world_name", "world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found, cannot initialize beacons");
            return;
        }

        for (TerritoryData territory : territories.values()) {
            Location beaconLoc = territory.getBeaconLocation(worldName);
            Block beaconBlock = beaconLoc.getBlock();

            // Place beacon if not already there
            if (beaconBlock.getType() != Material.BEACON) {
                beaconBlock.setType(Material.BEACON);
            }

            // Update beacon visual based on owner
            updateBeaconVisual(territory);
        }

        plugin.getLogger().info("Initialized beacons for all territories");
    }

    /**
     * Update beacon beam color to match owner team
     */
    public void updateBeaconVisual(TerritoryData territory) {
        if (territory == null) return;

        String worldName = plugin.getConfig().getString("game.world_name", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Location beaconLoc = territory.getBeaconLocation(worldName);
        Block beaconBlock = beaconLoc.getBlock();

        // Ensure beacon block exists
        if (beaconBlock.getType() != Material.BEACON) {
            beaconBlock.setType(Material.BEACON);
        }

        // Place stained glass above beacon to create colored beam
        Block glassBlock = beaconBlock.getRelative(0, 1, 0);

        int ownerTeamId = territory.getOwnerTeamId();
        if (ownerTeamId == 0) {
            // Neutral - use white glass
            glassBlock.setType(Material.WHITE_STAINED_GLASS);
        } else {
            // Team-owned - use team color
            TeamData ownerTeam = plugin.getTeamManager().getTeam(ownerTeamId);
            if (ownerTeam != null) {
                Material glassColor = getGlassColorForTeam(ownerTeam);
                glassBlock.setType(glassColor);
            }
        }
    }

    /**
     * Get stained glass material matching team color
     */
    private Material getGlassColorForTeam(TeamData team) {
        return switch (team.getColor()) {
            case AQUA, DARK_AQUA -> Material.CYAN_STAINED_GLASS;
            case GREEN, DARK_GREEN -> Material.LIME_STAINED_GLASS;
            case GOLD, YELLOW -> Material.YELLOW_STAINED_GLASS;
            case RED, DARK_RED -> Material.RED_STAINED_GLASS;
            case BLUE, DARK_BLUE -> Material.BLUE_STAINED_GLASS;
            default -> Material.WHITE_STAINED_GLASS;
        };
    }

    /**
     * Handle territory capture completion
     */
    public void captureTerritory(TerritoryData territory, int capturingTeamId) {
        if (territory == null) return;

        TeamData capturingTeam = plugin.getTeamManager().getTeam(capturingTeamId);
        if (capturingTeam == null) return;

        // Check if territory has active shield
        if (territory.hasActiveShield()) {
            // Reset capture progress and notify
            territory.setCapturingTeamId(0);
            territory.setCaptureProgress(0);
            MessageUtils.broadcast("&c" + capturingTeam.getColoredName() +
                " &cfailed to capture &e" + territory.getName() +
                " &c- Territory is protected by a shield!");
            plugin.getLogger().info("Capture of " + territory.getName() +
                " by team " + capturingTeam.getName() + " blocked by shield");
            return;
        }

        int previousOwner = territory.getOwnerTeamId();

        // Update territory ownership
        territory.setOwnerTeamId(capturingTeamId);
        territory.setCapturingTeamId(0);
        territory.setCaptureProgress(0);

        // Update team data
        if (previousOwner != 0) {
            TeamData previousTeam = plugin.getTeamManager().getTeam(previousOwner);
            if (previousTeam != null) {
                previousTeam.removeControlledTerritory(territory.getTerritoryId());
                plugin.getTeamManager().saveTeam(previousTeam);

                // Steal quest points
                int pointStealPercent = plugin.getConfig().getInt("capture.point_steal_percent", 25);
                int pointsStolen = (previousTeam.getQuestPoints() * pointStealPercent) / 100;
                previousTeam.subtractPoints(pointsStolen);
                capturingTeam.addPoints(pointsStolen);
            }
        }

        capturingTeam.addControlledTerritory(territory.getTerritoryId());

        // Award quest points
        int pointReward = plugin.getConfig().getInt("capture.point_reward", 100);
        capturingTeam.addPoints(pointReward);
        plugin.getTeamManager().saveTeam(capturingTeam);

        // Update beacon visual
        updateBeaconVisual(territory);

        // Save territory
        saveTerritory(territory);

        // Broadcast capture
        String message = plugin.getConfig().getString("messages.territory_captured",
                                                       "&a{team} captured {territory}!");
        message = message.replace("{team}", capturingTeam.getColoredName())
                        .replace("{territory}", territory.getName());
        MessageUtils.broadcastRaw(message);

        plugin.getLogger().info("Team " + capturingTeam.getName() + " captured " + territory.getName());
    }

    /**
     * Save territory data to database
     */
    public void saveTerritory(TerritoryData territory) {
        if (territory != null) {
            plugin.getDataManager().saveTerritory(territory);
        }
    }

    /**
     * Save all territories to database
     */
    public void saveAllTerritories() {
        for (TerritoryData territory : territories.values()) {
            saveTerritory(territory);
        }
    }

    /**
     * Get territories owned by a team
     */
    public List<TerritoryData> getTeamTerritories(int teamId) {
        List<TerritoryData> teamTerritories = new ArrayList<>();
        for (TerritoryData territory : territories.values()) {
            if (territory.getOwnerTeamId() == teamId) {
                teamTerritories.add(territory);
            }
        }
        return teamTerritories;
    }

    /**
     * Check if player is within capture radius of a beacon
     */
    public boolean isInCaptureRadius(Player player, TerritoryData territory) {
        if (player == null || territory == null) return false;

        double captureRadius = plugin.getConfig().getDouble("capture.radius", 10.0);
        double distance = territory.getDistanceToBeacon(player.getLocation());
        return distance <= captureRadius;
    }

    /**
     * Check if any enemy player is within defense radius of a beacon
     */
    public boolean hasEnemyInDefenseRadius(TerritoryData territory, int attackingTeamId) {
        if (territory == null) return false;

        double defenseRadius = plugin.getConfig().getDouble("capture.defense_radius", 50.0);
        String worldName = plugin.getConfig().getString("game.world_name", "world");
        Location beaconLoc = territory.getBeaconLocation(worldName);

        for (Player player : Bukkit.getOnlinePlayers()) {
            TeamData playerTeam = plugin.getTeamManager().getTeam(player);
            if (playerTeam == null) continue;

            // Check if player is on a different team than the attacker
            if (playerTeam.getTeamId() != attackingTeamId) {
                if (player.getWorld().getName().equals(worldName)) {
                    double distance = player.getLocation().distance(beaconLoc);
                    if (distance <= defenseRadius) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
