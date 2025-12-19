package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.GameState;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.utils.MessageUtils;
import com.seasonsofconflict.utils.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final SeasonsOfConflict plugin;
    private GameState gameState;
    private final Map<UUID, PlayerData> playerDataMap;

    public GameManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
    }

    public void loadGameState() {
        gameState = plugin.getDataManager().loadGameState();
        plugin.getLogger().info("Loaded game state: Season " + gameState.getCurrentSeason() +
                                ", Cycle " + gameState.getCurrentCycle());

        // Apply difficulty for current cycle
        plugin.getDifficultyManager().applyCycleScaling(gameState.getCurrentCycle());
    }

    public void saveGameState() {
        if (gameState != null) {
            plugin.getDataManager().saveGameState(gameState);
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    // Player data management
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> {
            PlayerData data = plugin.getDataManager().loadPlayer(uuid);
            if (data == null) {
                Player player = Bukkit.getPlayer(uuid);
                data = new PlayerData(uuid, player != null ? player.getName() : "Unknown");
            }
            return data;
        });
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data != null) {
            plugin.getDataManager().savePlayer(data);
        }
    }

    public void saveAllPlayers() {
        for (PlayerData data : playerDataMap.values()) {
            plugin.getDataManager().savePlayer(data);
        }
    }

    public void removePlayerData(UUID uuid) {
        savePlayerData(uuid);
        playerDataMap.remove(uuid);
    }

    // Win condition check
    public void checkWinCondition() {
        int remainingTeams = plugin.getTeamManager().getRemainingTeamCount();

        if (remainingTeams <= 1) {
            TeamData winner = plugin.getTeamManager().getLastRemainingTeam();
            if (winner != null) {
                endGame(winner);
            }
        }
    }

    public void endGame(TeamData winner) {
        MessageUtils.broadcastRaw("&6&l========================================");
        MessageUtils.broadcastRaw("&e&l         GAME OVER");
        MessageUtils.broadcastRaw("");
        MessageUtils.broadcastRaw("&a" + winner.getColoredName() + " &aWINS!");
        MessageUtils.broadcastRaw("");
        MessageUtils.broadcastRaw("&7Congratulations to all team members!");
        MessageUtils.broadcastRaw("&6&l========================================");

        // Send dramatic victory title announcement
        TitleUtils.announceVictory(winner.getColoredName());

        // Schedule server restart or reset (optional)
        // Bukkit.getScheduler().runTaskLater(plugin, () -> {
        //     Bukkit.shutdown();
        // }, 20L * 60); // 1 minute
    }

    // Daily reset
    public void performDailyReset() {
        // Reset player daily stats
        for (PlayerData data : playerDataMap.values()) {
            data.resetDailyStats();
        }

        // Assign new daily quests
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getPlayerData(player).isAlive()) {
                plugin.getQuestManager().assignDailyQuests(player);
            }
        }

        MessageUtils.broadcast("&eA new day begins! Daily quests and limits have been reset.");
        saveAllPlayers();
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return playerDataMap;
    }
}
