package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Command to give players a tracking compass
 */
public class CompassCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public CompassCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if player already has a compass
        if (player.getInventory().contains(Material.COMPASS)) {
            MessageUtils.sendMessage(player, "&cYou already have a tracking compass!");
            return true;
        }

        // Create tracking compass
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Tracking Compass");
            meta.setLore(Arrays.asList(
                "§7Right-click to cycle tracking modes:",
                "§e• Home Territory",
                "§e• Nearest Enemy Territory",
                "§e• Nearest Teammate"
            ));
            compass.setItemMeta(meta);
        }

        // Give compass to player
        player.getInventory().addItem(compass);
        MessageUtils.sendMessage(player, "&aYou received a Tracking Compass!");
        MessageUtils.sendMessage(player, "&7Right-click to cycle tracking modes");

        return true;
    }
}
