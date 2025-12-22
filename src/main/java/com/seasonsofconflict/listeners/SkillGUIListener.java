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
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        // Check if this is a skill tree GUI (title-based or structure-based)
        if (!isSkillTreeGUI(title, inv)) {
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
        plugin.getSkillTreeGUI().handleClick(player, inv, event.getSlot(), event.getCurrentItem());
    }

    /**
     * Check if inventory title matches skill tree GUI format
     * Includes fallback structure-based detection
     */
    private boolean isSkillTreeGUI(String title, Inventory inv) {
        // Primary detection: Title-based matching
        String stripped = ChatColor.stripColor(title);
        if (stripped != null) {
            // Convert to lowercase for case-insensitive matching
            String lower = stripped.toLowerCase();

            // Check for all skill GUI variations
            if (lower.contains("skill trees") ||
                lower.contains("combat tree") ||
                lower.contains("gathering tree") ||
                lower.contains("survival tree") ||
                lower.contains("teamwork tree") ||
                lower.contains("confirm reset")) {
                return true;
            }
        }

        // Fallback detection: Structure-based matching
        // Skill GUIs have specific sizes: 45 (main menu), 54 (tree view), 27 (reset confirm)
        int size = inv.getSize();
        if (size == 45 || size == 54 || size == 27) {
            // Additional check: Look for characteristic items at known positions
            // Main menu has tree selector buttons at slots 11, 13, 15, 20, 22, 24
            // Tree view has back button at slot 45, reset at slot 53
            // Reset confirm has confirm/cancel buttons
            return hasSkillGUIStructure(inv, size);
        }

        return false;
    }

    /**
     * Check if inventory has the characteristic structure of a skill GUI
     */
    private boolean hasSkillGUIStructure(Inventory inv, int size) {
        // Check for skill GUI indicators based on size
        if (size == 45) {
            // Main menu: Has items at tree selector positions (11, 13, 15, 20, 22, 24)
            return inv.getItem(11) != null || inv.getItem(13) != null ||
                   inv.getItem(15) != null || inv.getItem(20) != null;
        } else if (size == 54) {
            // Tree view: Has back button at 45 or reset button at 53
            return inv.getItem(45) != null || inv.getItem(53) != null;
        } else if (size == 27) {
            // Reset confirmation: Has confirm/cancel buttons
            return inv.getItem(11) != null || inv.getItem(15) != null;
        }
        return false;
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
        Inventory inv = event.getInventory();

        // Optional: Add any cleanup logic when player closes skill GUI
        if (isSkillTreeGUI(title, inv)) {
            // Future: Could track GUI sessions, log analytics, etc.
        }
    }
}
