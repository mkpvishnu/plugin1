package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.Season;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Controls world weather based on the current season for immersion
 * Runs every 5 minutes (6000 ticks)
 */
public class WeatherControlTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public WeatherControlTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Season currentSeason = plugin.getGameManager().getGameState().getCurrentSeason();
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("game.world_name", "world"));

        if (world == null) return;

        switch (currentSeason) {
            case SPRING:
                // Spring: Normal weather cycle (mix of clear and rain)
                // Let Minecraft handle natural weather
                break;

            case SUMMER:
                // Summer: Always sunny, no rain
                if (world.hasStorm()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                // Set clear weather for a long duration
                world.setWeatherDuration(6000); // 5 minutes until next check
                break;

            case FALL:
                // Fall: Frequent rain (60-70% of the time)
                if (!world.hasStorm()) {
                    // 65% chance to start rain
                    if (Math.random() < 0.65) {
                        world.setStorm(true);
                        world.setWeatherDuration(6000 + (int)(Math.random() * 12000)); // 5-15 minutes
                    }
                } else {
                    // Keep it rainy
                    world.setWeatherDuration(6000 + (int)(Math.random() * 12000));
                }
                break;

            case WINTER:
                // Winter: Clear skies (cold and desolate look)
                // In naturally snowy biomes, this allows snow without rain
                if (world.hasStorm()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                world.setWeatherDuration(6000);
                break;
        }
    }
}
