package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public AdminCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("soc.admin")) {
            MessageUtils.sendError(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendMessage(sender, "&6=== SoC Admin Commands ===");
            MessageUtils.sendMessage(sender, "&e/soc setseason <season>");
            MessageUtils.sendMessage(sender, "&e/soc setcycle <number>");
            MessageUtils.sendMessage(sender, "&e/soc apocalypse");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setseason":
                if (args.length < 2) {
                    MessageUtils.sendError(sender, "Usage: /soc setseason <season>");
                    return true;
                }
                try {
                    Season season = Season.valueOf(args[1].toUpperCase());
                    plugin.getGameManager().getGameState().setCurrentSeason(season);
                    plugin.getSeasonManager().applySeason(season);
                    MessageUtils.sendSuccess(sender, "Season set to " + season);
                } catch (IllegalArgumentException e) {
                    MessageUtils.sendError(sender, "Invalid season! Use: SPRING, SUMMER, FALL, WINTER");
                }
                break;

            case "setcycle":
                if (args.length < 2) {
                    MessageUtils.sendError(sender, "Usage: /soc setcycle <number>");
                    return true;
                }
                try {
                    int cycle = Integer.parseInt(args[1]);
                    plugin.getGameManager().getGameState().setCurrentCycle(cycle);
                    plugin.getDifficultyManager().applyCycleScaling(cycle);
                    MessageUtils.sendSuccess(sender, "Cycle set to " + cycle);
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(sender, "Invalid number!");
                }
                break;

            case "apocalypse":
                plugin.getDifficultyManager().startApocalypse();
                MessageUtils.sendSuccess(sender, "Apocalypse mode activated!");
                break;

            default:
                MessageUtils.sendError(sender, "Unknown subcommand: " + args[0]);
        }

        return true;
    }
}
