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

        // Vein Miner: Break connected ores when primed
        if (isOre(blockType)) {
            var activeSkillListener = plugin.getActiveSkillListener();
            if (activeSkillListener != null && activeSkillListener.isVeinMinerPrimed(player.getUniqueId())) {
                activeSkillListener.consumeVeinMinerPrime(player.getUniqueId());
                breakConnectedOres(player, block, blockType, 5);

                // Visual: Chain explosion particles
                block.getWorld().spawnParticle(
                    Particle.EXPLOSION_LARGE,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    3,
                    0.5, 0.5, 0.5,
                    0.1
                );

                MessageUtils.sendMessage(player, "&6⛏ &eVein Miner triggered!");
            }
        }

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

        // Prospector: +20% bonus drops from iron/gold/coal ores
        if (isCommonOre(blockType) && effectManager.hasSkillByName(player, "prospector")) {
            // 20% chance for bonus drop
            if (Math.random() < 0.20) {
                for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                }

                // Visual: Gold sparkles
                block.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    8,
                    0.3, 0.3, 0.3,
                    0.1
                );
            }
        }

        // Lumberjack: +50% wood drops + tree felling
        if (isLog(blockType) && effectManager.hasSkillByName(player, "lumberjack")) {
            // Apply +50% wood drops
            int normalDrops = 1;
            int bonusDrops = effectManager.applyLumberjack(player, normalDrops);

            if (bonusDrops > normalDrops) {
                // Add bonus wood drops
                ItemStack wood = new ItemStack(blockType, bonusDrops - normalDrops);
                block.getWorld().dropItemNaturally(block.getLocation(), wood);
            }

            // Tree felling: If breaking bottom log, fell entire tree
            if (isBottomLog(block)) {
                fellTree(player, block, blockType);
            }

            // Visual: Falling leaves
            block.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                block.getLocation().add(0.5, 0.5, 0.5),
                15,
                0.5, 0.5, 0.5,
                0.05,
                Material.OAK_LEAVES.createBlockData()
            );
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

    /**
     * Check if material is an ore
     */
    private boolean isOre(Material material) {
        return material.name().contains("_ORE");
    }

    /**
     * Check if material is a common ore (for Prospector skill: iron, gold, coal)
     */
    private boolean isCommonOre(Material material) {
        return material == Material.IRON_ORE ||
               material == Material.DEEPSLATE_IRON_ORE ||
               material == Material.GOLD_ORE ||
               material == Material.DEEPSLATE_GOLD_ORE ||
               material == Material.COAL_ORE ||
               material == Material.DEEPSLATE_COAL_ORE;
    }

    /**
     * Break connected ores of the same type (Vein Miner)
     */
    private void breakConnectedOres(Player player, Block startBlock, Material oreType, int maxOres) {
        java.util.Set<Block> visited = new java.util.HashSet<>();
        java.util.Queue<Block> toCheck = new java.util.LinkedList<>();
        toCheck.add(startBlock);
        visited.add(startBlock);

        int brokenCount = 0;

        while (!toCheck.isEmpty() && brokenCount < maxOres) {
            Block current = toCheck.poll();

            // Check all 6 adjacent blocks
            for (org.bukkit.util.Vector offset : new org.bukkit.util.Vector[]{
                new org.bukkit.util.Vector(1, 0, 0),
                new org.bukkit.util.Vector(-1, 0, 0),
                new org.bukkit.util.Vector(0, 1, 0),
                new org.bukkit.util.Vector(0, -1, 0),
                new org.bukkit.util.Vector(0, 0, 1),
                new org.bukkit.util.Vector(0, 0, -1)
            }) {
                Block adjacent = current.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());

                if (!visited.contains(adjacent) && adjacent.getType() == oreType) {
                    visited.add(adjacent);
                    toCheck.add(adjacent);

                    // Break the ore and give drops
                    for (ItemStack drop : adjacent.getDrops(player.getInventory().getItemInMainHand())) {
                        adjacent.getWorld().dropItemNaturally(adjacent.getLocation(), drop);
                    }
                    adjacent.setType(Material.AIR);

                    // Visual: Small explosion particles
                    adjacent.getWorld().spawnParticle(
                        Particle.BLOCK_CRACK,
                        adjacent.getLocation().add(0.5, 0.5, 0.5),
                        10,
                        0.3, 0.3, 0.3,
                        0.1,
                        adjacent.getBlockData()
                    );

                    brokenCount++;
                    if (brokenCount >= maxOres) {
                        break;
                    }
                }
            }
        }

        if (brokenCount > 0) {
            MessageUtils.sendMessage(player, "&7Vein mined &e" + brokenCount + " &7additional ores!");
        }
    }

    /**
     * Check if a log block is at the bottom of a tree
     */
    private boolean isBottomLog(Block block) {
        // Check if there's a non-log block below (ground level)
        Block below = block.getRelative(0, -1, 0);
        return !isLog(below.getType());
    }

    /**
     * Fell entire tree starting from bottom log
     */
    private void fellTree(Player player, Block startBlock, Material logType) {
        java.util.Set<Block> visited = new java.util.HashSet<>();
        java.util.Queue<Block> toCheck = new java.util.LinkedList<>();
        toCheck.add(startBlock);
        visited.add(startBlock);

        int felledCount = 0;
        int maxLogs = 128; // Prevent infinite loops on huge structures

        while (!toCheck.isEmpty() && felledCount < maxLogs) {
            Block current = toCheck.poll();

            // Check all adjacent blocks (including diagonals for branches)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        Block adjacent = current.getRelative(dx, dy, dz);

                        if (!visited.contains(adjacent) && isLog(adjacent.getType())) {
                            visited.add(adjacent);
                            toCheck.add(adjacent);

                            // Apply +50% bonus to each log
                            int bonusDrops = effectManager.applyLumberjack(player, 1);

                            // Drop the log with bonus
                            for (int i = 0; i < bonusDrops; i++) {
                                ItemStack logDrop = new ItemStack(adjacent.getType(), 1);
                                adjacent.getWorld().dropItemNaturally(adjacent.getLocation(), logDrop);
                            }

                            // Break the log
                            adjacent.setType(Material.AIR);

                            // Small leaf particles
                            if (felledCount % 3 == 0) { // Not every log to reduce lag
                                adjacent.getWorld().spawnParticle(
                                    Particle.BLOCK_CRACK,
                                    adjacent.getLocation().add(0.5, 0.5, 0.5),
                                    5,
                                    0.3, 0.3, 0.3,
                                    0.05,
                                    Material.OAK_LEAVES.createBlockData()
                                );
                            }

                            felledCount++;
                            if (felledCount >= maxLogs) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (felledCount > 0) {
            MessageUtils.sendMessage(player, "&6🪓 &eFelled tree: &a" + (felledCount + 1) + " &elogs!");
        }
    }
}
