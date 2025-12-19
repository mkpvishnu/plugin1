package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Spawns seasonal particle effects around players for immersion
 * Runs every 3 seconds (60 ticks)
 */
public class SeasonalParticlesTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public SeasonalParticlesTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Season currentSeason = plugin.getGameManager().getGameState().getCurrentSeason();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.getGameManager().getPlayerData(player);
            if (!data.isAlive()) continue;

            Location loc = player.getLocation();

            // Only spawn particles outdoors (can see sky)
            if (loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY()) {
                spawnSeasonalParticles(player, currentSeason);
            }
        }
    }

    /**
     * Spawn particles based on current season
     */
    private void spawnSeasonalParticles(Player player, Season season) {
        Location loc = player.getLocation();

        switch (season) {
            case SPRING:
                // Spring: Cherry blossom petals / flower particles
                // Sparse particle effect - gentle falling petals
                if (Math.random() < 0.3) {
                    loc.getWorld().spawnParticle(
                        Particle.CHERRY_LEAVES,
                        loc.clone().add(
                            (Math.random() - 0.5) * 6,  // Random X offset
                            2 + Math.random() * 2,       // Above player
                            (Math.random() - 0.5) * 6    // Random Z offset
                        ),
                        1,     // Count
                        0.5,   // Offset X
                        0,     // Offset Y
                        0.5,   // Offset Z
                        0.02   // Speed (slow fall)
                    );
                }
                break;

            case SUMMER:
                // Summer: Heat shimmer / rising heat particles
                if (Math.random() < 0.2) {
                    loc.getWorld().spawnParticle(
                        Particle.FLAME,
                        loc.clone().add(
                            (Math.random() - 0.5) * 4,
                            0.1,
                            (Math.random() - 0.5) * 4
                        ),
                        1,
                        0.1,
                        0.5,
                        0.1,
                        0.01
                    );
                }
                break;

            case FALL:
                // Fall: Falling leaves (brown/orange)
                if (Math.random() < 0.4) {
                    // Alternate between different colored particles for variety
                    Particle particleType = Math.random() < 0.5 ?
                        Particle.FALLING_SPORE_BLOSSOM : Particle.CHERRY_LEAVES;

                    loc.getWorld().spawnParticle(
                        particleType,
                        loc.clone().add(
                            (Math.random() - 0.5) * 6,
                            2 + Math.random() * 3,
                            (Math.random() - 0.5) * 6
                        ),
                        2,     // More particles
                        0.5,
                        0,
                        0.5,
                        0.03
                    );
                }
                break;

            case WINTER:
                // Winter: Snowflakes (even in non-snow biomes)
                if (Math.random() < 0.5) {
                    loc.getWorld().spawnParticle(
                        Particle.SNOWFLAKE,
                        loc.clone().add(
                            (Math.random() - 0.5) * 8,
                            3 + Math.random() * 3,
                            (Math.random() - 0.5) * 8
                        ),
                        3,     // More snowflakes
                        1,
                        0,
                        1,
                        0.01   // Slow falling
                    );

                    // Add occasional white ash particles for blizzard effect
                    if (Math.random() < 0.3) {
                        loc.getWorld().spawnParticle(
                            Particle.WHITE_ASH,
                            loc.clone().add(
                                (Math.random() - 0.5) * 6,
                                2 + Math.random() * 2,
                                (Math.random() - 0.5) * 6
                            ),
                            1,
                            0.5,
                            0,
                            0.5,
                            0.02
                        );
                    }
                }
                break;
        }
    }
}
