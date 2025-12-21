package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Handles clicks in skill tree GUI inventories
 */
public class SkillGUIListener implements Listener {

    private final SeasonsOfConflict plugin;

    public SkillGUIListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle clicks in skill tree inventories
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        // Check if this is a skill tree GUI
        if (!isSkillTreeGUI(title)) {
            return;
        }

        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Ignore clicks outside the inventory
        if (event.getClickedInventory() == null) {
            return;
        }

        // Ignore clicks in player inventory
        if (event.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        // Handle the click
        plugin.getSkillTreeGUI().handleClick(player, event.getSlot(), title);
    }

    /**
     * Check if inventory title matches skill tree GUI format
     */
    private boolean isSkillTreeGUI(String title) {
        // Strip color codes for comparison
        String stripped = ChatColor.stripColor(title);

        return stripped != null && (
            stripped.contains("SKILL TREES") ||
            stripped.contains("Combat Tree") ||
            stripped.contains("Gathering Tree") ||
            stripped.contains("Survival Tree") ||
            stripped.contains("Teamwork Tree")
        );
    }

    /**
     * Handle inventory close (optional cleanup)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        // Optional: Add any cleanup logic when player closes skill GUI
        if (isSkillTreeGUI(title)) {
            // Future: Could track GUI sessions, log analytics, etc.
        }
    }
}
