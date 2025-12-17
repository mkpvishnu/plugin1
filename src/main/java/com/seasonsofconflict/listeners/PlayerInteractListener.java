package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerInteractListener implements Listener {

    private final SeasonsOfConflict plugin;

    public PlayerInteractListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        
        if (type == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);
            MessageUtils.sendError(event.getPlayer(), "Enchanting is disabled in this world.");
        } else if (type == Material.ENDER_CHEST) {
            event.setCancelled(true);
            MessageUtils.sendError(event.getPlayer(), "Ender chests are disabled in this world.");
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
        MessageUtils.sendError(event.getPlayer(), "Dimensional travel is disabled.");
    }
}
