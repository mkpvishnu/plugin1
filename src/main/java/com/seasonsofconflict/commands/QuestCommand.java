package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.PlayerQuest;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public QuestCommand(SeasonsOfConflict plugin) {
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

        MessageUtils.sendMessage(player, "&6=== Your Quests ===");
        
        if (data.getActiveQuests().isEmpty()) {
            MessageUtils.sendMessage(player, "&7No active quests. Wait for daily reset.");
            return true;
        }

        for (PlayerQuest quest : data.getActiveQuests()) {
            String status = quest.isCompleted() ? "&a✓" : "&e⏳";
            String progress = quest.getProgressString();
            MessageUtils.sendMessage(player, status + " &f" + quest.getDescription() + 
                " &7(" + progress + ") &e+" + quest.getRewardPoints() + "pts");
        }

        return true;
    }
}
