package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Handles apocalypse world effects:
 * - Water turning to lava
 * - Random fire spawning
 * - Ash particles
 * - Dark sky
 * - End crystals
 *
 * Runs every 30 seconds during apocalypse mode
 */
public class ApocalypseEffectsTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;
    private final Random random;

    public ApocalypseEffectsTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @Override
    public void run() {
        // Only run during apocalypse
        if (!plugin.getGameManager().getGameState().isApocalypse()) {
            return;
        }

        World world = plugin.getServer().getWorld(plugin.getConfig().getString("game.world_name", "world"));
        if (world == null) return;

        // Apply dark sky effect
        applyDarkSky(world);

        // Apply apocalypse effects around each player
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.getGameManager().getPlayerData(player);
            if (!data.isAlive()) continue;

            applyApocalypseEffectsAroundPlayer(player);
        }
    }

    /**
     * Apply dark sky effect (perpetual night/red sky)
     */
    private void applyDarkSky(World world) {
        if (!plugin.getConfig().getBoolean("difficulty.apocalypse.enable_dark_sky", true)) {
            return;
        }

        // Set time to night and prevent day cycle
        world.setTime(18000); // Midnight
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        // Set stormy/dark weather for red sky effect
        if (!world.hasStorm()) {
            world.setStorm(true);
            world.setThundering(false); // Storm but no lightning
            world.setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    /**
     * Apply apocalypse effects around a player (60 block radius)
     */
    private void applyApocalypseEffectsAroundPlayer(Player player) {
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();
        int radius = 60;

        // Spawn ash particles around player
        spawnAshParticles(player);

        // Check random blocks around player for water->lava and fire spawning
        for (int i = 0; i < 10; i++) {
            int x = playerLoc.getBlockX() + random.nextInt(radius * 2) - radius;
            int y = playerLoc.getBlockY() + random.nextInt(40) - 20; // -20 to +20 from player
            int z = playerLoc.getBlockZ() + random.nextInt(radius * 2) - radius;

            y = Math.max(world.getMinHeight(), Math.min(world.getMaxHeight() - 1, y));

            Block block = world.getBlockAt(x, y, z);

            // Water to lava conversion
            convertWaterToLava(block);

            // Fire spawning on ground
            spawnRandomFire(block);
        }

        // Spawn end crystals occasionally (if enabled)
        spawnEndCrystals(playerLoc);
    }

    /**
     * Spawn ash particles around player
     */
    private void spawnAshParticles(Player player) {
        double density = plugin.getConfig().getDouble("difficulty.apocalypse.ash_particle_density", 0.3);

        if (random.nextDouble() > density) {
            return;
        }

        Location loc = player.getLocation();

        // Spawn multiple ash particles falling from sky
        for (int i = 0; i < 3; i++) {
            loc.getWorld().spawnParticle(
                Particle.WHITE_ASH,
                loc.clone().add(
                    (random.nextDouble() - 0.5) * 10,
                    5 + random.nextDouble() * 5,
                    (random.nextDouble() - 0.5) * 10
                ),
                1,     // count
                0.5,   // offset X
                0,     // offset Y
                0.5,   // offset Z
                0.02   // speed (slow fall)
            );

            // Add some falling dust particles for extra effect
            loc.getWorld().spawnParticle(
                Particle.FALLING_DUST,
                loc.clone().add(
                    (random.nextDouble() - 0.5) * 10,
                    5 + random.nextDouble() * 5,
                    (random.nextDouble() - 0.5) * 10
                ),
                1,
                0.5,
                0,
                0.5,
                0.01,
                Material.GRAY_CONCRETE.createBlockData()
            );
        }

        // Occasional lava particle effect
        if (random.nextDouble() < 0.1) {
            loc.getWorld().spawnParticle(
                Particle.LAVA,
                loc.clone().add(
                    (random.nextDouble() - 0.5) * 8,
                    0.5,
                    (random.nextDouble() - 0.5) * 8
                ),
                3,
                1,
                0.1,
                1,
                0
            );
        }
    }

    /**
     * Convert water blocks to lava
     */
    private void convertWaterToLava(Block block) {
        double waterLavaChance = plugin.getConfig().getDouble("difficulty.apocalypse.water_to_lava_chance", 0.001);

        if (block.getType() == Material.WATER && random.nextDouble() < waterLavaChance) {
            block.setType(Material.LAVA);

            // Spawn particle effect at conversion
            block.getWorld().spawnParticle(
                Particle.LAVA,
                block.getLocation().add(0.5, 0.5, 0.5),
                10,
                0.5,
                0.5,
                0.5,
                0.05
            );

            block.getWorld().playSound(
                block.getLocation(),
                Sound.BLOCK_LAVA_EXTINGUISH,
                0.5f,
                0.5f
            );
        }
    }

    /**
     * Spawn random fire on ground blocks
     */
    private void spawnRandomFire(Block block) {
        double fireChance = plugin.getConfig().getDouble("difficulty.apocalypse.fire_spawn_chance", 0.0005);

        // Check if block is solid and can support fire
        if (!block.getType().isSolid()) {
            return;
        }

        Block above = block.getRelative(0, 1, 0);

        // Only spawn fire on exposed blocks (can see sky or near surface)
        if (above.getType() != Material.AIR) {
            return;
        }

        // Random chance to spawn fire
        if (random.nextDouble() < fireChance) {
            above.setType(Material.FIRE);

            // Spawn flame particles
            above.getWorld().spawnParticle(
                Particle.FLAME,
                above.getLocation().add(0.5, 0.5, 0.5),
                5,
                0.3,
                0.3,
                0.3,
                0.02
            );
        }
    }

    /**
     * Spawn end crystals occasionally (if enabled)
     */
    private void spawnEndCrystals(Location playerLoc) {
        if (!plugin.getConfig().getBoolean("difficulty.apocalypse.enable_end_crystals", false)) {
            return;
        }

        // Very low chance to spawn end crystals (they're powerful and laggy)
        if (random.nextDouble() < 0.001) {
            // Find a suitable ground location near player
            int x = playerLoc.getBlockX() + random.nextInt(40) - 20;
            int z = playerLoc.getBlockZ() + random.nextInt(40) - 20;
            int y = playerLoc.getWorld().getHighestBlockYAt(x, z) + 1;

            Location crystalLoc = new Location(playerLoc.getWorld(), x, y, z);
            Block block = crystalLoc.getBlock();

            if (block.getType() == Material.AIR) {
                // Spawn the end crystal
                EnderCrystal crystal = playerLoc.getWorld().spawn(crystalLoc, EnderCrystal.class);
                crystal.setShowingBottom(false);

                // Dramatic spawn effect
                playerLoc.getWorld().spawnParticle(
                    Particle.PORTAL,
                    crystalLoc,
                    100,
                    1,
                    1,
                    1,
                    0.5
                );

                playerLoc.getWorld().playSound(
                    crystalLoc,
                    Sound.ENTITY_ENDER_DRAGON_GROWL,
                    1.0f,
                    1.0f
                );
            }
        }
    }
}
