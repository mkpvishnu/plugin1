package com.seasonsofconflict.models;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamData {
    private final int teamId;
    private String name;
    private ChatColor color;
    private int questPoints;
    private int homeTerritory;
    private final List<Integer> controlledTerritories;
    private boolean isEliminated;

    // Weekly quest tracking
    private int weeklyQuestId;
    private int weeklyQuestProgress;

    // Cooldowns
    private long lastShieldTime;
    private long activeShieldExpiry;

    public TeamData(int teamId, String name, ChatColor color, int homeTerritory) {
        this.teamId = teamId;
        this.name = name;
        this.color = color;
        this.questPoints = 0;
        this.homeTerritory = homeTerritory;
        this.controlledTerritories = new ArrayList<>();
        this.controlledTerritories.add(homeTerritory);
        this.isEliminated = false;
        this.weeklyQuestId = 0;
        this.weeklyQuestProgress = 0;
        this.lastShieldTime = 0;
        this.activeShieldExpiry = 0;
    }

    // Getters and Setters
    public int getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public String getColoredName() {
        return color + name + ChatColor.RESET;
    }

    public int getQuestPoints() {
        return questPoints;
    }

    public void setQuestPoints(int questPoints) {
        this.questPoints = Math.max(0, questPoints);
    }

    public void addPoints(int points) {
        this.questPoints += points;
    }

    public void subtractPoints(int points) {
        this.questPoints = Math.max(0, this.questPoints - points);
    }

    public int getHomeTerritory() {
        return homeTerritory;
    }

    public void setHomeTerritory(int homeTerritory) {
        this.homeTerritory = homeTerritory;
    }

    public List<Integer> getControlledTerritories() {
        return controlledTerritories;
    }

    public void addControlledTerritory(int territoryId) {
        if (!controlledTerritories.contains(territoryId)) {
            controlledTerritories.add(territoryId);
        }
    }

    public void removeControlledTerritory(int territoryId) {
        controlledTerritories.remove(Integer.valueOf(territoryId));
    }

    public boolean isEliminated() {
        return isEliminated;
    }

    public void setEliminated(boolean eliminated) {
        isEliminated = eliminated;
    }

    public int getWeeklyQuestId() {
        return weeklyQuestId;
    }

    public void setWeeklyQuestId(int weeklyQuestId) {
        this.weeklyQuestId = weeklyQuestId;
    }

    public int getWeeklyQuestProgress() {
        return weeklyQuestProgress;
    }

    public void setWeeklyQuestProgress(int weeklyQuestProgress) {
        this.weeklyQuestProgress = weeklyQuestProgress;
    }

    public void incrementWeeklyQuestProgress() {
        this.weeklyQuestProgress++;
    }

    public long getLastShieldTime() {
        return lastShieldTime;
    }

    public void setLastShieldTime(long lastShieldTime) {
        this.lastShieldTime = lastShieldTime;
    }

    public long getActiveShieldExpiry() {
        return activeShieldExpiry;
    }

    public void setActiveShieldExpiry(long activeShieldExpiry) {
        this.activeShieldExpiry = activeShieldExpiry;
    }

    public boolean hasActiveShield() {
        return System.currentTimeMillis() < activeShieldExpiry;
    }

    public long getShieldTimeRemaining() {
        if (!hasActiveShield()) return 0;
        return (activeShieldExpiry - System.currentTimeMillis()) / 1000; // seconds
    }

    public boolean canUseShield() {
        long cooldown = 72 * 60 * 60 * 1000L; // 72 hours
        return System.currentTimeMillis() - lastShieldTime >= cooldown;
    }
}
