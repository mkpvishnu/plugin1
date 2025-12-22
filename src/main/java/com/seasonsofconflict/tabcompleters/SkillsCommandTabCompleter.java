package com.seasonsofconflict.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for /skills player command
 */
public class SkillsCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcommands for /skills
            completions.addAll(Arrays.asList(
                "stats",  // View skill statistics
                "xp",     // View XP progress
                "info",   // Detailed skill information
                "active", // List active skills (from design doc)
                "reset"   // Reset tree (via shop)
            ));
            return filterCompletions(completions, args[0]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }
}
