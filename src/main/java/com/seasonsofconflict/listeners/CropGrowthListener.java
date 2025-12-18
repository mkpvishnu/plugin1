package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.Season;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class CropGrowthListener implements Listener {

    private final SeasonsOfConflict plugin;

    public CropGrowthListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCropGrow(BlockGrowEvent event) {
        Season season = plugin.getGameManager().getGameState().getCurrentSeason();

        // Spring: +50% crop growth speed (simulate by allowing growth more often)
        if (season == Season.SPRING) {
            double growthMultiplier = plugin.getConfig().getDouble("seasons.spring.crop_growth_multiplier", 1.5);

            // Give extra chance to advance growth stage
            if (Math.random() < (growthMultiplier - 1.0)) {
                BlockData data = event.getNewState().getBlockData();
                if (data instanceof Ageable) {
                    Ageable ageable = (Ageable) data;
                    if (ageable.getAge() < ageable.getMaximumAge()) {
                        ageable.setAge(ageable.getAge() + 1);
                        event.getNewState().setBlockData(ageable);
                    }
                }
            }
        }
    }
}
