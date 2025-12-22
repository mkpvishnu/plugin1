package com.seasonsofconflict.tabcompleters;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.models.Skill;
import com.seasonsofconflict.models.SkillTree;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for /soc admin command
 */
public class AdminCommandTabCompleter implements TabCompleter {

    private final SeasonsOfConflict plugin;

    public AdminCommandTabCompleter(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("soc.admin")) {
            return completions;
        }

        // First argument - main subcommands
        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                "setseason", "setcycle", "setpoints", "apocalypse", "revive",
                "eliminate", "territories", "teams", "gameinfo", "event", "skills"
            ));
            return filterCompletions(completions, args[0]);
        }

        // Second argument - context-specific
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "setseason":
                    completions.addAll(Arrays.stream(Season.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
                    break;

                case "setcycle":
                    for (int i = 1; i <= 7; i++) {
                        completions.add(String.valueOf(i));
                    }
                    break;

                case "setpoints":
                    completions.addAll(getTeamNames());
                    break;

                case "apocalypse":
                    completions.addAll(Arrays.asList("on", "off", "start", "stop"));
                    break;

                case "revive":
                case "eliminate":
                    completions.addAll(getPlayerNames());
                    break;

                case "event":
                    completions.addAll(Arrays.asList("trigger", "stop", "list", "info"));
                    break;

                case "skills":
                    completions.addAll(Arrays.asList(
                        "give", "set", "reset", "unlock", "info",
                        "clearall", "givexp", "reload"
                    ));
                    break;
            }
            return filterCompletions(completions, args[1]);
        }

        // Third argument
        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "setpoints":
                    // After team name, suggest point amounts
                    completions.addAll(Arrays.asList("100", "500", "1000", "2000", "5000"));
                    break;

                case "event":
                    if (args[1].equalsIgnoreCase("trigger") || args[1].equalsIgnoreCase("info")) {
                        completions.addAll(Arrays.asList(
                            "blood_moon", "meteor_shower", "aurora", "fog", "heatwave"
                        ));
                    }
                    break;

                case "skills":
                    switch (args[1].toLowerCase()) {
                        case "give":
                        case "set":
                        case "reset":
                        case "unlock":
                        case "info":
                        case "givexp":
                            completions.addAll(getPlayerNames());
                            break;
                    }
                    break;
            }
            return filterCompletions(completions, args[2]);
        }

        // Fourth argument
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("skills")) {
                switch (args[1].toLowerCase()) {
                    case "give":
                    case "set":
                        // Skill point amounts
                        completions.addAll(Arrays.asList("1", "5", "10", "25", "50", "100"));
                        break;

                    case "reset":
                        // Tree names
                        completions.addAll(Arrays.stream(SkillTree.values())
                            .map(Enum::name)
                            .collect(Collectors.toList()));
                        break;

                    case "unlock":
                        // All skill internal names
                        completions.addAll(Arrays.stream(Skill.values())
                            .map(Skill::getInternalName)
                            .collect(Collectors.toList()));
                        break;

                    case "givexp":
                        // XP amounts
                        completions.addAll(Arrays.asList("100", "500", "1000", "2500", "5000"));
                        break;
                }
            }
            return filterCompletions(completions, args[3]);
        }

        return completions;
    }

    /**
     * Get list of online player names
     */
    private List<String> getPlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
    }

    /**
     * Get list of team names
     */
    private List<String> getTeamNames() {
        List<String> teams = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            var team = plugin.getTeamManager().getTeam(i);
            if (team != null) {
                teams.add(team.getName());
                teams.add(String.valueOf(i)); // Also add team ID
            }
        }
        return teams;
    }

    /**
     * Filter completions based on what the user has typed
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }
}
