package com.seasonsofconflict.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    private static String prefix = ChatColor.GOLD + "[SoC]" + ChatColor.RESET + " ";

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(prefix + ChatColor.RED + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(prefix + ChatColor.GREEN + message);
    }

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void broadcastRaw(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void setPrefix(String newPrefix) {
        prefix = ChatColor.translateAlternateColorCodes('&', newPrefix);
    }
}
