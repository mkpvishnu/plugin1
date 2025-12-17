package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public LeaderboardCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String type = args.length > 0 ? args[0].toLowerCase() : "kills";

        if (type.equals("kills")) {
            MessageUtils.sendMessage(sender, "&6=== Top Kills ===");
            List<PlayerData> players = new ArrayList<>(plugin.getGameManager().getAllPlayerData().values());
            players.sort(Comparator.comparingInt(PlayerData::getTotalKills).reversed());
            
            int rank = 1;
            for (PlayerData data : players.subList(0, Math.min(10, players.size()))) {
                MessageUtils.sendMessage(sender, "&e" + rank + ". &f" + data.getName() + " &7- " + data.getTotalKills() + " kills");
                rank++;
            }
        } else if (type.equals("teams") || type.equals("points")) {
            MessageUtils.sendMessage(sender, "&6=== Team Rankings ===");
            for (int i = 1; i <= 5; i++) {
                TeamData team = plugin.getTeamManager().getTeam(i);
                if (team != null && !team.isEliminated()) {
                    MessageUtils.sendMessage(sender, team.getColoredName() + " &7- " + team.getQuestPoints() + " points, " +
                        plugin.getTeamManager().getAlivePlayerCount(i) + " alive");
                }
            }
        }

        return true;
    }
}
