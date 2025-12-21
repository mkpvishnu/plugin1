package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.XPManager;
import com.seasonsofconflict.models.PlayerSkills;
import com.seasonsofconflict.models.SkillTree;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /skills command for viewing skill stats
 */
public class SkillsCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public SkillsCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Subcommands
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();

            return switch (subCommand) {
                case "stats" -> showStats(player);
                case "xp" -> showXP(player);
                case "info" -> showInfo(player);
                default -> openGUI(player); // Default to GUI
            };
        }

        // No args - open GUI
        return openGUI(player);
    }

    /**
     * Open the skill tree GUI
     */
    private boolean openGUI(Player player) {
        plugin.getSkillTreeGUI().openMainMenu(player);
        return true;
    }

    /**
     * Show player's skill statistics
     */
    private boolean showStats(Player player) {
        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        XPManager.PlayerXPData xpData = plugin.getXPManager().getPlayerXP(player.getUniqueId());

        MessageUtils.sendMessage(player, "&6&l========== SKILL STATS ==========");
        MessageUtils.sendMessage(player, "");

        // Skill points
        MessageUtils.sendMessage(player, "&e&lSkill Points:");
        MessageUtils.sendMessage(player, "&7  Available: &a" + skills.getSkillPointsAvailable());
        MessageUtils.sendMessage(player, "&7  Spent: &c" + skills.getSkillPointsSpent());
        MessageUtils.sendMessage(player, "&7  Total: &f" + (skills.getSkillPointsAvailable() + skills.getSkillPointsSpent()));

        MessageUtils.sendMessage(player, "");

        // XP progress
        int currentXP = xpData.getCurrentXP();
        int xpNeeded = plugin.getXPManager().getXPForNextSkillPoint(player.getUniqueId());
        double progress = plugin.getXPManager().getProgressPercent(player.getUniqueId());

        MessageUtils.sendMessage(player, "&e&lXP Progress:");
        MessageUtils.sendMessage(player, "&7  Current XP: &b" + currentXP + "&7/" + (currentXP + xpNeeded));
        MessageUtils.sendMessage(player, "&7  Progress: &b" + String.format("%.1f%%", progress));
        MessageUtils.sendMessage(player, "&7  Total XP Earned: &f" + xpData.getTotalXP());

        MessageUtils.sendMessage(player, "");

        // Skills per tree
        MessageUtils.sendMessage(player, "&e&lSkills by Tree:");
        for (SkillTree tree : SkillTree.values()) {
            int pointsSpent = skills.getPointsSpentInTree(tree);
            boolean hasUltimate = skills.hasSkill(tree, com.seasonsofconflict.models.SkillTier.ULTIMATE);

            String ultimateBadge = hasUltimate ? " &6&l★" : "";
            MessageUtils.sendMessage(player, "  " + tree.getFormattedName() + " &7- &e" +
                pointsSpent + " pts" + ultimateBadge);
        }

        MessageUtils.sendMessage(player, "");

        // Ultimates
        MessageUtils.sendMessage(player, "&e&lUltimates: &f" + skills.getUltimateCount() + "&7/2");

        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&7Commands:");
        MessageUtils.sendMessage(player, "&7  /skills stats &f- View this page");
        MessageUtils.sendMessage(player, "&7  /skills xp &f- View XP sources");
        MessageUtils.sendMessage(player, "&7  /skills info &f- View skill system info");

        MessageUtils.sendMessage(player, "&6&l================================");

        return true;
    }

    /**
     * Show XP sources and values
     */
    private boolean showXP(Player player) {
        MessageUtils.sendMessage(player, "&6&l========== XP SOURCES ==========");
        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lGathering:");
        MessageUtils.sendMessage(player, "&7  Stone: &a1 XP");
        MessageUtils.sendMessage(player, "&7  Coal Ore: &a2 XP");
        MessageUtils.sendMessage(player, "&7  Iron Ore: &a3 XP");
        MessageUtils.sendMessage(player, "&7  Gold Ore: &a5 XP");
        MessageUtils.sendMessage(player, "&7  Diamond Ore: &a10 XP");
        MessageUtils.sendMessage(player, "&7  Logs: &a2 XP");

        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lCombat:");
        MessageUtils.sendMessage(player, "&7  Zombie: &a10 XP");
        MessageUtils.sendMessage(player, "&7  Skeleton: &a15 XP");
        MessageUtils.sendMessage(player, "&7  Creeper: &a20 XP");
        MessageUtils.sendMessage(player, "&7  Spider: &a12 XP");
        MessageUtils.sendMessage(player, "&7  Enderman: &a50 XP");
        MessageUtils.sendMessage(player, "&7  Player Kill: &a200 XP");

        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lQuests:");
        MessageUtils.sendMessage(player, "&7  Easy Quest: &a100 XP");
        MessageUtils.sendMessage(player, "&7  Medium Quest: &a250 XP");
        MessageUtils.sendMessage(player, "&7  Hard Quest: &a500 XP");

        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&b&l500 XP = 1 Skill Point");

        MessageUtils.sendMessage(player, "&6&l================================");

        return true;
    }

    /**
     * Show skill system info
     */
    private boolean showInfo(Player player) {
        MessageUtils.sendMessage(player, "&6&l========== SKILL SYSTEM ==========");
        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lAbout Skills:");
        MessageUtils.sendMessage(player, "&7  Earn XP from gathering, combat, and quests.");
        MessageUtils.sendMessage(player, "&7  Every 500 XP = 1 Skill Point.");
        MessageUtils.sendMessage(player, "&7  Spend points to unlock powerful abilities!");

        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lSkill Trees:");
        MessageUtils.sendMessage(player, "&7  🗡️  Combat &f- High damage, aggressive combat");
        MessageUtils.sendMessage(player, "&7  ⛏️  Gathering &f- Resource efficiency, wealth");
        MessageUtils.sendMessage(player, "&7  🛡️  Survival &f- Tanking, sustain, resistance");
        MessageUtils.sendMessage(player, "&7  👥 Teamwork &f- Team buffs, support abilities");

        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lRules:");
        MessageUtils.sendMessage(player, "&7  • Choose 1 skill per tier in each tree");
        MessageUtils.sendMessage(player, "&7  • Must unlock tiers sequentially (1→2→3→4→5)");
        MessageUtils.sendMessage(player, "&7  • Maximum 2 Ultimate abilities (Tier 5)");
        MessageUtils.sendMessage(player, "&7  • Maximum 100 total skill points");
        MessageUtils.sendMessage(player, "&7  • Skills reset when difficulty cycle advances");

        MessageUtils.sendMessage(player, "");

        MessageUtils.sendMessage(player, "&e&lCommands:");
        MessageUtils.sendMessage(player, "&7  /skills &f- Open skill tree GUI");
        MessageUtils.sendMessage(player, "&7  /skills stats &f- View text stats");
        MessageUtils.sendMessage(player, "&7  /skills xp &f- View XP sources");

        MessageUtils.sendMessage(player, "&6&l==================================");

        return true;
    }
}
