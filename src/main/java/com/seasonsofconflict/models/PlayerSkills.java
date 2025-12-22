package com.seasonsofconflict.models;

import java.util.*;

/**
 * Tracks a player's skill tree progress
 */
public class PlayerSkills {
    private final UUID playerUUID;
    private int skillPointsAvailable;
    private int skillPointsSpent;
    private int totalXPEarned;

    // Unlocked skills per tree/tier (tree -> tier -> list of skill names)
    // Changed to support multiple skills per tier with dynamic pricing
    private final Map<SkillTree, Map<SkillTier, List<String>>> unlockedSkills;

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

        // Initialize empty skill lists for each tree/tier
        for (SkillTree tree : SkillTree.values()) {
            Map<SkillTier, List<String>> tierMap = new HashMap<>();
            for (SkillTier tier : SkillTier.values()) {
                tierMap.put(tier, new ArrayList<>());
            }
            unlockedSkills.put(tree, tierMap);
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
     * Check if ANY skill is unlocked in a tier
     */
    public boolean hasSkill(SkillTree tree, SkillTier tier) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        return skills != null && !skills.isEmpty();
    }

    /**
     * Check if a SPECIFIC skill is unlocked
     */
    public boolean hasSpecificSkill(SkillTree tree, SkillTier tier, String skillName) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        return skills != null && skills.contains(skillName);
    }

    /**
     * Get the first unlocked skill name for a tree/tier (for backwards compatibility)
     */
    public String getSkill(SkillTree tree, SkillTier tier) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        return (skills != null && !skills.isEmpty()) ? skills.get(0) : null;
    }

    /**
     * Get ALL unlocked skills in a tier
     */
    public List<String> getSkillsInTier(SkillTree tree, SkillTier tier) {
        return new ArrayList<>(unlockedSkills.get(tree).get(tier));
    }

    /**
     * Get count of skills unlocked in a tier (for dynamic pricing)
     */
    public int getSkillCountInTier(SkillTree tree, SkillTier tier) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        return skills != null ? skills.size() : 0;
    }

    /**
     * Calculate dynamic cost for next skill in tier
     * Formula: baseCost * (2^count)
     * Examples: 1st = 5, 2nd = 10, 3rd = 20
     */
    public int calculateDynamicCost(SkillTree tree, SkillTier tier) {
        int count = getSkillCountInTier(tree, tier);
        int baseCost = tier.getCost();
        return baseCost * (int)Math.pow(2, count);
    }

    /**
     * Unlock a skill in a specific tree/tier (adds to list)
     */
    public void unlockSkill(SkillTree tree, SkillTier tier, String skillName) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        if (!skills.contains(skillName)) {
            skills.add(skillName);
            if (tier.isUltimate()) {
                incrementUltimateCount();
            }
        }
    }

    /**
     * Force unlock a skill (admin bypass - no restrictions checked)
     */
    public void forceUnlockSkill(SkillTree tree, SkillTier tier, String skillName) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        if (!skills.contains(skillName)) {
            skills.add(skillName);
            if (tier.isUltimate()) {
                incrementUltimateCount();
            }
        }
    }

    /**
     * Remove a specific skill from a tree/tier
     */
    public void removeSkill(SkillTree tree, SkillTier tier, String skillName) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        if (skills.remove(skillName) && tier.isUltimate()) {
            decrementUltimateCount();
        }
    }

    /**
     * Remove ALL skills from a tier
     */
    public void removeAllSkillsInTier(SkillTree tree, SkillTier tier) {
        List<String> skills = unlockedSkills.get(tree).get(tier);
        int count = skills.size();
        skills.clear();
        if (tier.isUltimate()) {
            ultimateCount -= count;
            if (ultimateCount < 0) ultimateCount = 0;
        }
    }

    /**
     * Get total points spent in a specific tree (including dynamic costs)
     */
    public int getPointsSpentInTree(SkillTree tree) {
        int total = 0;
        for (Map.Entry<SkillTier, List<String>> entry : unlockedSkills.get(tree).entrySet()) {
            SkillTier tier = entry.getKey();
            List<String> skills = entry.getValue();
            int baseCost = tier.getCost();

            // Calculate cost for each skill with exponential pricing
            for (int i = 0; i < skills.size(); i++) {
                total += baseCost * (int)Math.pow(2, i);
            }
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
     * Check if player can afford a skill with dynamic pricing
     */
    public boolean canAfford(SkillTree tree, SkillTier tier) {
        int dynamicCost = calculateDynamicCost(tree, tier);
        return skillPointsAvailable >= dynamicCost;
    }

    /**
     * Check if player can afford a skill (legacy method using base cost)
     * @deprecated Use canAfford(SkillTree, SkillTier) instead for dynamic pricing
     */
    @Deprecated
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
     * Reset a specific skill tree (refunds with dynamic pricing)
     */
    public void resetTree(SkillTree tree) {
        Map<SkillTier, List<String>> treeSkills = unlockedSkills.get(tree);
        int refundPoints = 0;

        // Calculate refund including dynamic costs
        for (Map.Entry<SkillTier, List<String>> entry : treeSkills.entrySet()) {
            SkillTier tier = entry.getKey();
            List<String> skills = entry.getValue();
            int baseCost = tier.getCost();

            // Refund each skill with exponential pricing
            for (int i = 0; i < skills.size(); i++) {
                refundPoints += baseCost * (int)Math.pow(2, i);
                if (tier.isUltimate()) {
                    decrementUltimateCount();
                }
            }

            // Clear skills in this tier
            skills.clear();
        }

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
