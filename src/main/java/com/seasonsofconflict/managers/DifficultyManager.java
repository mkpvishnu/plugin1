package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.GameState;
import com.seasonsofconflict.utils.MessageUtils;
import com.seasonsofconflict.utils.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class DifficultyManager {

    private final SeasonsOfConflict plugin;

    // Scaling arrays from config
    private static final double[] MOB_DAMAGE_MULTIPLIERS = {1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0};
    private static final double[] MOB_HEALTH_MULTIPLIERS = {1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0};
    private static final double[] RESOURCE_MULTIPLIERS = {1.0, 0.9, 0.85, 0.75, 0.7, 0.6, 0.5};
    private static final int[] BORDER_SIZES = {5000, 4500, 4000, 3500, 3000, 2500, 2000};
    private static final int[] REVIVAL_COSTS = {500, 600, 750, 1000, 1500, 2500, 5000};

    public DifficultyManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    /**
     * Apply mob/resource/border scaling for the current cycle
     */
    public void applyCycleScaling(int cycle) {
        GameState gameState = plugin.getGameManager().getGameState();

        // Ensure cycle is within bounds (1-7, then apocalypse)
        int index = Math.min(cycle - 1, 6);

        if (cycle <= 7) {
            // Apply normal scaling
            double mobDamage = MOB_DAMAGE_MULTIPLIERS[index];
            double mobHealth = MOB_HEALTH_MULTIPLIERS[index];
            double resourceMultiplier = RESOURCE_MULTIPLIERS[index];
            int borderSize = BORDER_SIZES[index];
            int revivalCost = REVIVAL_COSTS[index];

            gameState.setMobDamageMultiplier(mobDamage);
            gameState.setMobHealthMultiplier(mobHealth);
            gameState.setResourceMultiplier(resourceMultiplier);
            gameState.setWorldBorderSize(borderSize);
            gameState.setRevivalCost(revivalCost);

            // Apply world border
            applyWorldBorder(borderSize);

            plugin.getLogger().info("Applied cycle " + cycle + " scaling: " +
                "Mob Damage=" + mobDamage + "x, " +
                "Mob Health=" + mobHealth + "x, " +
                "Resources=" + resourceMultiplier + "x, " +
                "Border=" + borderSize + ", " +
                "Revival Cost=" + revivalCost);
        } else {
            // Apocalypse mode
            if (!gameState.isApocalypse()) {
                startApocalypse();
            }
        }

        plugin.getGameManager().saveGameState();
    }

    /**
     * Increment cycle and apply new scaling
     */
    public void applyNewCycleScaling() {
        GameState gameState = plugin.getGameManager().getGameState();
        gameState.incrementCycle();
        int newCycle = gameState.getCurrentCycle();

        MessageUtils.broadcast("&6&l========================================");
        MessageUtils.broadcast("&c&lCYCLE " + newCycle + " BEGINS");
        MessageUtils.broadcast("&7The world grows more dangerous...");
        MessageUtils.broadcast("&6&l========================================");

        // Send dramatic title announcement
        TitleUtils.announceCycleAdvance(newCycle, "The world grows more dangerous...");

        applyCycleScaling(newCycle);

        // Check for apocalypse
        if (newCycle > 7) {
            startApocalypse();
        }
    }

    /**
     * Enable apocalypse mode and start border shrink
     */
    public void startApocalypse() {
        GameState gameState = plugin.getGameManager().getGameState();

        if (gameState.isApocalypse()) {
            return; // Already in apocalypse
        }

        gameState.setApocalypse(true);
        gameState.setMobDamageMultiplier(3.5);
        gameState.setMobHealthMultiplier(3.5);
        gameState.setResourceMultiplier(0.4);
        gameState.setRevivalCost(10000);

        plugin.getGameManager().saveGameState();

        // Dramatic announcement
        MessageUtils.broadcastRaw("&4&l========================================");
        MessageUtils.broadcastRaw("&c&l         APOCALYPSE MODE");
        MessageUtils.broadcastRaw("&7The world is ending...");
        MessageUtils.broadcastRaw("&7The border begins to shrink!");
        MessageUtils.broadcastRaw("&c&lSURVIVE AT ALL COSTS");
        MessageUtils.broadcastRaw("&4&l========================================");

        // Send dramatic title to all players
        TitleUtils.announceApocalypse();

        plugin.getLogger().warning("APOCALYPSE MODE ACTIVATED!");
    }

    /**
     * Disable apocalypse mode and restore normal cycle scaling
     */
    public void stopApocalypse() {
        GameState gameState = plugin.getGameManager().getGameState();

        if (!gameState.isApocalypse()) {
            return;
        }

        gameState.setApocalypse(false);

        // Restore scaling for current cycle (or cycle 7 if beyond)
        int currentCycle = gameState.getCurrentCycle();
        int applyCycle = Math.min(currentCycle, 7);
        applyCycleScaling(applyCycle);

        // Restore normal world state (fix stuck night/rain)
        restoreNormalWorldState();

        plugin.getGameManager().saveGameState();

        MessageUtils.broadcast("&a&lAPOCALYPSE MODE DISABLED");
        MessageUtils.broadcast("&7Normal difficulty scaling restored.");

        plugin.getLogger().info("APOCALYPSE MODE DISABLED");
    }

    /**
     * Restore normal world state after apocalypse
     * - Re-enable daylight cycle
     * - Clear storms
     * - Reset weather
     */
    private void restoreNormalWorldState() {
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("game.world_name", "world"));
        if (world == null) return;

        // Re-enable daylight cycle
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, true);

        // Clear storm
        world.setStorm(false);
        world.setThundering(false);

        // Reset weather duration to normal
        world.setWeatherDuration(0); // Will choose new random duration

        plugin.getLogger().info("Restored normal world state (daylight cycle, weather)");
    }

    /**
     * Get mob damage multiplier from game state
     */
    public double getMobDamageMultiplier() {
        return plugin.getGameManager().getGameState().getMobDamageMultiplier();
    }

    /**
     * Get mob health multiplier from game state
     */
    public double getMobHealthMultiplier() {
        return plugin.getGameManager().getGameState().getMobHealthMultiplier();
    }

    /**
     * Get resource multiplier from game state
     */
    public double getResourceMultiplier() {
        return plugin.getGameManager().getGameState().getResourceMultiplier();
    }

    /**
     * Reduce border by 200 blocks (apocalypse only)
     */
    public void shrinkBorder() {
        GameState gameState = plugin.getGameManager().getGameState();

        if (!gameState.isApocalypse()) {
            return;
        }

        int currentSize = gameState.getWorldBorderSize();
        int newSize = Math.max(currentSize - 200, 100); // Minimum 100 blocks

        gameState.setWorldBorderSize(newSize);
        applyWorldBorder(newSize);
        plugin.getGameManager().saveGameState();

        MessageUtils.broadcast("&c&lWARNING: &7The world border is shrinking to &c" + newSize + " blocks&7!");

        plugin.getLogger().warning("Border shrunk to " + newSize + " blocks");
    }

    /**
     * Apply world border to all worlds
     */
    private void applyWorldBorder(int size) {
        for (World world : Bukkit.getWorlds()) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(size);
            border.setWarningDistance(50);
            border.setWarningTime(15);
            border.setDamageAmount(0.5);
            border.setDamageBuffer(5);
        }
    }

    /**
     * Get revival cost from game state
     */
    public int getRevivalCost() {
        return plugin.getGameManager().getGameState().getRevivalCost();
    }

    /**
     * Check if apocalypse mode is active
     */
    public boolean isApocalypse() {
        return plugin.getGameManager().getGameState().isApocalypse();
    }
}
