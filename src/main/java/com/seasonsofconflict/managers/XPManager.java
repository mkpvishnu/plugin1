package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player XP tracking and conversion to skill points
 */
public class XPManager {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, PlayerXPData> xpCache;

    // Configuration values
    private int xpPerSkillPoint;

    public XPManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.xpCache = new HashMap<>();
        loadConfiguration();
    }

    /**
     * Load XP configuration from config.yml
     */
    private void loadConfiguration() {
        this.xpPerSkillPoint = plugin.getConfig().getInt("skills.xp_per_skill_point", 500);
    }

    /**
     * Get player's XP data (loads from DB if not cached)
     */
    public PlayerXPData getPlayerXP(UUID playerUUID) {
        return xpCache.computeIfAbsent(playerUUID, uuid -> {
            PlayerXPData data = loadPlayerXP(uuid);
            if (data == null) {
                data = new PlayerXPData(uuid);
                savePlayerXP(data);
            }
            return data;
        });
    }

    /**
     * Add XP to a player and convert to skill points if threshold reached
     * @param playerUUID Player UUID
     * @param amount XP amount to add
     * @return Number of skill points earned (0 if none)
     */
    public int addXP(UUID playerUUID, int amount) {
        PlayerXPData xpData = getPlayerXP(playerUUID);
        xpData.addTotalXP(amount);
        xpData.addCurrentXP(amount);

        // Calculate skill points earned
        int skillPointsEarned = 0;
        while (xpData.getCurrentXP() >= xpPerSkillPoint) {
            xpData.addCurrentXP(-xpPerSkillPoint);
            skillPointsEarned++;
        }

        // If skill points were earned, update player skills
        if (skillPointsEarned > 0) {
            plugin.getSkillManager().addSkillPoints(playerUUID, skillPointsEarned);
        }

        // Save to database
        savePlayerXP(xpData);

        return skillPointsEarned;
    }

    /**
     * Add XP to a player with multipliers applied
     */
    public int addXPWithMultipliers(Player player, int baseAmount) {
        PlayerXPData xpData = getPlayerXP(player.getUniqueId());

        // Apply XP multiplier
        double multiplier = xpData.getXPMultiplier();

        // Check for territory bonus
        if (plugin.getConfig().getBoolean("skills.enabled", true)) {
            double territoryBonus = plugin.getTerritoryManager().getTerritoryBonus(player,
                com.seasonsofconflict.models.BonusType.XP);
            multiplier *= territoryBonus; // Apply territory multiplier
        }

        int finalAmount = (int) (baseAmount * multiplier);
        return addXP(player.getUniqueId(), finalAmount);
    }

    /**
     * Get XP required for next skill point
     */
    public int getXPForNextSkillPoint(UUID playerUUID) {
        PlayerXPData xpData = getPlayerXP(playerUUID);
        return xpPerSkillPoint - xpData.getCurrentXP();
    }

    /**
     * Get progress percentage toward next skill point (0-100)
     */
    public double getProgressPercent(UUID playerUUID) {
        PlayerXPData xpData = getPlayerXP(playerUUID);
        return (double) xpData.getCurrentXP() / xpPerSkillPoint * 100.0;
    }

    /**
     * Set XP multiplier for a player (from skills/events)
     */
    public void setXPMultiplier(UUID playerUUID, double multiplier) {
        PlayerXPData xpData = getPlayerXP(playerUUID);
        xpData.setXPMultiplier(multiplier);
        savePlayerXP(xpData);
    }

    /**
     * Reset player's current XP (keeps total XP)
     */
    public void resetCurrentXP(UUID playerUUID) {
        PlayerXPData xpData = getPlayerXP(playerUUID);
        xpData.setCurrentXP(0);
        savePlayerXP(xpData);
    }

    /**
     * Load player XP data from database
     */
    private PlayerXPData loadPlayerXP(UUID playerUUID) {
        String sql = "SELECT * FROM player_xp WHERE player_uuid = ?";

        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PlayerXPData data = new PlayerXPData(playerUUID);
                data.setTotalXP(rs.getInt("total_xp"));
                data.setCurrentXP(rs.getInt("current_xp"));
                data.setXPMultiplier(rs.getDouble("xp_multiplier"));
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load XP for player " + playerUUID + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Save player XP data to database
     */
    public void savePlayerXP(PlayerXPData xpData) {
        String sql = """
            INSERT OR REPLACE INTO player_xp
            (player_uuid, total_xp, current_xp, xp_multiplier)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, xpData.getPlayerUUID().toString());
            pstmt.setInt(2, xpData.getTotalXP());
            pstmt.setInt(3, xpData.getCurrentXP());
            pstmt.setDouble(4, xpData.getXPMultiplier());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save XP for player " + xpData.getPlayerUUID() + ": " + e.getMessage());
        }
    }

    /**
     * Save all cached XP data
     */
    public void saveAll() {
        for (PlayerXPData xpData : xpCache.values()) {
            savePlayerXP(xpData);
        }
    }

    /**
     * Inner class to track player XP data
     */
    public static class PlayerXPData {
        private final UUID playerUUID;
        private int totalXP;
        private int currentXP;  // XP toward next skill point
        private double xpMultiplier;

        public PlayerXPData(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.totalXP = 0;
            this.currentXP = 0;
            this.xpMultiplier = 1.0;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public int getTotalXP() {
            return totalXP;
        }

        public void setTotalXP(int totalXP) {
            this.totalXP = totalXP;
        }

        public void addTotalXP(int xp) {
            this.totalXP += xp;
        }

        public int getCurrentXP() {
            return currentXP;
        }

        public void setCurrentXP(int currentXP) {
            this.currentXP = currentXP;
        }

        public void addCurrentXP(int xp) {
            this.currentXP += xp;
        }

        public double getXPMultiplier() {
            return xpMultiplier;
        }

        public void setXPMultiplier(double xpMultiplier) {
            this.xpMultiplier = xpMultiplier;
        }
    }
}
