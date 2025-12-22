package com.seasonsofconflict.tabcompleters;

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
 * General tab completer for various player commands
 */
public class GeneralTabCompleter implements TabCompleter {

    private final String commandName;

    public GeneralTabCompleter(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (commandName.toLowerCase()) {
            case "quest":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("list", "progress"));
                }
                break;

            case "territory":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("info", "map", "capture"));
                }
                break;

            case "revive":
                if (args.length == 1) {
                    // Suggest online player names
                    completions.addAll(getPlayerNames());
                }
                break;

            case "shop":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("buy", "list"));
                }
                else if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
                    // Shop items
                    completions.addAll(Arrays.asList(
                        "golden_apple", "totem", "iron_armor", "arrows",
                        "ender_pearls", "territory_shield", "revival_token"
                    ));
                }
                break;

            case "stats":
                if (args.length == 1) {
                    // Optionally specify player name
                    completions.addAll(getPlayerNames());
                }
                break;

            case "leaderboard":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("kills", "points", "teams", "xp"));
                }
                break;
        }

        return filterCompletions(completions, args.length > 0 ? args[args.length - 1] : "");
    }

    private List<String> getPlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }
}
