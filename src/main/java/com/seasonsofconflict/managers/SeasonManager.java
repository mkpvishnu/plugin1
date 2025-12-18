package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.GameState;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SeasonManager {

    private final SeasonsOfConflict plugin;

    public SeasonManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    /**
     * Apply all season effects based on the given season
     */
    public void applySeason(Season season) {
        GameState gameState = plugin.getGameManager().getGameState();
        gameState.setCurrentSeason(season);
        gameState.setSeasonStartDate(LocalDate.now());
        plugin.getGameManager().saveGameState();

        // Broadcast season change
        String seasonName = season.name();
        MessageUtils.broadcast("&6&l========================================");
        MessageUtils.broadcast("&e&lSEASON CHANGE: " + seasonName);
        MessageUtils.broadcast("&7" + getSeasonalEffect(season));
        MessageUtils.broadcast("&6&l========================================");

        plugin.getLogger().info("Season changed to " + season.name());
    }

    /**
     * Check if 30 days have passed and advance season if needed
     */
    public void checkSeasonTransition() {
        GameState gameState = plugin.getGameManager().getGameState();
        LocalDate seasonStart = gameState.getSeasonStartDate();
        LocalDate now = LocalDate.now();

        long daysPassed = ChronoUnit.DAYS.between(seasonStart, now);

        if (daysPassed >= 30) {
            Season currentSeason = gameState.getCurrentSeason();
            Season nextSeason = currentSeason.getNext();

            applySeason(nextSeason);

            // If we're transitioning from WINTER to SPRING, increment cycle
            if (currentSeason == Season.WINTER && nextSeason == Season.SPRING) {
                plugin.getDifficultyManager().applyNewCycleScaling();
            }
        }
    }

    /**
     * Get description of current season effects
     */
    public String getSeasonalEffect(Season season) {
        return switch (season) {
            case SPRING -> "Regeneration effect active for all players. Time to heal and rebuild!";
            case SUMMER -> "Mobs are stronger and more aggressive. Prepare for battle!";
            case FALL -> "Harvest bonus active. Gather resources while you can!";
            case WINTER -> "Freezing damage outdoors. Stay near heat sources to survive!";
        };
    }

    /**
     * Apply seasonal effects to players (called periodically)
     */
    public void applySeasonalEffects() {
        GameState gameState = plugin.getGameManager().getGameState();
        Season currentSeason = gameState.getCurrentSeason();
        boolean isApocalypse = gameState.isApocalypse();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getGameManager().getPlayerData(player);
            if (!data.isAlive()) continue;

            // Apocalypse mode: Apply Hunger II to all players
            if (isApocalypse) {
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.HUNGER,
                    60 * 20, // 60 seconds
                    1, // Level 2 (Hunger II)
                    false,
                    false
                ));
            }

            switch (currentSeason) {
                case SPRING:
                    // Apply regeneration effect (60 seconds duration, reapplied periodically)
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION,
                        60 * 20, // 60 seconds
                        0, // Level 1
                        false,
                        false
                    ));
                    break;

                case SUMMER:
                    // Mob boost is handled in EntityDamageListener
                    break;

                case FALL:
                    // Harvest boost is handled in BlockBreakListener
                    break;

                case WINTER:
                    // Apply Hunger I effect in Winter (unless apocalypse, which applies Hunger II)
                    if (!isApocalypse) {
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.HUNGER,
                            60 * 20, // 60 seconds
                            0, // Level 1 (Hunger I)
                            false,
                            false
                        ));
                    }
                    // Freezing damage is handled separately
                    break;
            }
        }
    }

    /**
     * Apply freezing damage to players outdoors without heat source (winter only)
     */
    public void applyFreezingDamage() {
        GameState gameState = plugin.getGameManager().getGameState();

        if (gameState.getCurrentSeason() != Season.WINTER) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getGameManager().getPlayerData(player);
            if (!data.isAlive()) continue;

            Location loc = player.getLocation();

            // Check if player is outdoors (can see sky)
            if (loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY()) {
                // Player is outdoors
                if (!isNearHeatSource(player)) {
                    // Apply freezing damage
                    player.damage(2.0); // 1 heart damage
                    MessageUtils.sendMessage(player, "&b&lFREEZING! &7Find a heat source!");

                    // Apply slowness effect
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW,
                        60, // 3 seconds
                        0,
                        false,
                        false
                    ));
                }
            }
        }
    }

    /**
     * Check if player is near a heat source (campfire, lava, furnace) within 5 blocks
     */
    public boolean isNearHeatSource(Player player) {
        Location playerLoc = player.getLocation();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = playerLoc.getWorld().getBlockAt(
                        playerLoc.getBlockX() + x,
                        playerLoc.getBlockY() + y,
                        playerLoc.getBlockZ() + z
                    );

                    Material type = block.getType();

                    // Check for heat sources
                    if (type == Material.CAMPFIRE ||
                        type == Material.SOUL_CAMPFIRE ||
                        type == Material.LAVA ||
                        type == Material.FURNACE ||
                        type == Material.BLAST_FURNACE ||
                        type == Material.SMOKER ||
                        type == Material.FIRE ||
                        type == Material.SOUL_FIRE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Get the current season multiplier for harvest
     */
    public double getHarvestMultiplier() {
        GameState gameState = plugin.getGameManager().getGameState();
        if (gameState.getCurrentSeason() == Season.FALL) {
            return 1.5; // 50% bonus in fall
        }
        return 1.0;
    }

    /**
     * Get the current season multiplier for mob damage
     */
    public double getMobSeasonMultiplier() {
        GameState gameState = plugin.getGameManager().getGameState();
        if (gameState.getCurrentSeason() == Season.SUMMER) {
            return 1.5; // 50% more damage in summer
        }
        return 1.0;
    }
}
