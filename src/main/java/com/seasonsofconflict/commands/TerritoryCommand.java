package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TerritoryCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public TerritoryCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            TerritoryData territory = plugin.getTerritoryManager().getTerritoryAt(player.getLocation());
            
            if (territory == null) {
                MessageUtils.sendMessage(player, "&7You are not in any territory.");
                return true;
            }

            MessageUtils.sendMessage(player, "&6=== Territory Info ===");
            MessageUtils.sendMessage(player, "&eName: &f" + territory.getName());
            
            if (territory.getOwnerTeamId() == 0) {
                MessageUtils.sendMessage(player, "&eOwner: &7Neutral");
            } else {
                MessageUtils.sendMessage(player, "&eOwner: " + 
                    plugin.getTeamManager().getTeam(territory.getOwnerTeamId()).getColoredName());
            }
            
            MessageUtils.sendMessage(player, "&eBonus: &f+" + territory.getBaseBonusPercent() + "% " + 
                territory.getBonusType());
        } else if (args[0].equalsIgnoreCase("map")) {
            MessageUtils.sendMessage(player, "&6=== Territory Map ===");
            for (int i = 1; i <= 5; i++) {
                TerritoryData t = plugin.getTerritoryManager().getTerritory(i);
                if (t != null) {
                    String owner = t.getOwnerTeamId() == 0 ? "&7Neutral" : 
                        plugin.getTeamManager().getTeam(t.getOwnerTeamId()).getColoredName();
                    MessageUtils.sendMessage(player, "&f" + t.getName() + " &7- " + owner);
                }
            }
        }

        return true;
    }
}
