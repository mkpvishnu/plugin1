package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listens for XP-earning events and awards skill XP
 */
public class XPGainListener implements Listener {

    private final SeasonsOfConflict plugin;

    public XPGainListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    /**
     * Award XP for mining/gathering
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();

        int xpAmount = getGatheringXP(type);
        if (xpAmount > 0) {
            awardXP(player, xpAmount, "Mining " + formatMaterialName(type));
        }
    }

    /**
     * Award XP for killing mobs or players
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        EntityType entityType = event.getEntityType();
        int xpAmount = 0;
        String source = "";

        // Player kill
        if (entityType == EntityType.PLAYER) {
            xpAmount = plugin.getConfig().getInt("skills.xp_sources.player_kill", 200);
            source = "Player Kill";
        }
        // Mob kill
        else {
            xpAmount = getMobKillXP(entityType);
            if (xpAmount > 0) {
                source = "Killing " + formatEntityName(entityType);
            }
        }

        if (xpAmount > 0) {
            awardXP(killer, xpAmount, source);
        }
    }

    /**
     * Get XP amount for gathering resources
     */
    private int getGatheringXP(Material type) {
        return switch (type) {
            case STONE, COBBLESTONE, DEEPSLATE, COBBLED_DEEPSLATE ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.stone", 1);

            case COAL_ORE, DEEPSLATE_COAL_ORE ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.coal", 2);

            case IRON_ORE, DEEPSLATE_IRON_ORE ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.iron", 3);

            case GOLD_ORE, DEEPSLATE_GOLD_ORE ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.gold", 5);

            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.diamond", 10);

            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.log", 2);

            case WHEAT, CARROTS, POTATOES, BEETROOTS ->
                plugin.getConfig().getInt("skills.xp_sources.gathering.wheat", 1);

            default -> 0;
        };
    }

    /**
     * Get XP amount for killing mobs
     */
    private int getMobKillXP(EntityType type) {
        return switch (type) {
            case ZOMBIE ->
                plugin.getConfig().getInt("skills.xp_sources.mob_kill.zombie", 10);

            case SKELETON ->
                plugin.getConfig().getInt("skills.xp_sources.mob_kill.skeleton", 15);

            case CREEPER ->
                plugin.getConfig().getInt("skills.xp_sources.mob_kill.creeper", 20);

            case SPIDER ->
                plugin.getConfig().getInt("skills.xp_sources.mob_kill.spider", 12);

            case ENDERMAN ->
                plugin.getConfig().getInt("skills.xp_sources.mob_kill.enderman", 50);

            case WITHER_SKELETON ->
                plugin.getConfig().getInt("skills.xp_sources.mob_kill.wither_skeleton", 100);

            default -> 0;
        };
    }

    /**
     * Award XP to a player
     */
    private void awardXP(Player player, int amount, String source) {
        // Inspirational Leader: +10% XP if teammate with skill is within 50 blocks
        boolean hasInspirationalLeader = plugin.getSkillEffectManager().hasNearbyInspirationalLeader(player);
        if (hasInspirationalLeader) {
            amount = (int) (amount * 1.10);
        }

        int skillPointsEarned = plugin.getXPManager().addXPWithMultipliers(player, amount);

        // Notify player if they earned skill points
        if (skillPointsEarned > 0) {
            String message = "&a&l+ " + skillPointsEarned + " Skill Point" +
                (skillPointsEarned > 1 ? "s" : "") + "! &7(Use /skills)";

            if (hasInspirationalLeader) {
                message += " &e(+10% from Inspirational Leader)";
            }

            MessageUtils.sendMessage(player, message);
        }
    }

    /**
     * Format material name for display
     */
    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return capitalizeWords(name);
    }

    /**
     * Format entity name for display
     */
    private String formatEntityName(EntityType type) {
        String name = type.name().toLowerCase().replace("_", " ");
        return capitalizeWords(name);
    }

    /**
     * Capitalize first letter of each word
     */
    private String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Award XP for quest completion (called by QuestManager)
     */
    public void awardQuestXP(Player player, int amount, String questName) {
        awardXP(player, amount, "Quest: " + questName);
    }
}
