package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public StatsCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Please specify a player.");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.sendError(sender, "Player not found!");
                return true;
            }
        }

        PlayerData data = plugin.getGameManager().getPlayerData(target);

        MessageUtils.sendMessage(sender, "&6=== Stats for " + target.getName() + " ===");
        MessageUtils.sendMessage(sender, "&eTeam: " + plugin.getTeamManager().getTeam(data.getTeamId()).getColoredName());
        MessageUtils.sendMessage(sender, "&eKills: &f" + data.getTotalKills());
        MessageUtils.sendMessage(sender, "&eDeaths: &f" + data.getTotalDeaths());
        MessageUtils.sendMessage(sender, "&eK/D: &f" + String.format("%.2f", 
            data.getTotalDeaths() > 0 ? (double) data.getTotalKills() / data.getTotalDeaths() : data.getTotalKills()));
        MessageUtils.sendMessage(sender, "&eKill Streak: &f" + data.getKillStreak());
        MessageUtils.sendMessage(sender, "&eBounty: &f" + data.getBounty());
        MessageUtils.sendMessage(sender, "&eRevivals Used: &f" + data.getRevivalsUsed() + "/2");

        return true;
    }
}
