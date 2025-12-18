package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.models.TeamData;
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
            MessageUtils.sendMessage(sender, "&e/soc setpoints <team> <amount>");
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

            case "setpoints":
                if (args.length < 3) {
                    MessageUtils.sendError(sender, "Usage: /soc setpoints <team> <amount>");
                    return true;
                }

                // Parse team identifier (name or ID)
                TeamData team = parseTeam(args[1]);
                if (team == null) {
                    MessageUtils.sendError(sender, "Unknown team: " + args[1]);
                    MessageUtils.sendMessage(sender, "&7Available teams: North, West, Center, East, South (or 1-5)");
                    return true;
                }

                // Parse amount
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount < 0) {
                        MessageUtils.sendError(sender, "Amount must be positive!");
                        return true;
                    }

                    // Set points
                    team.setQuestPoints(amount);
                    plugin.getTeamManager().saveTeam(team);

                    // Confirm to admin
                    MessageUtils.sendSuccess(sender, "Set " + team.getColoredName() + " &apoints to " + amount);

                    // Broadcast to team
                    plugin.getServer().getOnlinePlayers().forEach(p -> {
                        PlayerData pd = plugin.getGameManager().getPlayerData(p);
                        if (pd.getTeamId() == team.getTeamId()) {
                            MessageUtils.sendMessage(p, "&eTeam points set to " + amount + " by an admin");
                        }
                    });

                    // Log
                    plugin.getLogger().info(sender.getName() + " set " + team.getName() + " points to " + amount);

                } catch (NumberFormatException e) {
                    MessageUtils.sendError(sender, "Invalid amount: " + args[2]);
                }
                break;

            default:
                MessageUtils.sendError(sender, "Unknown subcommand: " + args[0]);
        }

        return true;
    }

    /**
     * Parse team from string (name or ID)
     */
    private TeamData parseTeam(String input) {
        // Try parsing as ID first
        try {
            int teamId = Integer.parseInt(input);
            if (teamId >= 1 && teamId <= 5) {
                return plugin.getTeamManager().getTeam(teamId);
            }
        } catch (NumberFormatException e) {
            // Not a number, try team name
        }

        // Try matching team name (case-insensitive)
        String lower = input.toLowerCase();
        for (int i = 1; i <= 5; i++) {
            TeamData team = plugin.getTeamManager().getTeam(i);
            if (team != null && team.getName().toLowerCase().equals(lower)) {
                return team;
            }
        }

        return null;
    }
}
