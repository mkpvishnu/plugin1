package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.GameState;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            MessageUtils.sendMessage(sender, "&e/soc setseason <season> &7- Change season");
            MessageUtils.sendMessage(sender, "&e/soc setcycle <number> &7- Set difficulty cycle");
            MessageUtils.sendMessage(sender, "&e/soc setpoints <team> <amount> &7- Set team points");
            MessageUtils.sendMessage(sender, "&e/soc apocalypse [on|off] &7- Toggle apocalypse");
            MessageUtils.sendMessage(sender, "&e/soc revive <player> &7- Admin revive player");
            MessageUtils.sendMessage(sender, "&e/soc eliminate <team> &7- Eliminate a team");
            MessageUtils.sendMessage(sender, "&e/soc territories &7- View territory status");
            MessageUtils.sendMessage(sender, "&e/soc teams &7- View team status");
            MessageUtils.sendMessage(sender, "&e/soc gameinfo &7- View game state");
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
                if (args.length < 2) {
                    // Toggle
                    if (plugin.getDifficultyManager().isApocalypse()) {
                        plugin.getDifficultyManager().stopApocalypse();
                        MessageUtils.sendSuccess(sender, "Apocalypse mode disabled!");
                    } else {
                        plugin.getDifficultyManager().startApocalypse();
                        MessageUtils.sendSuccess(sender, "Apocalypse mode activated!");
                    }
                } else {
                    String action = args[1].toLowerCase();
                    switch (action) {
                        case "on":
                        case "start":
                            if (plugin.getDifficultyManager().isApocalypse()) {
                                MessageUtils.sendError(sender, "Apocalypse is already active!");
                            } else {
                                plugin.getDifficultyManager().startApocalypse();
                                MessageUtils.sendSuccess(sender, "Apocalypse mode activated!");
                            }
                            break;
                        case "off":
                        case "stop":
                            if (!plugin.getDifficultyManager().isApocalypse()) {
                                MessageUtils.sendError(sender, "Apocalypse is not active!");
                            } else {
                                plugin.getDifficultyManager().stopApocalypse();
                                MessageUtils.sendSuccess(sender, "Apocalypse mode disabled!");
                            }
                            break;
                        default:
                            MessageUtils.sendError(sender, "Usage: /soc apocalypse [on|off]");
                    }
                }
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

            case "revive":
                if (args.length < 2) {
                    MessageUtils.sendError(sender, "Usage: /soc revive <player>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    MessageUtils.sendError(sender, "Player not found!");
                    return true;
                }

                PlayerData targetData = plugin.getGameManager().getPlayerData(target);

                if (targetData.isAlive()) {
                    MessageUtils.sendError(sender, "That player is already alive!");
                    return true;
                }

                TeamData reviveTeam = plugin.getTeamManager().getTeam(targetData.getTeamId());
                if (reviveTeam == null || reviveTeam.isEliminated()) {
                    MessageUtils.sendError(sender, "Player's team is eliminated!");
                    return true;
                }

                // Set player alive (skip revival count)
                targetData.setAlive(true);
                plugin.getGameManager().savePlayerData(target.getUniqueId());

                // Teleport to team beacon
                TerritoryData homeTerritory = plugin.getTerritoryManager().getTerritory(reviveTeam.getHomeTerritory());
                if (homeTerritory != null) {
                    String worldName = plugin.getConfig().getString("game.world_name", "world");
                    Location spawnLocation = homeTerritory.getBeaconLocation(worldName);
                    spawnLocation.add(0, 2, 0);
                    target.teleport(spawnLocation);
                }

                // Set gamemode and health
                target.setGameMode(GameMode.SURVIVAL);
                plugin.getHealthManager().setMaxHealth(target);
                plugin.getHealthManager().giveSpawnProtection(target);

                // Notify
                MessageUtils.sendSuccess(sender, "Revived " + target.getName() + " (admin bypass)");
                MessageUtils.sendSuccess(target, "You have been revived by an admin!");

                plugin.getLogger().info(sender.getName() + " admin-revived " + target.getName());
                break;

            case "eliminate":
                if (args.length < 2) {
                    MessageUtils.sendError(sender, "Usage: /soc eliminate <team>");
                    return true;
                }

                TeamData targetTeam = parseTeam(args[1]);
                if (targetTeam == null) {
                    MessageUtils.sendError(sender, "Unknown team: " + args[1]);
                    MessageUtils.sendMessage(sender, "&7Available teams: North, West, Center, East, South (or 1-5)");
                    return true;
                }

                if (targetTeam.isEliminated()) {
                    MessageUtils.sendError(sender, targetTeam.getName() + " is already eliminated!");
                    return true;
                }

                // Eliminate the team
                plugin.getTeamManager().eliminateTeam(targetTeam.getTeamId());

                MessageUtils.sendSuccess(sender, "Eliminated " + targetTeam.getColoredName() + " &a(admin)");
                plugin.getLogger().info(sender.getName() + " admin-eliminated team " + targetTeam.getName());
                break;

            case "territories":
                MessageUtils.sendMessage(sender, "&6&l=== Territory Overview ===");
                for (int i = 1; i <= 5; i++) {
                    TerritoryData t = plugin.getTerritoryManager().getTerritory(i);
                    if (t != null) {
                        String owner = t.getOwnerTeamId() == 0 ? "&7[NEUTRAL]" :
                            plugin.getTeamManager().getTeam(t.getOwnerTeamId()).getColoredName();
                        String bonus = t.getBonusType() + " +" + t.getBaseBonusPercent() + "%";

                        MessageUtils.sendMessage(sender, "&e" + i + ". " + t.getName());
                        MessageUtils.sendMessage(sender, "   Owner: " + owner);
                        MessageUtils.sendMessage(sender, "   &7Bonus: &f" + bonus);
                        MessageUtils.sendMessage(sender, "   &7Beacon: &f" +
                            t.getBeaconX() + ", " + t.getBeaconY() + ", " + t.getBeaconZ());

                        if (t.getCapturingTeamId() != 0) {
                            TeamData capturingTeam = plugin.getTeamManager().getTeam(t.getCapturingTeamId());
                            MessageUtils.sendMessage(sender, "   &c⚠ Being captured by " +
                                capturingTeam.getColoredName() +
                                " &7(" + t.getCaptureProgress() + "%)");
                        }
                    }
                }
                break;

            case "teams":
                MessageUtils.sendMessage(sender, "&6&l=== Team Overview ===");
                for (int i = 1; i <= 5; i++) {
                    TeamData t = plugin.getTeamManager().getTeam(i);
                    if (t != null) {
                        String status = t.isEliminated() ? "&c&l[ELIMINATED]" : "&a&l[ACTIVE]";
                        int totalPlayers = plugin.getTeamManager().getPlayerCount(i);
                        int alivePlayers = plugin.getTeamManager().getAlivePlayerCount(i);
                        int territories = t.getControlledTerritories().size();

                        MessageUtils.sendMessage(sender, status + " " + t.getColoredName());
                        MessageUtils.sendMessage(sender, "   &7Players: &f" + alivePlayers + "/" + totalPlayers + " alive");
                        MessageUtils.sendMessage(sender, "   &7Points: &f" + t.getQuestPoints());
                        MessageUtils.sendMessage(sender, "   &7Territories: &f" + territories);

                        // List online players
                        StringBuilder onlinePlayers = new StringBuilder("   &7Online: &f");
                        boolean first = true;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            PlayerData pd = plugin.getGameManager().getPlayerData(p);
                            if (pd.getTeamId() == i) {
                                if (!first) onlinePlayers.append(", ");
                                onlinePlayers.append(p.getName());
                                if (!pd.isAlive()) onlinePlayers.append(" &c[DEAD]");
                                first = false;
                            }
                        }
                        if (first) onlinePlayers.append("&7None");
                        MessageUtils.sendMessage(sender, onlinePlayers.toString());
                    }
                }
                break;

            case "gameinfo":
                GameState gs = plugin.getGameManager().getGameState();
                MessageUtils.sendMessage(sender, "&6&l=== Game State ===");
                MessageUtils.sendMessage(sender, "&eSeason: &f" + gs.getCurrentSeason());
                MessageUtils.sendMessage(sender, "&eCycle: &f" + gs.getCurrentCycle());
                MessageUtils.sendMessage(sender, "&eApocalypse: &f" + (gs.isApocalypse() ? "&cYES" : "&aNo"));
                MessageUtils.sendMessage(sender, "&eWorld Border: &f" + gs.getWorldBorderSize());
                MessageUtils.sendMessage(sender, "&eRevival Cost: &f" + gs.getRevivalCost());
                MessageUtils.sendMessage(sender, "&eActive Teams: &f" + plugin.getTeamManager().getRemainingTeamCount());
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
