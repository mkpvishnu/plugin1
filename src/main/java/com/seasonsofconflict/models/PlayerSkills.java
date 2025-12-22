package com.seasonsofconflict.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks a player's skill tree progress
 */
public class PlayerSkills {
    private final UUID playerUUID;
    private int skillPointsAvailable;
    private int skillPointsSpent;
    private int totalXPEarned;

    // Unlocked skills per tree/tier (tree -> tier -> skill name)
    private final Map<SkillTree, Map<SkillTier, String>> unlockedSkills;

    // Ultimate count (max 2)
    private int ultimateCount;
    private long lastResetTime;

    public PlayerSkills(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.skillPointsAvailable = 0;
        this.skillPointsSpent = 0;
        this.totalXPEarned = 0;
        this.unlockedSkills = new HashMap<>();
        this.ultimateCount = 0;
        this.lastResetTime = 0;

        // Initialize empty skill maps for each tree
        for (SkillTree tree : SkillTree.values()) {
            unlockedSkills.put(tree, new HashMap<>());
        }
    }

    // Getters and setters
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getSkillPointsAvailable() {
        return skillPointsAvailable;
    }

    public void setSkillPointsAvailable(int skillPointsAvailable) {
        this.skillPointsAvailable = skillPointsAvailable;
    }

    public void addSkillPoints(int points) {
        this.skillPointsAvailable += points;
    }

    public void spendSkillPoints(int points) {
        this.skillPointsAvailable -= points;
        this.skillPointsSpent += points;
    }

    public int getSkillPointsSpent() {
        return skillPointsSpent;
    }

    public void setSkillPointsSpent(int skillPointsSpent) {
        this.skillPointsSpent = skillPointsSpent;
    }

    public int getTotalXPEarned() {
        return totalXPEarned;
    }

    public void setTotalXPEarned(int totalXPEarned) {
        this.totalXPEarned = totalXPEarned;
    }

    public void addXP(int xp) {
        this.totalXPEarned += xp;
    }

    public int getUltimateCount() {
        return ultimateCount;
    }

    public void setUltimateCount(int ultimateCount) {
        this.ultimateCount = ultimateCount;
    }

    public void incrementUltimateCount() {
        this.ultimateCount++;
    }

    public void decrementUltimateCount() {
        if (this.ultimateCount > 0) {
            this.ultimateCount--;
        }
    }

    public long getLastResetTime() {
        return lastResetTime;
    }

    public void setLastResetTime(long lastResetTime) {
        this.lastResetTime = lastResetTime;
    }

    /**
     * Check if a specific skill is unlocked
     */
    public boolean hasSkill(SkillTree tree, SkillTier tier) {
        return unlockedSkills.get(tree).containsKey(tier);
    }

    /**
     * Get the unlocked skill name for a tree/tier
     */
    public String getSkill(SkillTree tree, SkillTier tier) {
        return unlockedSkills.get(tree).get(tier);
    }

    /**
     * Unlock a skill in a specific tree/tier
     */
    public void unlockSkill(SkillTree tree, SkillTier tier, String skillName) {
        unlockedSkills.get(tree).put(tier, skillName);
        if (tier.isUltimate()) {
            incrementUltimateCount();
        }
    }

    /**
     * Force unlock a skill (admin bypass - no restrictions checked)
     */
    public void forceUnlockSkill(SkillTree tree, SkillTier tier, String skillName) {
        unlockedSkills.get(tree).put(tier, skillName);
        if (tier.isUltimate()) {
            incrementUltimateCount();
        }
    }

    /**
     * Remove a skill from a tree/tier
     */
    public void removeSkill(SkillTree tree, SkillTier tier) {
        String removed = unlockedSkills.get(tree).remove(tier);
        if (removed != null && tier.isUltimate()) {
            decrementUltimateCount();
        }
    }

    /**
     * Get total points spent in a specific tree
     */
    public int getPointsSpentInTree(SkillTree tree) {
        int total = 0;
        for (SkillTier tier : unlockedSkills.get(tree).keySet()) {
            total += tier.getCost();
        }
        return total;
    }

    /**
     * Check if player has reached max ultimates (2)
     */
    public boolean hasMaxUltimates() {
        return ultimateCount >= 2;
    }

    /**
     * Check if player can afford a skill
     */
    public boolean canAfford(SkillTier tier) {
        return skillPointsAvailable >= tier.getCost();
    }

    /**
     * Check if prerequisite tier is unlocked
     */
    public boolean hasPrerequisite(SkillTree tree, SkillTier tier) {
        if (tier == SkillTier.TIER_1) {
            return true; // No prerequisite for Tier 1
        }
        SkillTier previousTier = tier.getPreviousTier();
        return previousTier != null && hasSkill(tree, previousTier);
    }

    /**
     * Reset a specific skill tree
     */
    public void resetTree(SkillTree tree) {
        Map<SkillTier, String> treeSkills = unlockedSkills.get(tree);
        int refundPoints = 0;

        // Calculate refund
        for (SkillTier tier : treeSkills.keySet()) {
            refundPoints += tier.getCost();
            if (tier.isUltimate()) {
                decrementUltimateCount();
            }
        }

        // Clear tree
        treeSkills.clear();

        // Refund points
        skillPointsAvailable += refundPoints;
        skillPointsSpent -= refundPoints;
    }

    /**
     * Reset all skill trees
     */
    public void resetAll() {
        for (SkillTree tree : SkillTree.values()) {
            resetTree(tree);
        }
        lastResetTime = System.currentTimeMillis();
    }
}
