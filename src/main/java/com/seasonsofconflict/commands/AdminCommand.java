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
            MessageUtils.sendMessage(sender, "&e/soc reactivate <team> &7- Reactivate an eliminated team");
            MessageUtils.sendMessage(sender, "&e/soc territories &7- View territory status");
            MessageUtils.sendMessage(sender, "&e/soc teams &7- View team status");
            MessageUtils.sendMessage(sender, "&e/soc gameinfo &7- View game state");
            MessageUtils.sendMessage(sender, "&e/soc event <trigger|stop|list|info> &7- Manage world events");
            MessageUtils.sendMessage(sender, "&e/soc skills <subcommand> &7- Manage player skills/XP");
            MessageUtils.sendMessage(sender, "&c/soc resetall confirm &7- RESET EVERYTHING (requires 'confirm')");
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

            case "reactivate":
                if (args.length < 2) {
                    MessageUtils.sendError(sender, "Usage: /soc reactivate <team>");
                    return true;
                }

                TeamData reactivateTeam = parseTeam(args[1]);
                if (reactivateTeam == null) {
                    MessageUtils.sendError(sender, "Unknown team: " + args[1]);
                    MessageUtils.sendMessage(sender, "&7Available teams: North, West, Center, East, South (or 1-5)");
                    return true;
                }

                if (!reactivateTeam.isEliminated()) {
                    MessageUtils.sendError(sender, reactivateTeam.getName() + " is not eliminated!");
                    return true;
                }

                // Reactivate the team
                reactivateTeam.setEliminated(false);
                plugin.getTeamManager().saveTeam(reactivateTeam);

                // Broadcast to server
                Bukkit.broadcastMessage(MessageUtils.colorize("&6&l[!] " + reactivateTeam.getColoredName() +
                    " &6&lhas been REACTIVATED by an admin!"));

                MessageUtils.sendSuccess(sender, "Reactivated " + reactivateTeam.getColoredName() + " &a(admin)");
                plugin.getLogger().info(sender.getName() + " admin-reactivated team " + reactivateTeam.getName());
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

            case "event":
                handleEventCommand(sender, args);
                break;

            case "skills":
                handleSkillsCommand(sender, args);
                break;

            case "resetall":
                if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                    MessageUtils.sendMessage(sender, "&c&l⚠ WARNING: COMPLETE GAME RESET ⚠");
                    MessageUtils.sendMessage(sender, "");
                    MessageUtils.sendMessage(sender, "&7This will reset:");
                    MessageUtils.sendMessage(sender, "&7  • All player skills and XP");
                    MessageUtils.sendMessage(sender, "&7  • All team points and territories");
                    MessageUtils.sendMessage(sender, "&7  • All team elimination status");
                    MessageUtils.sendMessage(sender, "&7  • Game state (season, cycle, apocalypse)");
                    MessageUtils.sendMessage(sender, "&7  • Player stats (kills, deaths, revivals)");
                    MessageUtils.sendMessage(sender, "");
                    MessageUtils.sendMessage(sender, "&c&lThis CANNOT be undone!");
                    MessageUtils.sendMessage(sender, "");
                    MessageUtils.sendMessage(sender, "&eTo confirm, use: &c/soc resetall confirm");
                    return true;
                }

                // User confirmed - perform complete reset
                MessageUtils.sendMessage(sender, "&6&l[!] Starting complete game reset...");

                int onlinePlayers = Bukkit.getOnlinePlayers().size();

                // 1. Reset all player skills and XP
                MessageUtils.sendMessage(sender, "&7[1/6] Resetting player skills and XP...");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    plugin.getSkillManager().resetAllTrees(p.getUniqueId(), 0); // Free reset
                    plugin.getXPManager().resetPlayerXP(p.getUniqueId());
                }
                plugin.getSkillManager().saveAll();

                // 2. Reset all teams
                MessageUtils.sendMessage(sender, "&7[2/6] Resetting all teams...");
                for (int teamId = 1; teamId <= 5; teamId++) {
                    TeamData team = plugin.getTeamManager().getTeam(teamId);
                    if (team != null) {
                        team.setQuestPoints(0);
                        team.setEliminated(false);
                        team.getControlledTerritories().clear();
                        plugin.getTeamManager().saveTeam(team);
                    }
                }

                // 3. Reset all territories to neutral
                MessageUtils.sendMessage(sender, "&7[3/6] Resetting all territories...");
                for (int territoryId = 1; territoryId <= 5; territoryId++) {
                    TerritoryData territory = plugin.getTerritoryManager().getTerritory(territoryId);
                    if (territory != null) {
                        // Set to original owner from config
                        int originalOwner = plugin.getConfig().getInt("territories." + territoryId + ".starting_owner", 0);
                        territory.setOwnerTeamId(originalOwner);
                        territory.setCaptureProgress(0);
                        territory.setCapturingTeamId(0);
                        plugin.getTerritoryManager().saveTerritory(territory);
                    }
                }

                // 4. Reset game state
                MessageUtils.sendMessage(sender, "&7[4/6] Resetting game state...");
                GameState gameState = plugin.getGameManager().getGameState();
                gameState.setCurrentSeason(Season.SPRING);
                gameState.setCurrentCycle(1);
                gameState.setDayInSeason(0);
                plugin.getDifficultyManager().stopApocalypse();
                plugin.getSeasonManager().applySeason(Season.SPRING);
                plugin.getDifficultyManager().applyCycleScaling(1);

                // 5. Reset all player data
                MessageUtils.sendMessage(sender, "&7[5/6] Resetting player data...");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    PlayerData pd = plugin.getGameManager().getPlayerData(p);
                    pd.setTotalKills(0);
                    pd.setTotalDeaths(0);
                    pd.setKillStreak(0);
                    pd.setBounty(0);
                    pd.setRevivalsUsed(0);
                    pd.setAlive(true);
                    plugin.getGameManager().savePlayerData(p.getUniqueId());

                    // Reset health
                    p.setGameMode(GameMode.SURVIVAL);
                    plugin.getHealthManager().setMaxHealth(p);
                }

                // 6. Save everything
                MessageUtils.sendMessage(sender, "&7[6/6] Saving all data...");
                plugin.getDataManager().saveAll();

                // Done!
                MessageUtils.sendSuccess(sender, "Complete game reset finished!");
                MessageUtils.sendMessage(sender, "&7Reset " + onlinePlayers + " online players");

                // Broadcast to all players
                Bukkit.broadcastMessage(MessageUtils.colorize(""));
                Bukkit.broadcastMessage(MessageUtils.colorize("&c&l⚠ GAME HAS BEEN COMPLETELY RESET ⚠"));
                Bukkit.broadcastMessage(MessageUtils.colorize("&7All progress, skills, XP, and stats have been wiped."));
                Bukkit.broadcastMessage(MessageUtils.colorize("&7The game has restarted from the beginning."));
                Bukkit.broadcastMessage(MessageUtils.colorize(""));

                plugin.getLogger().warning(sender.getName() + " performed a complete game reset!");
                break;

            default:
                MessageUtils.sendError(sender, "Unknown subcommand: " + args[0]);
        }

        return true;
    }

    /**
     * Handle event subcommands
     */
    private void handleEventCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, "&6=== World Event Commands ===");
            MessageUtils.sendMessage(sender, "&e/soc event trigger <type> &7- Manually start an event");
            MessageUtils.sendMessage(sender, "&e/soc event stop &7- End current event");
            MessageUtils.sendMessage(sender, "&e/soc event list &7- List all event types");
            MessageUtils.sendMessage(sender, "&e/soc event info <type> &7- Show event details");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "trigger":
            case "start":
                if (args.length < 3) {
                    MessageUtils.sendError(sender, "Usage: /soc event trigger <event_type>");
                    MessageUtils.sendMessage(sender, "&7Types: blood_moon, meteor_shower, aurora, fog, heatwave");
                    return;
                }

                String eventType = args[2].toLowerCase();

                // Check if events are enabled
                if (!plugin.getConfig().getBoolean("world_events.enabled", true)) {
                    MessageUtils.sendError(sender, "World events are disabled in config!");
                    return;
                }

                // Check if there's already an active event
                if (plugin.getWorldEventManager().hasActiveEvent()) {
                    String activeEvent = plugin.getWorldEventManager().getActiveEventType();
                    MessageUtils.sendError(sender, "An event is already active: " + activeEvent);
                    MessageUtils.sendMessage(sender, "&7Use '/soc event stop' first");
                    return;
                }

                // Trigger the event
                boolean success = plugin.getWorldEventManager().triggerEvent(eventType);

                if (success) {
                    MessageUtils.sendSuccess(sender, "Triggered event: &6" + eventType.replace("_", " ").toUpperCase());
                    plugin.getLogger().info(sender.getName() + " triggered world event: " + eventType);
                } else {
                    MessageUtils.sendError(sender, "Unknown event type: " + eventType);
                    MessageUtils.sendMessage(sender, "&7Available: blood_moon, meteor_shower, aurora, fog, heatwave");
                }
                break;

            case "stop":
            case "end":
                if (!plugin.getWorldEventManager().hasActiveEvent()) {
                    MessageUtils.sendError(sender, "No event is currently active!");
                    return;
                }

                String stoppedEvent = plugin.getWorldEventManager().getActiveEventType();
                plugin.getWorldEventManager().endCurrentEvent();

                MessageUtils.sendSuccess(sender, "Stopped event: &6" + stoppedEvent.replace("_", " ").toUpperCase());
                plugin.getLogger().info(sender.getName() + " stopped world event: " + stoppedEvent);
                break;

            case "list":
                MessageUtils.sendMessage(sender, "&6&l=== Available World Events ===");
                MessageUtils.sendMessage(sender, "&e1. blood_moon &7- 2x mobs, increased danger");
                MessageUtils.sendMessage(sender, "&e2. meteor_shower &7- Falling meteors with ore deposits");
                MessageUtils.sendMessage(sender, "&e3. aurora &7- Speed boost for all players");
                MessageUtils.sendMessage(sender, "&e4. fog &7- Reduced visibility");
                MessageUtils.sendMessage(sender, "&e5. heatwave &7- Outdoor damage (Summer only)");
                MessageUtils.sendMessage(sender, "");
                MessageUtils.sendMessage(sender, "&7Use '/soc event trigger <type>' to start");
                break;

            case "info":
                if (args.length < 3) {
                    MessageUtils.sendError(sender, "Usage: /soc event info <event_type>");
                    return;
                }

                String infoType = args[2].toLowerCase();
                showEventInfo(sender, infoType);
                break;

            default:
                MessageUtils.sendError(sender, "Unknown event subcommand: " + args[1]);
                MessageUtils.sendMessage(sender, "&7Use: trigger, stop, list, info");
        }
    }

    /**
     * Show detailed info about an event type
     */
    private void showEventInfo(CommandSender sender, String eventType) {
        String basePath = "world_events." + eventType + ".";

        if (!plugin.getConfig().contains(basePath + "enabled")) {
            MessageUtils.sendError(sender, "Unknown event type: " + eventType);
            return;
        }

        boolean enabled = plugin.getConfig().getBoolean(basePath + "enabled", true);
        double chance = plugin.getConfig().getDouble(basePath + "chance", 0);
        int duration = plugin.getConfig().getInt(basePath + "duration_minutes", 0);

        MessageUtils.sendMessage(sender, "&6&l=== " + eventType.replace("_", " ").toUpperCase() + " ===");
        MessageUtils.sendMessage(sender, "&eEnabled: " + (enabled ? "&aYes" : "&cNo"));
        MessageUtils.sendMessage(sender, "&eChance: &f" + (int)(chance * 100) + "%");
        MessageUtils.sendMessage(sender, "&eDuration: &f" + duration + " minutes");
        MessageUtils.sendMessage(sender, "");

        switch (eventType) {
            case "blood_moon":
                double mobSpawn = plugin.getConfig().getDouble(basePath + "mob_spawn_multiplier", 2.0);
                double mobDamage = plugin.getConfig().getDouble(basePath + "mob_damage_multiplier", 2.0);
                double mobHealth = plugin.getConfig().getDouble(basePath + "mob_health_multiplier", 1.5);
                MessageUtils.sendMessage(sender, "&eEffects:");
                MessageUtils.sendMessage(sender, "&7• Mob Spawns: &f" + mobSpawn + "x");
                MessageUtils.sendMessage(sender, "&7• Mob Damage: &f" + mobDamage + "x");
                MessageUtils.sendMessage(sender, "&7• Mob Health: &f" + mobHealth + "x");
                MessageUtils.sendMessage(sender, "&7• Forces nighttime");
                break;

            case "meteor_shower":
                int meteorsPerMin = plugin.getConfig().getInt(basePath + "meteors_per_minute", 3);
                double explosionPower = plugin.getConfig().getDouble(basePath + "explosion_power", 2.0);
                boolean spawnOres = plugin.getConfig().getBoolean(basePath + "spawn_ores", true);
                MessageUtils.sendMessage(sender, "&eEffects:");
                MessageUtils.sendMessage(sender, "&7• Meteors/min: &f" + meteorsPerMin);
                MessageUtils.sendMessage(sender, "&7• Explosion Power: &f" + explosionPower);
                MessageUtils.sendMessage(sender, "&7• Spawn Ores: " + (spawnOres ? "&aYes" : "&cNo"));
                break;

            case "aurora":
                int speedLevel = plugin.getConfig().getInt(basePath + "speed_amplifier", 2);
                boolean nightOnly = plugin.getConfig().getBoolean(basePath + "night_only", true);
                MessageUtils.sendMessage(sender, "&eEffects:");
                MessageUtils.sendMessage(sender, "&7• Speed Level: &fII (" + speedLevel + ")");
                MessageUtils.sendMessage(sender, "&7• Night Only: " + (nightOnly ? "&aYes" : "&cNo"));
                MessageUtils.sendMessage(sender, "&7• Northern lights particles");
                break;

            case "fog":
                int blindnessLevel = plugin.getConfig().getInt(basePath + "blindness_amplifier", 0);
                MessageUtils.sendMessage(sender, "&eEffects:");
                MessageUtils.sendMessage(sender, "&7• Cloud particles");
                MessageUtils.sendMessage(sender, "&7• Blindness: " + (blindnessLevel > 0 ? "&fLevel " + blindnessLevel : "&cDisabled"));
                break;

            case "heatwave":
                double damage = plugin.getConfig().getDouble(basePath + "damage_per_tick", 0.5);
                MessageUtils.sendMessage(sender, "&eEffects:");
                MessageUtils.sendMessage(sender, "&7• Outdoor Damage: &f" + damage + " HP per 5s");
                MessageUtils.sendMessage(sender, "&7• Summer Only: &aYes");
                MessageUtils.sendMessage(sender, "&7• Flame particles");
                break;
        }
    }

    /**
     * Handle skills subcommands
     */
    private void handleSkillsCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, "&6=== Skill Admin Commands ===");
            MessageUtils.sendMessage(sender, "&e/soc skills give <player> <amount> &7- Give skill points");
            MessageUtils.sendMessage(sender, "&e/soc skills set <player> <amount> &7- Set skill points");
            MessageUtils.sendMessage(sender, "&e/soc skills reset <player> [tree] &7- Force reset (free)");
            MessageUtils.sendMessage(sender, "&e/soc skills unlock <player> <skill> &7- Force unlock skill");
            MessageUtils.sendMessage(sender, "&e/soc skills info <player> &7- View player's skills");
            MessageUtils.sendMessage(sender, "&e/soc skills clearall &7- Reset all players' skills");
            MessageUtils.sendMessage(sender, "&e/soc skills givexp <player> <amount> &7- Give XP to player");
            MessageUtils.sendMessage(sender, "&e/soc skills reload &7- Reload skill configs");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "give":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /soc skills give <player> <amount>");
                    return;
                }

                Player targetGive = Bukkit.getPlayer(args[2]);
                if (targetGive == null) {
                    MessageUtils.sendError(sender, "Player not found: " + args[2]);
                    return;
                }

                try {
                    int amount = Integer.parseInt(args[3]);
                    if (amount <= 0) {
                        MessageUtils.sendError(sender, "Amount must be positive!");
                        return;
                    }

                    com.seasonsofconflict.models.PlayerSkills skills =
                        plugin.getSkillManager().getPlayerSkills(targetGive.getUniqueId());
                    skills.addSkillPoints(amount);
                    plugin.getSkillManager().savePlayerSkills(skills);

                    MessageUtils.sendSuccess(sender, "Gave " + targetGive.getName() + " +" + amount + " skill points");
                    MessageUtils.sendSuccess(targetGive, "You received +" + amount + " skill points from an admin!");
                    plugin.getLogger().info(sender.getName() + " gave " + targetGive.getName() + " " + amount + " skill points");
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(sender, "Invalid amount: " + args[3]);
                }
                break;

            case "set":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /soc skills set <player> <amount>");
                    return;
                }

                Player targetSet = Bukkit.getPlayer(args[2]);
                if (targetSet == null) {
                    MessageUtils.sendError(sender, "Player not found: " + args[2]);
                    return;
                }

                try {
                    int amount = Integer.parseInt(args[3]);
                    if (amount < 0) {
                        MessageUtils.sendError(sender, "Amount cannot be negative!");
                        return;
                    }

                    com.seasonsofconflict.models.PlayerSkills skills =
                        plugin.getSkillManager().getPlayerSkills(targetSet.getUniqueId());
                    int current = skills.getSkillPointsAvailable();
                    int diff = amount - current;
                    if (diff > 0) {
                        skills.addSkillPoints(diff);
                    } else if (diff < 0) {
                        skills.spendSkillPoints(-diff);
                    }
                    plugin.getSkillManager().savePlayerSkills(skills);

                    MessageUtils.sendSuccess(sender, "Set " + targetSet.getName() + "'s skill points to " + amount);
                    MessageUtils.sendMessage(targetSet, "&eYour skill points were set to " + amount + " by an admin");
                    plugin.getLogger().info(sender.getName() + " set " + targetSet.getName() + "'s skill points to " + amount);
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(sender, "Invalid amount: " + args[3]);
                }
                break;

            case "reset":
                if (args.length < 3) {
                    MessageUtils.sendError(sender, "Usage: /soc skills reset <player> [tree]");
                    return;
                }

                Player targetReset = Bukkit.getPlayer(args[2]);
                if (targetReset == null) {
                    MessageUtils.sendError(sender, "Player not found: " + args[2]);
                    return;
                }

                if (args.length >= 4) {
                    // Reset specific tree
                    try {
                        com.seasonsofconflict.models.SkillTree tree =
                            com.seasonsofconflict.models.SkillTree.valueOf(args[3].toUpperCase());
                        plugin.getSkillManager().resetTree(targetReset.getUniqueId(), tree, 0); // Free admin reset
                        MessageUtils.sendSuccess(sender, "Reset " + targetReset.getName() + "'s " +
                            tree.getDisplayName() + " tree (admin)");
                        MessageUtils.sendMessage(targetReset, "&eYour " + tree.getDisplayName() +
                            " tree was reset by an admin");
                        plugin.getLogger().info(sender.getName() + " reset " + targetReset.getName() +
                            "'s " + tree.name() + " tree");
                    } catch (IllegalArgumentException e) {
                        MessageUtils.sendError(sender, "Invalid tree! Use: COMBAT, GATHERING, SURVIVAL, TEAMWORK");
                    }
                } else {
                    // Reset all trees
                    plugin.getSkillManager().resetAllTrees(targetReset.getUniqueId(), 0); // Free admin reset
                    MessageUtils.sendSuccess(sender, "Reset ALL of " + targetReset.getName() + "'s skills (admin)");
                    MessageUtils.sendMessage(targetReset, "&eAll your skills were reset by an admin");
                    plugin.getLogger().info(sender.getName() + " reset all of " + targetReset.getName() + "'s skills");
                }
                break;

            case "unlock":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /soc skills unlock <player> <skill_name>");
                    MessageUtils.sendMessage(sender, "&7Example: /soc skills unlock Steve swift_strikes");
                    return;
                }

                Player targetUnlock = Bukkit.getPlayer(args[2]);
                if (targetUnlock == null) {
                    MessageUtils.sendError(sender, "Player not found: " + args[2]);
                    return;
                }

                String skillName = args[3].toLowerCase();
                // Find skill by internal name
                com.seasonsofconflict.models.Skill skill = null;
                for (com.seasonsofconflict.models.Skill s : com.seasonsofconflict.models.Skill.values()) {
                    if (s.getInternalName().equalsIgnoreCase(skillName) || s.name().equalsIgnoreCase(skillName)) {
                        skill = s;
                        break;
                    }
                }

                if (skill == null) {
                    MessageUtils.sendError(sender, "Unknown skill: " + skillName);
                    MessageUtils.sendMessage(sender, "&7Use skill internal names (e.g., swift_strikes)");
                    return;
                }

                com.seasonsofconflict.models.PlayerSkills skills =
                    plugin.getSkillManager().getPlayerSkills(targetUnlock.getUniqueId());

                // Force unlock (bypass restrictions)
                skills.forceUnlockSkill(skill.getTree(), skill.getTier(), skill.getInternalName());
                plugin.getSkillManager().savePlayerSkills(skills);

                MessageUtils.sendSuccess(sender, "Force unlocked " + skill.getDisplayName() +
                    " for " + targetUnlock.getName());
                MessageUtils.sendMessage(targetUnlock, "&eYou received skill: " + skill.getDisplayName() +
                    " &e(admin grant)");
                plugin.getLogger().info(sender.getName() + " force-unlocked " + skill.name() +
                    " for " + targetUnlock.getName());
                break;

            case "info":
                if (args.length < 3) {
                    MessageUtils.sendError(sender, "Usage: /soc skills info <player>");
                    return;
                }

                Player targetInfo = Bukkit.getPlayer(args[2]);
                if (targetInfo == null) {
                    MessageUtils.sendError(sender, "Player not found: " + args[2]);
                    return;
                }

                com.seasonsofconflict.models.PlayerSkills skillsInfo =
                    plugin.getSkillManager().getPlayerSkills(targetInfo.getUniqueId());
                com.seasonsofconflict.managers.XPManager.PlayerXPData xpData =
                    plugin.getXPManager().getPlayerXP(targetInfo.getUniqueId());

                MessageUtils.sendMessage(sender, "&6&l=== " + targetInfo.getName() + "'s Skills ===");
                MessageUtils.sendMessage(sender, "&eSkill Points: &f" + skillsInfo.getSkillPointsAvailable() +
                    " available, " + skillsInfo.getSkillPointsSpent() + " spent");
                MessageUtils.sendMessage(sender, "&eXP: &f" + xpData.getTotalXP() + " total, " +
                    xpData.getCurrentXP() + "/" + plugin.getXPManager().getXPForSkillPoint() + " to next point");
                MessageUtils.sendMessage(sender, "&eUltimates: &f" + skillsInfo.getUltimateCount() + "/2");
                MessageUtils.sendMessage(sender, "");

                for (com.seasonsofconflict.models.SkillTree tree : com.seasonsofconflict.models.SkillTree.values()) {
                    int pointsSpent = skillsInfo.getPointsSpentInTree(tree);
                    boolean hasUltimate = skillsInfo.hasSkill(tree, com.seasonsofconflict.models.SkillTier.ULTIMATE);
                    String ultimateStr = hasUltimate ? "&a✓" : "&7✗";

                    MessageUtils.sendMessage(sender, tree.getIcon() + " &e" + tree.getDisplayName() +
                        " &7(" + pointsSpent + " pts) " + ultimateStr);

                    // List unlocked skills in this tree
                    StringBuilder unlockedSkills = new StringBuilder("   &7Skills: &f");
                    boolean hasAny = false;
                    for (com.seasonsofconflict.models.Skill s : com.seasonsofconflict.models.Skill.values()) {
                        if (s.getTree() == tree && skillsInfo.hasSkill(tree, s.getTier())) {
                            if (hasAny) unlockedSkills.append(", ");
                            unlockedSkills.append(s.getDisplayName());
                            hasAny = true;
                        }
                    }
                    if (!hasAny) unlockedSkills.append("&7None");
                    MessageUtils.sendMessage(sender, unlockedSkills.toString());
                }
                break;

            case "clearall":
                MessageUtils.sendMessage(sender, "&c&lWARNING: &7This will reset ALL players' skills!");
                MessageUtils.sendMessage(sender, "&7To confirm, type: &e/soc skills confirmclearall");
                break;

            case "confirmclearall":
                int count = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    plugin.getSkillManager().resetAllTrees(p.getUniqueId(), 0);
                    count++;
                }
                MessageUtils.sendSuccess(sender, "Reset skills for " + count + " online players");
                MessageUtils.broadcastRaw("&c&l⚠ &eAll player skills have been reset by an admin! &c&l⚠");
                plugin.getLogger().warning(sender.getName() + " reset all player skills (clearall)");
                break;

            case "givexp":
                if (args.length < 4) {
                    MessageUtils.sendError(sender, "Usage: /soc skills givexp <player> <amount>");
                    return;
                }

                Player targetXP = Bukkit.getPlayer(args[2]);
                if (targetXP == null) {
                    MessageUtils.sendError(sender, "Player not found: " + args[2]);
                    return;
                }

                try {
                    int xpAmount = Integer.parseInt(args[3]);
                    if (xpAmount <= 0) {
                        MessageUtils.sendError(sender, "XP amount must be positive!");
                        return;
                    }

                    int skillPoints = plugin.getXPManager().addXP(targetXP.getUniqueId(), xpAmount);

                    MessageUtils.sendSuccess(sender, "Gave " + targetXP.getName() + " +" + xpAmount + " XP");
                    if (skillPoints > 0) {
                        MessageUtils.sendMessage(sender, "&a└─ Earned " + skillPoints + " skill point(s)!");
                        MessageUtils.sendSuccess(targetXP, "You received +" + xpAmount + " XP and earned " +
                            skillPoints + " skill point(s)!");
                    } else {
                        MessageUtils.sendMessage(targetXP, "&eYou received +" + xpAmount + " XP from an admin");
                    }

                    plugin.getLogger().info(sender.getName() + " gave " + targetXP.getName() + " " + xpAmount + " XP");
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(sender, "Invalid XP amount: " + args[3]);
                }
                break;

            case "reload":
                plugin.getSkillManager().loadConfiguration();
                MessageUtils.sendSuccess(sender, "Reloaded skill configuration!");
                plugin.getLogger().info(sender.getName() + " reloaded skill configs");
                break;

            default:
                MessageUtils.sendError(sender, "Unknown skills subcommand: " + args[1]);
                MessageUtils.sendMessage(sender, "&7Use '/soc skills' for help");
        }
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
