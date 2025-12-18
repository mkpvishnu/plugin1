package com.seasonsofconflict.models;

import org.bukkit.Location;

public class TerritoryData {
    private final int territoryId;
    private String name;

    // Boundaries (rectangular)
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;

    // Beacon
    private int beaconX;
    private int beaconY;
    private int beaconZ;

    // Ownership
    private int ownerTeamId; // 0 = neutral

    // Capture state
    private int capturingTeamId; // 0 = none
    private int captureProgress; // seconds (0-300)

    // Bonuses
    private BonusType bonusType;
    private int baseBonusPercent;

    // Shield (in-memory, not persisted to database)
    private long shieldExpiryTime; // System.currentTimeMillis() when shield expires
    private long shieldCooldownExpiry; // System.currentTimeMillis() when cooldown expires

    public TerritoryData(int territoryId, String name, int minX, int maxX, int minZ, int maxZ,
                         int beaconX, int beaconY, int beaconZ,
                         BonusType bonusType, int baseBonusPercent) {
        this.territoryId = territoryId;
        this.name = name;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.beaconX = beaconX;
        this.beaconY = beaconY;
        this.beaconZ = beaconZ;
        this.bonusType = bonusType;
        this.baseBonusPercent = baseBonusPercent;
        this.ownerTeamId = 0;
        this.capturingTeamId = 0;
        this.captureProgress = 0;
        this.shieldExpiryTime = 0;
        this.shieldCooldownExpiry = 0;
    }

    public boolean isInTerritory(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public Location getBeaconLocation(String worldName) {
        return new Location(org.bukkit.Bukkit.getWorld(worldName), beaconX, beaconY, beaconZ);
    }

    public double getDistanceToBeacon(Location location) {
        Location beacon = getBeaconLocation(location.getWorld().getName());
        return location.distance(beacon);
    }

    // Getters and Setters
    public int getTerritoryId() {
        return territoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMinZ() {
        return minZ;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    public int getBeaconX() {
        return beaconX;
    }

    public void setBeaconX(int beaconX) {
        this.beaconX = beaconX;
    }

    public int getBeaconY() {
        return beaconY;
    }

    public void setBeaconY(int beaconY) {
        this.beaconY = beaconY;
    }

    public int getBeaconZ() {
        return beaconZ;
    }

    public void setBeaconZ(int beaconZ) {
        this.beaconZ = beaconZ;
    }

    public int getOwnerTeamId() {
        return ownerTeamId;
    }

    public void setOwnerTeamId(int ownerTeamId) {
        this.ownerTeamId = ownerTeamId;
    }

    public int getCapturingTeamId() {
        return capturingTeamId;
    }

    public void setCapturingTeamId(int capturingTeamId) {
        this.capturingTeamId = capturingTeamId;
    }

    public int getCaptureProgress() {
        return captureProgress;
    }

    public void setCaptureProgress(int captureProgress) {
        this.captureProgress = Math.max(0, Math.min(300, captureProgress));
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }

    public int getBaseBonusPercent() {
        return baseBonusPercent;
    }

    public void setBaseBonusPercent(int baseBonusPercent) {
        this.baseBonusPercent = baseBonusPercent;
    }

    public boolean hasActiveShield() {
        return System.currentTimeMillis() < shieldExpiryTime;
    }

    public long getShieldExpiryTime() {
        return shieldExpiryTime;
    }

    public void setShieldExpiryTime(long shieldExpiryTime) {
        this.shieldExpiryTime = shieldExpiryTime;
    }

    public boolean isOnShieldCooldown() {
        return System.currentTimeMillis() < shieldCooldownExpiry;
    }

    public long getShieldCooldownExpiry() {
        return shieldCooldownExpiry;
    }

    public void setShieldCooldownExpiry(long shieldCooldownExpiry) {
        this.shieldCooldownExpiry = shieldCooldownExpiry;
    }

    public void activateShield(int durationHours, int cooldownHours) {
        long now = System.currentTimeMillis();
        this.shieldExpiryTime = now + (durationHours * 60L * 60L * 1000L);
        this.shieldCooldownExpiry = now + (cooldownHours * 60L * 60L * 1000L);
    }
}
