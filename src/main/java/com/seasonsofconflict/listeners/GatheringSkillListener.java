package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.SkillEffectManager;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles Gathering Tree passive skill effects
 */
public class GatheringSkillListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;

    public GatheringSkillListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
    }

    /**
     * Handle block breaking with gathering passives
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Fortune's Touch: +15% chance for double drops
        if (effectManager.applyFortuneTouch(player)) {
            if (isGatherableResource(blockType)) {
                // Double the drops
                for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                }

                // Visual: Gold sparkles
                block.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    10,
                    0.3, 0.3, 0.3,
                    0.1
                );
            }
        }

        // Lumberjack: +50% wood drops from logs
        if (isLog(blockType)) {
            int bonusDrops = effectManager.applyLumberjack(player, 1);
            if (bonusDrops > 1) {
                // Add bonus wood drops
                ItemStack wood = new ItemStack(blockType, bonusDrops - 1);
                block.getWorld().dropItemNaturally(block.getLocation(), wood);

                // Visual: Falling leaves
                block.getWorld().spawnParticle(
                    Particle.FALLING_LEAVES,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    15,
                    0.5, 0.5, 0.5,
                    0.05
                );
            }
        }

        // Green Thumb: +20% crop yields
        if (isCrop(blockType)) {
            // This is handled more specifically in crop harvest events
            // But we can add bonus drops here too
            int bonusYield = effectManager.applyGreenThumb(player, 1);
            if (bonusYield > 1 && block.getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    // Fully grown crop - add bonus
                    for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                        if (Math.random() < 0.20) { // 20% chance for extra
                            block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                        }
                    }

                    // Visual: Bonemeal particles
                    block.getWorld().spawnParticle(
                        Particle.COMPOSTER,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        10,
                        0.3, 0.3, 0.3,
                        0.05
                    );
                }
            }
        }
    }

    /**
     * Apply Swift Hands effect (continuous Haste) every 10 seconds
     */
    public void applySwiftHandsEffect(Player player) {
        effectManager.applySwiftHands(player);
    }

    /**
     * Check if material is a gatherable resource (ore, log, crop)
     */
    private boolean isGatherableResource(Material material) {
        return material.name().contains("_ORE") ||
               isLog(material) ||
               isCrop(material);
    }

    /**
     * Check if material is a log
     */
    private boolean isLog(Material material) {
        return material.name().contains("_LOG") ||
               material.name().contains("_WOOD");
    }

    /**
     * Check if material is a crop
     */
    private boolean isCrop(Material material) {
        return material == Material.WHEAT ||
               material == Material.CARROTS ||
               material == Material.POTATOES ||
               material == Material.BEETROOTS ||
               material == Material.NETHER_WART ||
               material == Material.COCOA ||
               material == Material.SWEET_BERRY_BUSH ||
               material == Material.MELON ||
               material == Material.PUMPKIN;
    }
}
