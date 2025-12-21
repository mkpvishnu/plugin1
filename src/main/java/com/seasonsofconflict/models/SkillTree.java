package com.seasonsofconflict.models;

/**
 * Represents the 4 skill trees available to players
 */
public enum SkillTree {
    COMBAT("Combat", "🗡️", "High damage, aggressive playstyle, risk/reward combat"),
    GATHERING("Gathering", "⛏️", "Resource efficiency, wealth generation, exploration"),
    SURVIVAL("Survival", "🛡️", "Tanking, sustain, environmental resistance"),
    TEAMWORK("Teamwork", "👥", "Team buffs, support abilities, coordination");

    private final String displayName;
    private final String icon;
    private final String description;

    SkillTree(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedName() {
        return icon + " " + displayName;
    }
}
