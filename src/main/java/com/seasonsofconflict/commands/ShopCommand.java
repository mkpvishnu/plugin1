package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.ShopItem;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

        // Display shop (default behavior)
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            displayShop(player);
            return true;
        }

        // Purchase item
        if (args[0].equalsIgnoreCase("buy")) {
            handlePurchase(player, args);
            return true;
        }

        // Invalid subcommand
        MessageUtils.sendError(player, "Usage: /shop [buy <item>]");
        return true;
    }

    private void displayShop(Player player) {
        int cost = plugin.getGameManager().getGameState().getRevivalCost();

        MessageUtils.sendMessage(player, "&6=== Team Shop ===");
        MessageUtils.sendMessage(player, "&e1. &fRevive Teammate - &a" + cost + " pts &7(/revive <player>)");
        MessageUtils.sendMessage(player, "&e2. &fTerritory Shield (24hr) - &a200 pts &7(Coming Soon)");
        MessageUtils.sendMessage(player, "&e3. &fTeam Buff: 2x Drops (1hr) - &a100 pts &7(Coming Soon)");
        MessageUtils.sendMessage(player, "&e4. &fGolden Apple x2 - &a150 pts &7(/shop buy apples)");
        MessageUtils.sendMessage(player, "&e5. &fIron Armor Set - &a200 pts &7(/shop buy armor)");
        MessageUtils.sendMessage(player, "&7Use &e/shop buy <item>&7 to purchase items");
    }

    private void handlePurchase(Player player, String[] args) {
        // Validate arguments
        if (args.length < 2) {
            MessageUtils.sendError(player, "Usage: /shop buy <item>");
            MessageUtils.sendMessage(player, "&7Available: &eapples&7, &earmor");
            return;
        }

        // Parse item
        ShopItem item = ShopItem.fromString(args[1]);
        if (item == null) {
            MessageUtils.sendError(player, "Unknown item: " + args[1]);
            MessageUtils.sendMessage(player, "&7Available: &eapples&7, &earmor");
            return;
        }

        // Get player's team
        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        TeamData team = plugin.getTeamManager().getTeam(playerData.getTeamId());

        if (team == null) {
            MessageUtils.sendError(player, "You must be on a team to use the shop!");
            return;
        }

        // Check if player is alive
        if (!playerData.isAlive()) {
            MessageUtils.sendError(player, "You must be alive to purchase items!");
            return;
        }

        // Check team points
        if (team.getQuestPoints() < item.getCost()) {
            MessageUtils.sendError(player, "Not enough points! Need " +
                item.getCost() + ", have " + team.getQuestPoints());
            return;
        }

        // Check inventory space
        ItemStack[] items = item.getItems();
        if (!hasInventorySpace(player, items)) {
            MessageUtils.sendError(player, "Not enough inventory space! Need " +
                items.length + " free slots.");
            return;
        }

        // Execute purchase
        team.subtractPoints(item.getCost());
        plugin.getTeamManager().saveTeam(team);

        // Give items
        for (ItemStack itemStack : items) {
            player.getInventory().addItem(itemStack);
        }

        // Confirmation messages
        MessageUtils.sendSuccess(player, "Purchased " + item.getDisplayName() +
            " for " + item.getCost() + " points!");
        MessageUtils.sendMessage(player, "&7Team points remaining: &e" + team.getQuestPoints());

        // Log purchase
        plugin.getLogger().info(player.getName() + " purchased " + item.getDisplayName() +
            " from shop for " + item.getCost() + " points (Team: " + team.getName() + ")");
    }

    private boolean hasInventorySpace(Player player, ItemStack[] items) {
        int requiredSlots = items.length;
        int emptySlots = 0;

        for (ItemStack slot : player.getInventory().getStorageContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        return emptySlots >= requiredSlots;
    }
}
