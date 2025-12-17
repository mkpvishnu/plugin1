package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, Long> combatTimers;

    public CombatManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.combatTimers = new HashMap<>();
    }

    /**
     * Handle player kill - award points, update bounties, check cooldowns
     */
    public void handlePlayerKill(Player killer, Player victim) {
        if (killer == null || victim == null) return;
        if (killer.equals(victim)) return; // Suicide - no rewards

        PlayerData killerData = plugin.getGameManager().getPlayerData(killer);
        PlayerData victimData = plugin.getGameManager().getPlayerData(victim);

        TeamData killerTeam = plugin.getTeamManager().getTeam(killer);
        TeamData victimTeam = plugin.getTeamManager().getTeam(victim);

        // Prevent same-team kills from giving rewards
        if (killerTeam != null && victimTeam != null && killerTeam.getTeamId() == victimTeam.getTeamId()) {
            MessageUtils.sendError(killer, "You killed a teammate! No rewards given.");
            return;
        }

        // Check kill cooldown (prevent farming same player)
        if (killerData.hasKillCooldown(victim.getUniqueId())) {
            MessageUtils.sendMessage(killer, "&7You are on cooldown for killing " + victim.getName());
            return;
        }

        // Calculate kill reward
        int baseReward = plugin.getConfig().getInt("pvp.kill_reward_base", 50);
        int bountyReward = victimData.getBounty();
        int totalReward = baseReward + bountyReward;

        // Award quest points to killer's team
        if (killerTeam != null) {
            killerTeam.addPoints(totalReward);
            plugin.getTeamManager().saveTeam(killerTeam);
        }

        // Update killer stats
        killerData.incrementTotalKills();
        killerData.incrementKillStreak();
        killerData.addKillCooldown(victim.getUniqueId());

        // Update killer bounty
        updateBounty(killerData);

        // Save killer data
        plugin.getGameManager().savePlayerData(killer.getUniqueId());

        // Victim stats are updated in HealthManager.handleDeath()

        // Notify killer
        MessageUtils.sendSuccess(killer, "You killed " + victim.getName() + "!");
        MessageUtils.sendMessage(killer, "&a+" + totalReward + " quest points for your team");
        if (bountyReward > 0) {
            MessageUtils.sendMessage(killer, "&6+" + bountyReward + " bonus from bounty");
        }
        if (killerData.getKillStreak() >= 3) {
            MessageUtils.sendMessage(killer, "&e&lKILL STREAK: " + killerData.getKillStreak());
        }

        // Notify teams
        String killerName = killerTeam != null ? killerTeam.getColoredName() + " " + killer.getName() : killer.getName();
        String victimName = victimTeam != null ? victimTeam.getColoredName() + " " + victim.getName() : victim.getName();
        MessageUtils.broadcast("&c" + killerName + " &7killed &c" + victimName);

        if (killerData.getBounty() > 0) {
            MessageUtils.broadcast("&6" + killer.getName() + " now has a bounty of " + killerData.getBounty() + " points!");
        }

        plugin.getLogger().info(killer.getName() + " killed " + victim.getName() +
                               " (Reward: " + totalReward + " points, Killer streak: " + killerData.getKillStreak() + ")");
    }

    /**
     * Update player bounty based on kill streak
     */
    private void updateBounty(PlayerData playerData) {
        int killStreak = playerData.getKillStreak();
        int bountyThreshold = plugin.getConfig().getInt("pvp.bounty_threshold", 3);

        // No bounty below threshold
        if (killStreak < bountyThreshold) {
            playerData.setBounty(0);
            return;
        }

        // Get bounty levels from config
        // bounty_levels:
        //   3: 25
        //   5: 50
        //   10: 100
        int bounty = 0;
        for (String key : plugin.getConfig().getConfigurationSection("pvp.bounty_levels").getKeys(false)) {
            int threshold = Integer.parseInt(key);
            int amount = plugin.getConfig().getInt("pvp.bounty_levels." + key);

            if (killStreak >= threshold) {
                bounty = amount;
            }
        }

        playerData.setBounty(bounty);
    }

    /**
     * Check if player is in combat (within last 10 seconds)
     */
    public boolean isInCombat(Player player) {
        if (player == null) return false;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        return playerData.isInCombat();
    }

    /**
     * Set player as in combat (called when dealing or taking damage)
     */
    public void setInCombat(Player player) {
        if (player == null) return;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        long now = System.currentTimeMillis();
        playerData.setLastCombatTime(now);

        // Track in local map for quick access
        combatTimers.put(player.getUniqueId(), now);

        // Check if player has spawn protection - remove it when entering combat
        if (plugin.getHealthManager().hasSpawnProtection(player)) {
            plugin.getHealthManager().removeSpawnProtection(player);
        }
    }

    /**
     * Handle combat logging - kill player who logged out during combat
     */
    public void handleCombatLog(Player player) {
        if (player == null) return;

        if (!isInCombat(player)) return;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);

        // Mark player as dead (same as death by PvP)
        playerData.setAlive(false);
        playerData.incrementTotalDeaths();
        playerData.setKillStreak(0);
        playerData.setBounty(0);
        plugin.getGameManager().savePlayerData(player.getUniqueId());

        // Broadcast combat log
        TeamData team = plugin.getTeamManager().getTeam(player);
        String playerName = team != null ? team.getColoredName() + " " + player.getName() : player.getName();
        MessageUtils.broadcast("&c" + playerName + " &7logged out during combat and died!");

        // Check team elimination
        if (team != null) {
            plugin.getTeamManager().checkTeamElimination(team.getTeamId());
        }

        plugin.getLogger().info(player.getName() + " combat logged and died");

        // Clear combat timer
        combatTimers.remove(player.getUniqueId());
    }

    /**
     * Clear combat status for a player
     */
    public void clearCombat(Player player) {
        if (player == null) return;

        combatTimers.remove(player.getUniqueId());
    }

    /**
     * Get remaining combat time in seconds
     */
    public int getCombatTimeRemaining(Player player) {
        if (player == null) return 0;
        if (!isInCombat(player)) return 0;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        long lastCombatTime = playerData.getLastCombatTime();
        long combatThreshold = plugin.getConfig().getInt("player.combat_log_threshold_seconds", 10) * 1000L;

        long elapsed = System.currentTimeMillis() - lastCombatTime;
        long remaining = combatThreshold - elapsed;

        return (int) Math.max(0, remaining / 1000);
    }

    /**
     * Check if two players are on the same team
     */
    public boolean isSameTeam(Player player1, Player player2) {
        if (player1 == null || player2 == null) return false;

        PlayerData data1 = plugin.getGameManager().getPlayerData(player1);
        PlayerData data2 = plugin.getGameManager().getPlayerData(player2);

        return data1.getTeamId() != 0 && data1.getTeamId() == data2.getTeamId();
    }

    /**
     * Check if player can attack another player (not same team, victim not protected)
     */
    public boolean canAttack(Player attacker, Player victim) {
        if (attacker == null || victim == null) return false;
        if (attacker.equals(victim)) return true; // Allow self-damage

        // Check if same team
        if (isSameTeam(attacker, victim)) {
            return false; // Prevent friendly fire
        }

        // Check if victim has spawn protection
        if (plugin.getHealthManager().hasSpawnProtection(victim)) {
            MessageUtils.sendMessage(attacker, "&c" + victim.getName() + " has spawn protection!");
            return false;
        }

        return true;
    }

    /**
     * Get player's current kill cooldowns
     */
    public Map<UUID, Long> getKillCooldowns(Player player) {
        if (player == null) return new HashMap<>();

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        // Note: PlayerData doesn't expose the kill cooldowns map directly
        // This is intentional for encapsulation. If needed, add a getter to PlayerData.
        return new HashMap<>();
    }

    /**
     * Clear all combat timers (used on server shutdown)
     */
    public void clearAllCombatTimers() {
        combatTimers.clear();
    }

    /**
     * Check if attacker has kill cooldown for victim
     */
    public boolean hasKillCooldown(Player attacker, Player victim) {
        if (attacker == null || victim == null) return false;

        PlayerData attackerData = plugin.getGameManager().getPlayerData(attacker);
        return attackerData.hasKillCooldown(victim.getUniqueId());
    }

    /**
     * Get time remaining on kill cooldown for a specific victim
     */
    public long getKillCooldownRemaining(Player attacker, Player victim) {
        if (attacker == null || victim == null) return 0;

        PlayerData attackerData = plugin.getGameManager().getPlayerData(attacker);

        // This would require exposing the cooldown map from PlayerData
        // For now, we can only check if there IS a cooldown, not how long
        if (attackerData.hasKillCooldown(victim.getUniqueId())) {
            // Return max cooldown time (we don't know the exact remaining time without exposing the map)
            return plugin.getConfig().getInt("pvp.kill_cooldown_hours", 12);
        }

        return 0;
    }

    /**
     * Award assist points (if needed for future implementation)
     */
    public void handleAssist(Player assister, Player killer, Player victim) {
        // Future implementation: track damage dealt and award assist points
        // For now, only direct kills are rewarded
    }

    /**
     * Reset combat timers for a player (used when respawning)
     */
    public void resetCombatTimer(Player player) {
        if (player == null) return;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        playerData.setLastCombatTime(0);
        combatTimers.remove(player.getUniqueId());
    }
}
