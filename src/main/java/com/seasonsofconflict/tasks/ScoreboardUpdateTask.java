package com.seasonsofconflict.tasks;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.GameState;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.models.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class ScoreboardUpdateTask extends BukkitRunnable {

    private final SeasonsOfConflict plugin;

    public ScoreboardUpdateTask(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Update scoreboard for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player);
        }
    }

    private void updatePlayerScoreboard(Player player) {
        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        GameState gameState = plugin.getGameManager().getGameState();

        // Get or create scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = manager.getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        // Get or create objective
        Objective objective = scoreboard.getObjective("soc_main");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("soc_main", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Seasons of Conflict");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Clear old scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Get player info
        TeamData team = plugin.getTeamManager().getTeam(playerData.getTeamId());
        TerritoryData territory = plugin.getTerritoryManager().getTerritoryAt(player.getLocation());

        int line = 15;

        // Team info
        if (team != null) {
            objective.getScore(ChatColor.AQUA + "Team: " + team.getColoredName()).setScore(line--);
            objective.getScore(ChatColor.YELLOW + "Points: " + ChatColor.WHITE + team.getQuestPoints()).setScore(line--);
            int alive = plugin.getTeamManager().getAlivePlayerCount(team.getTeamId());
            int total = plugin.getTeamManager().getPlayerCount(team.getTeamId());
            objective.getScore(ChatColor.GREEN + "Alive: " + ChatColor.WHITE + alive + "/" + total).setScore(line--);
        }

        objective.getScore("").setScore(line--); // Blank line

        // Location info
        if (territory != null) {
            String ownerText;
            if (territory.getOwnerTeamId() == 0) {
                ownerText = ChatColor.GRAY + "Neutral";
            } else if (territory.getOwnerTeamId() == playerData.getTeamId()) {
                ownerText = ChatColor.GREEN + "Your Team";
            } else {
                TeamData ownerTeam = plugin.getTeamManager().getTeam(territory.getOwnerTeamId());
                ownerText = ChatColor.RED + ownerTeam.getName();
            }
            objective.getScore(ChatColor.GOLD + "Territory: " + ChatColor.WHITE + territory.getName()).setScore(line--);
            objective.getScore(ChatColor.GRAY + "Owner: " + ownerText).setScore(line--);
        } else {
            objective.getScore(ChatColor.GRAY + "Territory: " + ChatColor.WHITE + "Wilderness").setScore(line--);
        }

        objective.getScore(" ").setScore(line--); // Blank line

        // Game state
        objective.getScore(ChatColor.LIGHT_PURPLE + "Season: " + ChatColor.WHITE +
            formatSeason(gameState.getCurrentSeason().name())).setScore(line--);

        String cycleText = gameState.isApocalypse() ?
            ChatColor.DARK_RED + "APOCALYPSE" :
            ChatColor.WHITE + String.valueOf(gameState.getCurrentCycle());
        objective.getScore(ChatColor.RED + "Cycle: " + cycleText).setScore(line--);

        objective.getScore("  ").setScore(line--); // Blank line

        // Player status
        if (!playerData.isAlive()) {
            objective.getScore(ChatColor.DARK_RED + "Status: DEAD").setScore(line--);
            int revivalsUsed = playerData.getRevivalsUsed();
            int maxRevivals = plugin.getConfig().getInt("player.max_revivals_per_cycle", 2);
            objective.getScore(ChatColor.YELLOW + "Revivals: " + revivalsUsed + "/" + maxRevivals).setScore(line--);
        } else {
            if (playerData.getKillStreak() > 0) {
                objective.getScore(ChatColor.GOLD + "Streak: " + ChatColor.WHITE + playerData.getKillStreak()).setScore(line--);
            }
            if (playerData.getBounty() > 0) {
                objective.getScore(ChatColor.RED + "Bounty: " + ChatColor.WHITE + playerData.getBounty() + " pts").setScore(line--);
            }
        }
    }

    private String formatSeason(String season) {
        return season.substring(0, 1).toUpperCase() + season.substring(1).toLowerCase();
    }
}
