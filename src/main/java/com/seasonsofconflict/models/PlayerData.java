package com.seasonsofconflict.models;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private String name;
    private int teamId;
    private boolean isAlive;
    private int revivalsUsed;

    // Daily tracking (resets at midnight)
    private int dailyQuestsCompleted;
    private int dailyMobKills;
    private int dailyOresMined;

    // Stats
    private int totalKills;
    private int totalDeaths;
    private int bounty;
    private int killStreak;

    // Cooldowns
    private final Map<UUID, Long> killCooldowns; // UUID -> timestamp
    private long lastCombatTime;

    // Quests
    private final List<PlayerQuest> activeQuests;

    // Territory tracking
    private int currentTerritoryId;
    private long lastTerritoryEnterTime;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.teamId = 0;
        this.isAlive = true;
        this.revivalsUsed = 0;
        this.dailyQuestsCompleted = 0;
        this.dailyMobKills = 0;
        this.dailyOresMined = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.bounty = 0;
        this.killStreak = 0;
        this.killCooldowns = new HashMap<>();
        this.lastCombatTime = 0;
        this.activeQuests = new ArrayList<>();
        this.currentTerritoryId = 0;
        this.lastTerritoryEnterTime = 0;
    }

    // Getters and Setters
    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getRevivalsUsed() {
        return revivalsUsed;
    }

    public void setRevivalsUsed(int revivalsUsed) {
        this.revivalsUsed = revivalsUsed;
    }

    public void incrementRevivalsUsed() {
        this.revivalsUsed++;
    }

    public int getDailyQuestsCompleted() {
        return dailyQuestsCompleted;
    }

    public void setDailyQuestsCompleted(int dailyQuestsCompleted) {
        this.dailyQuestsCompleted = dailyQuestsCompleted;
    }

    public void incrementDailyQuestsCompleted() {
        this.dailyQuestsCompleted++;
    }

    public int getDailyMobKills() {
        return dailyMobKills;
    }

    public void setDailyMobKills(int dailyMobKills) {
        this.dailyMobKills = dailyMobKills;
    }

    public void incrementDailyMobKills() {
        this.dailyMobKills++;
    }

    public int getDailyOresMined() {
        return dailyOresMined;
    }

    public void setDailyOresMined(int dailyOresMined) {
        this.dailyOresMined = dailyOresMined;
    }

    public void incrementDailyOresMined() {
        this.dailyOresMined++;
    }

    public void resetDailyStats() {
        this.dailyQuestsCompleted = 0;
        this.dailyMobKills = 0;
        this.dailyOresMined = 0;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public void incrementTotalKills() {
        this.totalKills++;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public void incrementTotalDeaths() {
        this.totalDeaths++;
    }

    public int getBounty() {
        return bounty;
    }

    public void setBounty(int bounty) {
        this.bounty = bounty;
    }

    public int getKillStreak() {
        return killStreak;
    }

    public void setKillStreak(int killStreak) {
        this.killStreak = killStreak;
    }

    public void incrementKillStreak() {
        this.killStreak++;
    }

    public boolean hasKillCooldown(UUID victimUUID) {
        Long cooldownExpiry = killCooldowns.get(victimUUID);
        if (cooldownExpiry == null) return false;

        long now = System.currentTimeMillis();
        if (now > cooldownExpiry) {
            killCooldowns.remove(victimUUID);
            return false;
        }
        return true;
    }

    public void addKillCooldown(UUID victimUUID) {
        long expiry = System.currentTimeMillis() + (12 * 60 * 60 * 1000L); // 12 hours
        killCooldowns.put(victimUUID, expiry);
    }

    public long getLastCombatTime() {
        return lastCombatTime;
    }

    public void setLastCombatTime(long lastCombatTime) {
        this.lastCombatTime = lastCombatTime;
    }

    public boolean isInCombat() {
        return System.currentTimeMillis() - lastCombatTime < 10000; // 10 seconds
    }

    public List<PlayerQuest> getActiveQuests() {
        return activeQuests;
    }

    public void addQuest(PlayerQuest quest) {
        this.activeQuests.add(quest);
    }

    public void clearDailyQuests() {
        this.activeQuests.clear();
    }

    public int getCurrentTerritoryId() {
        return currentTerritoryId;
    }

    public void setCurrentTerritoryId(int currentTerritoryId) {
        this.currentTerritoryId = currentTerritoryId;
    }

    public long getLastTerritoryEnterTime() {
        return lastTerritoryEnterTime;
    }

    public void setLastTerritoryEnterTime(long lastTerritoryEnterTime) {
        this.lastTerritoryEnterTime = lastTerritoryEnterTime;
    }
}
