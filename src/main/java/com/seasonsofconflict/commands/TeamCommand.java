package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public TeamCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData data = plugin.getGameManager().getPlayerData(player);
        TeamData team = plugin.getTeamManager().getTeam(data.getTeamId());

        if (team == null) {
            MessageUtils.sendError(player, "You are not on a team!");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            MessageUtils.sendMessage(player, "&6=== Team Info ===");
            MessageUtils.sendMessage(player, "&eTeam: " + team.getColoredName());
            MessageUtils.sendMessage(player, "&ePoints: &f" + team.getQuestPoints());
            MessageUtils.sendMessage(player, "&eMembers: &f" + plugin.getTeamManager().getAlivePlayerCount(team.getTeamId()) +
                "/" + plugin.getTeamManager().getPlayerCount(team.getTeamId()) + " alive");
            MessageUtils.sendMessage(player, "&eTerritories: &f" + team.getControlledTerritories().size());
        } else if (args[0].equalsIgnoreCase("list")) {
            MessageUtils.sendMessage(player, "&6=== Teams ===");
            for (int i = 1; i <= 5; i++) {
                TeamData t = plugin.getTeamManager().getTeam(i);
                if (t != null) {
                    String status = t.isEliminated() ? "&c[ELIMINATED]" : "&a[ACTIVE]";
                    MessageUtils.sendMessage(player, status + " " + t.getColoredName() + " &7- " + 
                        t.getQuestPoints() + " pts, " + 
                        plugin.getTeamManager().getAlivePlayerCount(i) + " alive");
                }
            }
        }

        return true;
    }
}
