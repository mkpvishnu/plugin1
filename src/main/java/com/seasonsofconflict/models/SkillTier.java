package com.seasonsofconflict.models;

/**
 * Represents the 5 tiers of skills in each tree
 */
public enum SkillTier {
    TIER_1(1, 5, "Basic"),
    TIER_2(2, 10, "Advanced"),
    TIER_3(3, 15, "Expert"),
    TIER_4(4, 20, "Master"),
    ULTIMATE(5, 25, "Ultimate");

    private final int tier;
    private final int cost;
    private final String displayName;

    SkillTier(int tier, int cost, String displayName) {
        this.tier = tier;
        this.cost = cost;
        this.displayName = displayName;
    }

    public int getTier() {
        return tier;
    }

    public int getCost() {
        return cost;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the previous tier requirement
     * @return Previous tier, or null if this is TIER_1
     */
    public SkillTier getPreviousTier() {
        return switch (this) {
            case TIER_2 -> TIER_1;
            case TIER_3 -> TIER_2;
            case TIER_4 -> TIER_3;
            case ULTIMATE -> TIER_4;
            default -> null;
        };
    }

    /**
     * Check if this is an ultimate tier
     */
    public boolean isUltimate() {
        return this == ULTIMATE;
    }

    /**
     * Get tier by number (1-5)
     */
    public static SkillTier fromTier(int tier) {
        return switch (tier) {
            case 1 -> TIER_1;
            case 2 -> TIER_2;
            case 3 -> TIER_3;
            case 4 -> TIER_4;
            case 5 -> ULTIMATE;
            default -> throw new IllegalArgumentException("Invalid tier: " + tier);
        };
    }
}
