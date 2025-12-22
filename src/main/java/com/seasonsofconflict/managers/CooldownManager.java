package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages skill cooldowns with database persistence
 */
public class CooldownManager {

    private final SeasonsOfConflict plugin;

    // In-memory cooldown tracking: PlayerUUID -> (SkillName -> ExpiryTimestamp)
    private final Map<UUID, Map<String, Long>> cooldowns;

    public CooldownManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
    }

    /**
     * Check if a skill is on cooldown
     */
    public boolean isOnCooldown(Player player, String skillName) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns == null || !playerCooldowns.containsKey(skillName)) {
            return false;
        }

        long expiryTime = playerCooldowns.get(skillName);
        long currentTime = System.currentTimeMillis();

        if (currentTime >= expiryTime) {
            // Cooldown expired
            playerCooldowns.remove(skillName);
            return false;
        }

        return true;
    }

    /**
     * Get remaining cooldown in seconds
     */
    public long getRemainingCooldown(Player player, String skillName) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns == null || !playerCooldowns.containsKey(skillName)) {
            return 0;
        }

        long expiryTime = playerCooldowns.get(skillName);
        long currentTime = System.currentTimeMillis();
        long remaining = expiryTime - currentTime;

        return Math.max(0, remaining / 1000); // Convert to seconds
    }

    /**
     * Set a cooldown for a skill
     * @param player Player who used the skill
     * @param skillName Skill internal name
     * @param durationSeconds Cooldown duration in seconds
     */
    public void setCooldown(Player player, String skillName, int durationSeconds) {
        UUID uuid = player.getUniqueId();
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                 .put(skillName, expiryTime);

        // Save to database
        saveCooldownToDatabase(uuid, skillName, expiryTime);
    }

    /**
     * Try to activate a skill (checks cooldown and sets new one)
     * @return true if skill can be activated, false if on cooldown
     */
    public boolean tryActivateSkill(Player player, String skillName, int cooldownSeconds) {
        if (isOnCooldown(player, skillName)) {
            long remaining = getRemainingCooldown(player, skillName);
            MessageUtils.sendMessage(player, "&c⏰ " + formatSkillName(skillName) +
                " is on cooldown! &7(" + formatTime(remaining) + " remaining)");
            return false;
        }

        setCooldown(player, skillName, cooldownSeconds);
        return true;
    }

    /**
     * Clear all cooldowns for a player
     */
    public void clearCooldowns(UUID playerUUID) {
        cooldowns.remove(playerUUID);
        clearCooldownsFromDatabase(playerUUID);
    }

    /**
     * Clear a specific cooldown
     */
    public void clearCooldown(UUID playerUUID, String skillName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns != null) {
            playerCooldowns.remove(skillName);
        }
        removeCooldownFromDatabase(playerUUID, skillName);
    }

    /**
     * Load all active cooldowns for a player from database
     */
    public void loadCooldowns(UUID playerUUID) {
        String query = "SELECT skill_name, cooldown_end FROM skill_cooldowns WHERE player_uuid = ? AND cooldown_end > ?";

        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setLong(2, System.currentTimeMillis());

            ResultSet rs = stmt.executeQuery();
            Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();

            while (rs.next()) {
                String skillName = rs.getString("skill_name");
                long expiryTime = rs.getLong("cooldown_end");
                playerCooldowns.put(skillName, expiryTime);
            }

            if (!playerCooldowns.isEmpty()) {
                cooldowns.put(playerUUID, playerCooldowns);
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load cooldowns for player " + playerUUID + ": " + e.getMessage());
        }
    }

    /**
     * Save a cooldown to database
     */
    private void saveCooldownToDatabase(UUID playerUUID, String skillName, long expiryTime) {
        String query = "INSERT OR REPLACE INTO skill_cooldowns (player_uuid, skill_name, cooldown_end) VALUES (?, ?, ?)";

        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, skillName);
            stmt.setLong(3, expiryTime);
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save cooldown: " + e.getMessage());
        }
    }

    /**
     * Clear all cooldowns from database for a player
     */
    private void clearCooldownsFromDatabase(UUID playerUUID) {
        String query = "DELETE FROM skill_cooldowns WHERE player_uuid = ?";

        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to clear cooldowns: " + e.getMessage());
        }
    }

    /**
     * Remove a specific cooldown from database
     */
    private void removeCooldownFromDatabase(UUID playerUUID, String skillName) {
        String query = "DELETE FROM skill_cooldowns WHERE player_uuid = ? AND skill_name = ?";

        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, skillName);
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to remove cooldown: " + e.getMessage());
        }
    }

    /**
     * Clean up expired cooldowns (run periodically)
     */
    public void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();

        cooldowns.forEach((uuid, playerCooldowns) -> {
            playerCooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        });

        // Clean database
        String query = "DELETE FROM skill_cooldowns WHERE cooldown_end <= ?";
        try (Connection conn = plugin.getDataManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, currentTime);
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to cleanup cooldowns: " + e.getMessage());
        }
    }

    /**
     * Format skill name for display (snake_case -> Title Case)
     */
    private String formatSkillName(String internalName) {
        String[] parts = internalName.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(Character.toUpperCase(part.charAt(0)))
                     .append(part.substring(1).toLowerCase());
        }

        return formatted.toString();
    }

    /**
     * Format time remaining (seconds -> human readable)
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Get all active cooldowns for a player (for display)
     */
    public Map<String, Long> getPlayerCooldowns(UUID playerUUID) {
        return cooldowns.getOrDefault(playerUUID, new ConcurrentHashMap<>());
    }
}
