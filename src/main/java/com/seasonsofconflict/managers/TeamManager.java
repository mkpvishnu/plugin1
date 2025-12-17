package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    private final SeasonsOfConflict plugin;
    private final Map<Integer, TeamData> teams;
    private final Map<Integer, Integer> teamPlayerCounts;

    public TeamManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.teamPlayerCounts = new HashMap<>();
    }

    /**
     * Load all teams from database or initialize them from config
     */
    public void loadTeams() {
        // Try loading from database first
        List<TeamData> loadedTeams = plugin.getDataManager().loadAllTeams();

        if (loadedTeams.isEmpty()) {
            // Initialize teams from config
            initializeTeamsFromConfig();
        } else {
            // Load existing teams
            for (TeamData team : loadedTeams) {
                teams.put(team.getTeamId(), team);
            }
            plugin.getLogger().info("Loaded " + teams.size() + " teams from database");
        }

        // Update player counts
        updateTeamPlayerCounts();
    }

    /**
     * Initialize teams from config.yml
     */
    private void initializeTeamsFromConfig() {
        for (int teamId = 1; teamId <= 5; teamId++) {
            String basePath = "teams." + teamId + ".";
            String name = plugin.getConfig().getString(basePath + "name");
            String colorStr = plugin.getConfig().getString(basePath + "color");
            ChatColor color = ChatColor.valueOf(colorStr);

            // Home territory matches team ID (1-5)
            int homeTerritory = teamId;

            TeamData team = new TeamData(teamId, name, color, homeTerritory);
            teams.put(teamId, team);

            // Save to database
            plugin.getDataManager().saveTeam(team);
        }
        plugin.getLogger().info("Initialized " + teams.size() + " teams from config");
    }

    /**
     * Assign a player to the smallest team (round-robin)
     */
    public void assignPlayerToTeam(Player player) {
        if (player == null) return;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);

        // Check if player already has a team
        if (playerData.getTeamId() != 0) {
            TeamData existingTeam = getTeam(playerData.getTeamId());
            if (existingTeam != null) {
                MessageUtils.sendMessage(player, "&eYou are already on team " + existingTeam.getColoredName());
                return;
            }
        }

        // Find team with smallest player count (excluding eliminated teams)
        TeamData smallestTeam = null;
        int smallestCount = Integer.MAX_VALUE;

        for (TeamData team : teams.values()) {
            if (team.isEliminated()) continue;

            int count = teamPlayerCounts.getOrDefault(team.getTeamId(), 0);
            if (count < smallestCount) {
                smallestCount = count;
                smallestTeam = team;
            }
        }

        if (smallestTeam == null) {
            MessageUtils.sendError(player, "No available teams!");
            return;
        }

        // Assign player to team
        playerData.setTeamId(smallestTeam.getTeamId());
        playerData.setAlive(true);
        teamPlayerCounts.put(smallestTeam.getTeamId(), smallestCount + 1);

        // Save player data
        plugin.getGameManager().savePlayerData(player.getUniqueId());

        // Notify player
        MessageUtils.sendSuccess(player, "You have been assigned to " + smallestTeam.getColoredName());
        plugin.getLogger().info(player.getName() + " assigned to team " + smallestTeam.getName() +
                               " (Team size: " + (smallestCount + 1) + ")");
    }

    /**
     * Get team by team ID
     */
    public TeamData getTeam(int teamId) {
        return teams.get(teamId);
    }

    /**
     * Get team by player
     */
    public TeamData getTeam(Player player) {
        if (player == null) return null;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        return getTeam(playerData.getTeamId());
    }

    /**
     * Get all teams
     */
    public Collection<TeamData> getAllTeams() {
        return teams.values();
    }

    /**
     * Count non-eliminated teams
     */
    public int getRemainingTeamCount() {
        int count = 0;
        for (TeamData team : teams.values()) {
            if (!team.isEliminated()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the last remaining team (winner)
     */
    public TeamData getLastRemainingTeam() {
        for (TeamData team : teams.values()) {
            if (!team.isEliminated()) {
                return team;
            }
        }
        return null;
    }

    /**
     * Eliminate a team - mark as eliminated and release territories
     */
    public void eliminateTeam(int teamId) {
        TeamData team = getTeam(teamId);
        if (team == null) {
            plugin.getLogger().warning("Attempted to eliminate non-existent team: " + teamId);
            return;
        }

        if (team.isEliminated()) {
            plugin.getLogger().warning("Team " + team.getName() + " is already eliminated");
            return;
        }

        // Mark team as eliminated
        team.setEliminated(true);
        saveTeam(team);

        // Release all controlled territories
        TerritoryManager territoryManager = plugin.getTerritoryManager();
        List<Integer> territoriesLost = new ArrayList<>(team.getControlledTerritories());

        for (int territoryId : territoriesLost) {
            TerritoryData territory = territoryManager.getTerritory(territoryId);
            if (territory != null && territory.getOwnerTeamId() == teamId) {
                // Make territory neutral
                territory.setOwnerTeamId(0);
                territory.setCapturingTeamId(0);
                territory.setCaptureProgress(0);
                territoryManager.saveTerritory(territory);

                // Update beacon visual
                territoryManager.updateBeaconVisual(territory);
            }
        }

        // Clear team's controlled territories list
        team.getControlledTerritories().clear();

        // Broadcast elimination
        String message = plugin.getConfig().getString("messages.team_eliminated", "&c{team} has been ELIMINATED!");
        message = message.replace("{team}", team.getColoredName());
        MessageUtils.broadcastRaw(message);

        plugin.getLogger().info("Team " + team.getName() + " has been eliminated!");

        // Check win condition
        plugin.getGameManager().checkWinCondition();
    }

    /**
     * Check if a team should be eliminated (all players dead)
     */
    public void checkTeamElimination(int teamId) {
        TeamData team = getTeam(teamId);
        if (team == null || team.isEliminated()) return;

        // Count alive players on this team
        int aliveCount = 0;
        for (PlayerData playerData : plugin.getGameManager().getAllPlayerData().values()) {
            if (playerData.getTeamId() == teamId && playerData.isAlive()) {
                aliveCount++;
            }
        }

        // If no alive players, eliminate the team
        if (aliveCount == 0) {
            eliminateTeam(teamId);
        }
    }

    /**
     * Save team data to database
     */
    public void saveTeam(TeamData team) {
        if (team != null) {
            plugin.getDataManager().saveTeam(team);
        }
    }

    /**
     * Save all teams to database
     */
    public void saveAllTeams() {
        for (TeamData team : teams.values()) {
            saveTeam(team);
        }
    }

    /**
     * Update player counts for each team (for load balancing)
     */
    private void updateTeamPlayerCounts() {
        // Reset counts
        teamPlayerCounts.clear();
        for (int teamId : teams.keySet()) {
            teamPlayerCounts.put(teamId, 0);
        }

        // Count players in each team
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = plugin.getGameManager().getPlayerData(player);
            int teamId = playerData.getTeamId();
            if (teamId != 0) {
                teamPlayerCounts.put(teamId, teamPlayerCounts.getOrDefault(teamId, 0) + 1);
            }
        }
    }

    /**
     * Get number of players on a team
     */
    public int getTeamPlayerCount(int teamId) {
        int count = 0;
        for (PlayerData playerData : plugin.getGameManager().getAllPlayerData().values()) {
            if (playerData.getTeamId() == teamId) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get number of alive players on a team
     */
    public int getTeamAliveCount(int teamId) {
        int count = 0;
        for (PlayerData playerData : plugin.getGameManager().getAllPlayerData().values()) {
            if (playerData.getTeamId() == teamId && playerData.isAlive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get all alive players on a team
     */
    public List<Player> getTeamAlivePlayers(int teamId) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = plugin.getGameManager().getPlayerData(player);
            if (playerData.getTeamId() == teamId && playerData.isAlive()) {
                players.add(player);
            }
        }
        return players;
    }
}
