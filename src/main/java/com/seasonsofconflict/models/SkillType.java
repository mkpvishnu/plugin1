package com.seasonsofconflict.models;

/**
 * Defines the type of skill effect
 */
public enum SkillType {
    PASSIVE("Passive", "Always active"),
    ACTIVE("Active", "Activated on use"),
    PASSIVE_ACTIVE("Passive + Active", "Has both passive and active effects");

    private final String displayName;
    private final String description;

    SkillType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasActiveComponent() {
        return this == ACTIVE || this == PASSIVE_ACTIVE;
    }

    public boolean hasPassiveComponent() {
        return this == PASSIVE || this == PASSIVE_ACTIVE;
    }
}
