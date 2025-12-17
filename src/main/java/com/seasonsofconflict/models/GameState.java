package com.seasonsofconflict.models;

import java.time.LocalDate;

public class GameState {
    private Season currentSeason;
    private int currentCycle;
    private LocalDate seasonStartDate;

    private int worldBorderSize;
    private boolean isApocalypse;

    // Calculated values based on cycle
    private double mobDamageMultiplier;
    private double mobHealthMultiplier;
    private double resourceMultiplier;
    private int revivalCost;

    public GameState() {
        this.currentSeason = Season.SPRING;
        this.currentCycle = 1;
        this.seasonStartDate = LocalDate.now();
        this.worldBorderSize = 5000;
        this.isApocalypse = false;
        this.mobDamageMultiplier = 1.0;
        this.mobHealthMultiplier = 1.0;
        this.resourceMultiplier = 1.0;
        this.revivalCost = 500;
    }

    // Getters and Setters
    public Season getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(Season currentSeason) {
        this.currentSeason = currentSeason;
    }

    public int getCurrentCycle() {
        return currentCycle;
    }

    public void setCurrentCycle(int currentCycle) {
        this.currentCycle = currentCycle;
    }

    public void incrementCycle() {
        this.currentCycle++;
    }

    public LocalDate getSeasonStartDate() {
        return seasonStartDate;
    }

    public void setSeasonStartDate(LocalDate seasonStartDate) {
        this.seasonStartDate = seasonStartDate;
    }

    public int getWorldBorderSize() {
        return worldBorderSize;
    }

    public void setWorldBorderSize(int worldBorderSize) {
        this.worldBorderSize = worldBorderSize;
    }

    public boolean isApocalypse() {
        return isApocalypse;
    }

    public void setApocalypse(boolean apocalypse) {
        isApocalypse = apocalypse;
    }

    public double getMobDamageMultiplier() {
        return mobDamageMultiplier;
    }

    public void setMobDamageMultiplier(double mobDamageMultiplier) {
        this.mobDamageMultiplier = mobDamageMultiplier;
    }

    public double getMobHealthMultiplier() {
        return mobHealthMultiplier;
    }

    public void setMobHealthMultiplier(double mobHealthMultiplier) {
        this.mobHealthMultiplier = mobHealthMultiplier;
    }

    public double getResourceMultiplier() {
        return resourceMultiplier;
    }

    public void setResourceMultiplier(double resourceMultiplier) {
        this.resourceMultiplier = resourceMultiplier;
    }

    public int getRevivalCost() {
        return revivalCost;
    }

    public void setRevivalCost(int revivalCost) {
        this.revivalCost = revivalCost;
    }
}
