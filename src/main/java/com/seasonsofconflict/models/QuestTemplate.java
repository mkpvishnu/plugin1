package com.seasonsofconflict.models;

public enum QuestTemplate {
    // Daily Combat
    KILL_ZOMBIES("Kill 30 zombies", "zombie_kills", 30, 15, QuestCategory.COMBAT),
    KILL_SKELETONS("Kill 15 skeletons", "skeleton_kills", 15, 15, QuestCategory.COMBAT),
    KILL_CREEPERS("Kill 10 creepers", "creeper_kills", 10, 20, QuestCategory.COMBAT),
    KILL_PLAYER("Kill an enemy player", "player_kills", 1, 50, QuestCategory.COMBAT),
    SURVIVE_ENEMY_TERRITORY("Survive 30 min in enemy territory", "enemy_territory_time", 30, 40, QuestCategory.COMBAT),

    // Daily Gathering
    MINE_IRON("Mine 32 iron ore", "iron_mined", 32, 20, QuestCategory.GATHERING),
    MINE_COAL("Mine 64 coal ore", "coal_mined", 64, 10, QuestCategory.GATHERING),
    HARVEST_WHEAT("Harvest 64 wheat", "wheat_harvested", 64, 15, QuestCategory.GATHERING),
    CATCH_FISH("Catch 16 fish", "fish_caught", 16, 20, QuestCategory.GATHERING),
    CHOP_LOGS("Chop 64 logs", "logs_chopped", 64, 15, QuestCategory.GATHERING),

    // Daily Exploration
    TRAVEL_DISTANCE("Travel 1000 blocks", "distance_traveled", 1000, 15, QuestCategory.EXPLORATION),
    VISIT_TERRITORIES("Visit 3 different territories", "territories_visited", 3, 25, QuestCategory.EXPLORATION),
    FIND_STRUCTURE("Discover a structure", "structures_found", 1, 30, QuestCategory.EXPLORATION),

    // Daily Survival
    SURVIVE_DAY("Survive 24 hours without dying", "survival_time", 1440, 50, QuestCategory.SURVIVAL),
    EAT_VARIETY("Eat 8 different foods", "food_variety", 8, 20, QuestCategory.SURVIVAL),
    SURVIVE_NIGHT_OUTDOORS("Survive night outdoors", "nights_outdoors", 1, 25, QuestCategory.SURVIVAL),

    // Weekly Team
    WEEKLY_CONTROL_TERRITORY("Control home territory 7 days", "territory_control_days", 7, 300, QuestCategory.WEEKLY),
    WEEKLY_CAPTURE("Capture an enemy territory", "captures", 1, 400, QuestCategory.WEEKLY),
    WEEKLY_TEAM_KILLS("Kill 5 enemy players as team", "team_kills", 5, 350, QuestCategory.WEEKLY),
    WEEKLY_SURVIVAL("All members survive the week", "team_survival_days", 7, 500, QuestCategory.WEEKLY),
    WEEKLY_BUILD("Build 500+ block structure", "blocks_placed", 500, 200, QuestCategory.WEEKLY);

    private final String description;
    private final String progressKey;
    private final int targetAmount;
    private final int rewardPoints;
    private final QuestCategory category;

    QuestTemplate(String description, String progressKey, int targetAmount, int rewardPoints, QuestCategory category) {
        this.description = description;
        this.progressKey = progressKey;
        this.targetAmount = targetAmount;
        this.rewardPoints = rewardPoints;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public String getProgressKey() {
        return progressKey;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public QuestCategory getCategory() {
        return category;
    }
}
