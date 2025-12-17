package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public ShopCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        int cost = plugin.getGameManager().getGameState().getRevivalCost();

        MessageUtils.sendMessage(player, "&6=== Team Shop ===");
        MessageUtils.sendMessage(player, "&e1. &fRevive Teammate - &a" + cost + " pts");
        MessageUtils.sendMessage(player, "&e2. &fTerritory Shield (24hr) - &a200 pts");
        MessageUtils.sendMessage(player, "&e3. &fTeam Buff: 2x Drops (1hr) - &a100 pts");
        MessageUtils.sendMessage(player, "&e4. &fGolden Apple x2 - &a150 pts");
        MessageUtils.sendMessage(player, "&e5. &fIron Armor Set - &a200 pts");
        MessageUtils.sendMessage(player, "&7Use /revive to revive teammates");

        return true;
    }
}
