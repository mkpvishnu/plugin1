package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerSkills;
import com.seasonsofconflict.models.SkillTier;
import com.seasonsofconflict.models.SkillTree;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages skill unlocking, validation, and persistence
 */
public class SkillManager {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, PlayerSkills> skillsCache;

    // Configuration
    private int maxSkillPoints;
    private int maxUltimateUnlocks;

    public SkillManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.skillsCache = new HashMap<>();
        loadConfiguration();
    }

    /**
     * Load configuration from config.yml
     */
    public void loadConfiguration() {
        this.maxSkillPoints = plugin.getConfig().getInt("skills.max_skill_points", 100);
        this.maxUltimateUnlocks = plugin.getConfig().getInt("skills.max_ultimate_unlocks", 2);
    }

    /**
     * Get player's skills (loads from DB if not cached)
     */
    public PlayerSkills getPlayerSkills(UUID playerUUID) {
        return skillsCache.computeIfAbsent(playerUUID, uuid -> {
            PlayerSkills skills = loadPlayerSkills(uuid);
            if (skills == null) {
                skills = new PlayerSkills(uuid);
                savePlayerSkills(skills);
            }
            return skills;
        });
    }

    /**
     * Add skill points to a player
     */
    public void addSkillPoints(UUID playerUUID, int points) {
        PlayerSkills skills = getPlayerSkills(playerUUID);
        skills.addSkillPoints(points);
        savePlayerSkills(skills);
    }

    /**
     * Attempt to unlock a skill
     * @return true if unlocked successfully, false otherwise
     */
    public UnlockResult unlockSkill(UUID playerUUID, SkillTree tree, SkillTier tier, String skillName) {
        PlayerSkills skills = getPlayerSkills(playerUUID);

        // Validation checks
        if (skills.hasSkill(tree, tier)) {
            return new UnlockResult(false, "You already have a skill in this tier!");
        }

        if (!skills.canAfford(tier)) {
            return new UnlockResult(false, "Not enough skill points! Need " + tier.getCost() + ", have " + skills.getSkillPointsAvailable());
        }

        if (!skills.hasPrerequisite(tree, tier)) {
            SkillTier prevTier = tier.getPreviousTier();
            return new UnlockResult(false, "You must unlock " + prevTier.getDisplayName() + " tier first!");
        }

        if (tier.isUltimate() && skills.hasMaxUltimates()) {
            return new UnlockResult(false, "You can only unlock " + maxUltimateUnlocks + " Ultimate abilities!");
        }

        // Check max skill points
        if (skills.getSkillPointsSpent() + tier.getCost() > maxSkillPoints) {
            return new UnlockResult(false, "Cannot exceed " + maxSkillPoints + " total skill points!");
        }

        // Unlock the skill
        skills.unlockSkill(tree, tier, skillName);
        skills.spendSkillPoints(tier.getCost());

        // Save to database
        savePlayerSkills(skills);

        return new UnlockResult(true, "Unlocked " + skillName + "!");
    }

    /**
     * Reset a specific skill tree
     * @param cost Team points cost for reset
     * @return true if reset successful
     */
    public boolean resetTree(UUID playerUUID, SkillTree tree, int cost) {
        PlayerSkills skills = getPlayerSkills(playerUUID);

        // Check if player's team can afford reset
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(playerUUID);
        if (player != null && cost > 0) {
            com.seasonsofconflict.models.TeamData team = plugin.getTeamManager().getTeam(player);
            if (team == null) {
                return false; // Player not on a team
            }
            if (team.getQuestPoints() < cost) {
                return false; // Team doesn't have enough points
            }
            team.subtractPoints(cost);
            plugin.getTeamManager().saveTeam(team);
        }

        skills.resetTree(tree);
        savePlayerSkills(skills);

        return true;
    }

    /**
     * Reset all skill trees
     */
    public boolean resetAllTrees(UUID playerUUID, int cost) {
        PlayerSkills skills = getPlayerSkills(playerUUID);

        // Check if player's team can afford reset
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(playerUUID);
        if (player != null && cost > 0) {
            com.seasonsofconflict.models.TeamData team = plugin.getTeamManager().getTeam(player);
            if (team == null) {
                return false; // Player not on a team
            }
            if (team.getQuestPoints() < cost) {
                return false; // Team doesn't have enough points
            }
            team.subtractPoints(cost);
            plugin.getTeamManager().saveTeam(team);
        }

        skills.resetAll();
        savePlayerSkills(skills);

        return true;
    }

    /**
     * Reset all skills for all players (called on cycle advance)
     */
    public void resetAllPlayersSkills() {
        for (PlayerSkills skills : skillsCache.values()) {
            skills.resetAll();
            savePlayerSkills(skills);
        }

        plugin.getLogger().info("Reset all player skills due to cycle advance");
    }

    /**
     * Load player skills from database
     */
    private PlayerSkills loadPlayerSkills(UUID playerUUID) {
        String sql = "SELECT * FROM player_skills WHERE player_uuid = ?";

        // Get shared connection - don't close it!
        Connection conn = plugin.getDataManager().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PlayerSkills skills = new PlayerSkills(playerUUID);
                skills.setSkillPointsAvailable(rs.getInt("skill_points_available"));
                skills.setSkillPointsSpent(rs.getInt("skill_points_spent"));
                skills.setTotalXPEarned(rs.getInt("total_xp_earned"));
                skills.setUltimateCount(rs.getInt("ultimate_count"));
                skills.setLastResetTime(rs.getLong("last_reset_time"));

                // Load combat tree skills
                loadTreeSkills(skills, SkillTree.COMBAT, rs, "combat");

                // Load gathering tree skills
                loadTreeSkills(skills, SkillTree.GATHERING, rs, "gathering");

                // Load survival tree skills
                loadTreeSkills(skills, SkillTree.SURVIVAL, rs, "survival");

                // Load teamwork tree skills
                loadTreeSkills(skills, SkillTree.TEAMWORK, rs, "teamwork");

                return skills;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load skills for player " + playerUUID + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Helper to load skills for a specific tree from ResultSet
     */
    private void loadTreeSkills(PlayerSkills skills, SkillTree tree, ResultSet rs, String prefix) throws SQLException {
        String tier1 = rs.getString(prefix + "_tier1");
        String tier2 = rs.getString(prefix + "_tier2");
        String tier3 = rs.getString(prefix + "_tier3");
        String tier4 = rs.getString(prefix + "_tier4");
        boolean ultimate = rs.getInt(prefix + "_ultimate") == 1;

        if (tier1 != null) skills.unlockSkill(tree, SkillTier.TIER_1, tier1);
        if (tier2 != null) skills.unlockSkill(tree, SkillTier.TIER_2, tier2);
        if (tier3 != null) skills.unlockSkill(tree, SkillTier.TIER_3, tier3);
        if (tier4 != null) skills.unlockSkill(tree, SkillTier.TIER_4, tier4);
        if (ultimate) {
            // Ultimate skill name stored in tier column
            String ultimateSkill = rs.getString(prefix + "_tier4"); // Reuse tier4 for ultimate name
            if (ultimateSkill != null) {
                skills.unlockSkill(tree, SkillTier.ULTIMATE, ultimateSkill);
            }
        }
    }

    /**
     * Save player skills to database
     */
    public void savePlayerSkills(PlayerSkills skills) {
        String sql = """
            INSERT OR REPLACE INTO player_skills
            (player_uuid, skill_points_available, skill_points_spent, total_xp_earned,
             combat_tier1, combat_tier2, combat_tier3, combat_tier4, combat_ultimate,
             gathering_tier1, gathering_tier2, gathering_tier3, gathering_tier4, gathering_ultimate,
             survival_tier1, survival_tier2, survival_tier3, survival_tier4, survival_ultimate,
             teamwork_tier1, teamwork_tier2, teamwork_tier3, teamwork_tier4, teamwork_ultimate,
             last_reset_time, ultimate_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        // Get shared connection - don't close it!
        Connection conn = plugin.getDataManager().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, skills.getPlayerUUID().toString());
            pstmt.setInt(2, skills.getSkillPointsAvailable());
            pstmt.setInt(3, skills.getSkillPointsSpent());
            pstmt.setInt(4, skills.getTotalXPEarned());

            // Combat tree
            pstmt.setString(5, skills.getSkill(SkillTree.COMBAT, SkillTier.TIER_1));
            pstmt.setString(6, skills.getSkill(SkillTree.COMBAT, SkillTier.TIER_2));
            pstmt.setString(7, skills.getSkill(SkillTree.COMBAT, SkillTier.TIER_3));
            pstmt.setString(8, skills.getSkill(SkillTree.COMBAT, SkillTier.TIER_4));
            pstmt.setInt(9, skills.hasSkill(SkillTree.COMBAT, SkillTier.ULTIMATE) ? 1 : 0);

            // Gathering tree
            pstmt.setString(10, skills.getSkill(SkillTree.GATHERING, SkillTier.TIER_1));
            pstmt.setString(11, skills.getSkill(SkillTree.GATHERING, SkillTier.TIER_2));
            pstmt.setString(12, skills.getSkill(SkillTree.GATHERING, SkillTier.TIER_3));
            pstmt.setString(13, skills.getSkill(SkillTree.GATHERING, SkillTier.TIER_4));
            pstmt.setInt(14, skills.hasSkill(SkillTree.GATHERING, SkillTier.ULTIMATE) ? 1 : 0);

            // Survival tree
            pstmt.setString(15, skills.getSkill(SkillTree.SURVIVAL, SkillTier.TIER_1));
            pstmt.setString(16, skills.getSkill(SkillTree.SURVIVAL, SkillTier.TIER_2));
            pstmt.setString(17, skills.getSkill(SkillTree.SURVIVAL, SkillTier.TIER_3));
            pstmt.setString(18, skills.getSkill(SkillTree.SURVIVAL, SkillTier.TIER_4));
            pstmt.setInt(19, skills.hasSkill(SkillTree.SURVIVAL, SkillTier.ULTIMATE) ? 1 : 0);

            // Teamwork tree
            pstmt.setString(20, skills.getSkill(SkillTree.TEAMWORK, SkillTier.TIER_1));
            pstmt.setString(21, skills.getSkill(SkillTree.TEAMWORK, SkillTier.TIER_2));
            pstmt.setString(22, skills.getSkill(SkillTree.TEAMWORK, SkillTier.TIER_3));
            pstmt.setString(23, skills.getSkill(SkillTree.TEAMWORK, SkillTier.TIER_4));
            pstmt.setInt(24, skills.hasSkill(SkillTree.TEAMWORK, SkillTier.ULTIMATE) ? 1 : 0);

            // Metadata
            pstmt.setLong(25, skills.getLastResetTime());
            pstmt.setInt(26, skills.getUltimateCount());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save skills for player " + skills.getPlayerUUID() + ": " + e.getMessage());
        }
    }

    /**
     * Save all cached player skills
     */
    public void saveAll() {
        for (PlayerSkills skills : skillsCache.values()) {
            savePlayerSkills(skills);
        }
    }

    /**
     * Result of an unlock attempt
     */
    public static class UnlockResult {
        private final boolean success;
        private final String message;

        public UnlockResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
