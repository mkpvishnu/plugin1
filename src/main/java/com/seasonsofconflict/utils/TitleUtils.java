package com.seasonsofconflict.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class for sending title messages to players
 */
public class TitleUtils {

    /**
     * Send a title to all online players
     * @param title Main title text (supports & color codes)
     * @param subtitle Subtitle text (supports & color codes)
     * @param fadeIn Fade in time in ticks (20 ticks = 1 second)
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     */
    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        String coloredTitle = ChatColor.translateAlternateColorCodes('&', title);
        String coloredSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(coloredTitle, coloredSubtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * Send a title to a specific player
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        String coloredTitle = ChatColor.translateAlternateColorCodes('&', title);
        String coloredSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

        player.sendTitle(coloredTitle, coloredSubtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Send a quick title (1s fade in, 3s stay, 1s fade out)
     */
    public static void broadcastQuickTitle(String title, String subtitle) {
        broadcastTitle(title, subtitle, 20, 60, 20);
    }

    /**
     * Send a dramatic title (1s fade in, 5s stay, 2s fade out)
     */
    public static void broadcastDramaticTitle(String title, String subtitle) {
        broadcastTitle(title, subtitle, 20, 100, 40);
    }

    /**
     * Clear titles for all players
     */
    public static void clearTitles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.resetTitle();
        }
    }

    /**
     * Send team elimination announcement with remaining teams count
     */
    public static void announceTeamElimination(String teamName, int teamsRemaining) {
        broadcastDramaticTitle(
            "&c&l" + teamName + " ELIMINATED!",
            "&7" + teamsRemaining + " team" + (teamsRemaining == 1 ? "" : "s") + " remaining"
        );
    }

    /**
     * Send territory capture announcement
     */
    public static void announceTerritoryCapture(String teamName, String territoryName) {
        broadcastQuickTitle(
            "&a&lTERRITORY CAPTURED!",
            teamName + " &7took &e" + territoryName
        );
    }

    /**
     * Send season change announcement
     */
    public static void announceSeasonChange(String seasonName, String seasonDescription) {
        broadcastDramaticTitle(
            "&6&l" + seasonName.toUpperCase(),
            "&e" + seasonDescription
        );
    }

    /**
     * Send cycle advancement announcement
     */
    public static void announceCycleAdvance(int cycleNumber, String description) {
        broadcastDramaticTitle(
            "&c&lCYCLE " + cycleNumber,
            "&7" + description
        );
    }

    /**
     * Send apocalypse start announcement
     */
    public static void announceApocalypse() {
        broadcastTitle(
            "&4&l&k|||&r &4&lAPOCALYPSE&r &4&l&k|||",
            "&c&lThe world is ending...",
            30, 120, 40  // 1.5s fade in, 6s stay, 2s fade out
        );
    }

    /**
     * Send game victory announcement
     */
    public static void announceVictory(String teamName) {
        broadcastTitle(
            "&6&l" + teamName + " WINS!",
            "&e&lVictory Royale!",
            20, 160, 40  // 1s fade in, 8s stay, 2s fade out
        );
    }

    /**
     * Send game start/goal announcement
     */
    public static void announceGameGoal() {
        broadcastTitle(
            "&6&lSEASONS OF CONFLICT",
            "&e&lBe the last team standing!",
            40, 80, 40  // 2s fade in, 4s stay, 2s fade out
        );
    }
}
