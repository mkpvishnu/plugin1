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
     * MASSIVELY INCREASED for immediate visual feedback
     */
    private void spawnSeasonalParticles(Player player, Season season) {
        Location loc = player.getLocation();

        switch (season) {
            case SPRING:
                // Spring: Cherry blossom petals / flower particles - HEAVY BLOOM
                // Multiple particle spawns for intense effect
                for (int i = 0; i < 8; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.CHERRY_LEAVES,
                        loc.clone().add(
                            (Math.random() - 0.5) * 10,  // Wider X range
                            3 + Math.random() * 4,        // Higher above player
                            (Math.random() - 0.5) * 10    // Wider Z range
                        ),
                        3,     // Count (3x more per spawn)
                        0.8,   // Larger offset X
                        0.2,   // Slight vertical spread
                        0.8,   // Larger offset Z
                        0.03   // Slightly faster fall
                    );
                }
                // Add occasional flower particles
                if (Math.random() < 0.4) {
                    loc.getWorld().spawnParticle(
                        Particle.FALLING_SPORE_BLOSSOM,
                        loc.clone().add(
                            (Math.random() - 0.5) * 8,
                            3 + Math.random() * 3,
                            (Math.random() - 0.5) * 8
                        ),
                        5,
                        1, 0.5, 1,
                        0.02
                    );
                }
                break;

            case SUMMER:
                // Summer: Heat shimmer / rising heat particles - INTENSE HEAT
                // Multiple flame/smoke particles for shimmering heat effect
                for (int i = 0; i < 6; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.FLAME,
                        loc.clone().add(
                            (Math.random() - 0.5) * 8,
                            0.1,
                            (Math.random() - 0.5) * 8
                        ),
                        2,
                        0.3,
                        0.8,  // Rising upward
                        0.3,
                        0.02
                    );
                }
                // Add smoke for haze effect
                for (int i = 0; i < 4; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        loc.clone().add(
                            (Math.random() - 0.5) * 6,
                            0.1,
                            (Math.random() - 0.5) * 6
                        ),
                        1,
                        0.2, 1, 0.2,
                        0.01
                    );
                }
                // Occasional lava drips for extreme heat
                if (Math.random() < 0.3) {
                    loc.getWorld().spawnParticle(
                        Particle.DRIPPING_LAVA,
                        loc.clone().add(
                            (Math.random() - 0.5) * 5,
                            4,
                            (Math.random() - 0.5) * 5
                        ),
                        3,
                        0.5, 0, 0.5,
                        0
                    );
                }
                break;

            case FALL:
                // Fall: Falling leaves (brown/orange) - HEAVY LEAF FALL
                // TONS of falling leaves
                for (int i = 0; i < 12; i++) {
                    // Alternate between different colored particles for variety
                    Particle particleType = Math.random() < 0.5 ?
                        Particle.FALLING_SPORE_BLOSSOM : Particle.CHERRY_LEAVES;

                    loc.getWorld().spawnParticle(
                        particleType,
                        loc.clone().add(
                            (Math.random() - 0.5) * 12,  // Much wider range
                            3 + Math.random() * 5,        // Higher up
                            (Math.random() - 0.5) * 12
                        ),
                        4,     // 4 particles per spawn
                        1,     // Wider spread
                        0.2,
                        1,
                        0.04   // Faster falling
                    );
                }
                // Add brown dust/ash particles for autumn feel
                for (int i = 0; i < 5; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.FALLING_DUST,
                        loc.clone().add(
                            (Math.random() - 0.5) * 8,
                            2 + Math.random() * 3,
                            (Math.random() - 0.5) * 8
                        ),
                        Material.BROWN_CONCRETE.createBlockData(),
                        3,
                        0.8, 0.5, 0.8,
                        0.02
                    );
                }
                break;

            case WINTER:
                // Winter: Snowflakes (even in non-snow biomes) - HEAVY BLIZZARD
                // MASSIVE snowfall
                for (int i = 0; i < 15; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.SNOWFLAKE,
                        loc.clone().add(
                            (Math.random() - 0.5) * 15,  // Very wide area
                            4 + Math.random() * 6,        // High above
                            (Math.random() - 0.5) * 15
                        ),
                        5,     // Lots of snowflakes per spawn
                        1.5,   // Wide spread
                        0.2,
                        1.5,
                        0.015  // Slow, gentle falling
                    );
                }

                // Add tons of white ash particles for blizzard effect
                for (int i = 0; i < 10; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.WHITE_ASH,
                        loc.clone().add(
                            (Math.random() - 0.5) * 10,
                            3 + Math.random() * 4,
                            (Math.random() - 0.5) * 10
                        ),
                        3,
                        1,
                        0.2,
                        1,
                        0.02
                    );
                }

                // Add cloud particles for foggy/snowy atmosphere
                for (int i = 0; i < 6; i++) {
                    loc.getWorld().spawnParticle(
                        Particle.CLOUD,
                        loc.clone().add(
                            (Math.random() - 0.5) * 8,
                            1 + Math.random() * 2,
                            (Math.random() - 0.5) * 8
                        ),
                        2,
                        1, 0.3, 1,
                        0.01
                    );
                }
                break;
        }
    }
}
