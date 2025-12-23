package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles Gathering Tree furnace-related passive skills (Bulk Processing)
 */
public class FurnaceListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;

    public FurnaceListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Bulk Processing: 25% chance to smelt 2x items at once
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Location furnaceLocation = event.getBlock().getLocation();
        ItemStack result = event.getResult();

        // Find nearby players with Bulk Processing skill (within 10 blocks)
        for (Player player : furnaceLocation.getWorld().getNearbyPlayers(furnaceLocation, 10.0)) {
            if (effectManager.hasSkillByName(player, "bulk_processing")) {
                // 25% chance to double the smelted item
                if (Math.random() < 0.25) {
                    // Add bonus item to furnace result slot
                    // We'll schedule this to run next tick to avoid modifying during event
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getBlock().getState() instanceof org.bukkit.block.Furnace furnace) {
                            ItemStack currentResult = furnace.getInventory().getResult();

                            if (currentResult != null && currentResult.getType() == result.getType()) {
                                // Add one more to the result
                                if (currentResult.getAmount() < currentResult.getMaxStackSize()) {
                                    currentResult.setAmount(currentResult.getAmount() + 1);
                                } else {
                                    // Stack full, try to add to inventory if player is nearby
                                    if (player.getLocation().distance(furnaceLocation) <= 10.0) {
                                        ItemStack bonus = result.clone();
                                        bonus.setAmount(1);
                                        player.getInventory().addItem(bonus);
                                        player.sendMessage(org.bukkit.ChatColor.GOLD + "⚙ Bulk Processing! Bonus item sent to inventory");
                                    }
                                }
                            }
                        }
                    });

                    // Visual: Extra furnace smoke particles
                    furnaceLocation.getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        furnaceLocation.clone().add(0.5, 0.8, 0.5),
                        15,
                        0.2, 0.2, 0.2,
                        0.02
                    );

                    // Additional flame particles for "double processing" effect
                    furnaceLocation.getWorld().spawnParticle(
                        Particle.FLAME,
                        furnaceLocation.clone().add(0.5, 0.8, 0.5),
                        10,
                        0.15, 0.15, 0.15,
                        0.01
                    );

                    break; // Only apply once even if multiple players have the skill
                }
            }
        }
    }
}
