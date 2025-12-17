package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.models.TerritoryData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealthManager {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, Long> spawnProtection;

    public HealthManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.spawnProtection = new HashMap<>();
    }

    /**
     * Set player to maximum health (40 hearts = 80 HP)
     */
    public void setMaxHealth(Player player) {
        if (player == null) return;

        double maxHealth = plugin.getConfig().getDouble("player.max_health", 80.0);

        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(maxHealth);
            player.setHealth(maxHealth);
        }
    }

    /**
     * Revive a player - teleport to team beacon, give spawn protection, restore to life
     */
    public void revivePlayer(Player player, TeamData team) {
        if (player == null || team == null) {
            plugin.getLogger().warning("Cannot revive player: player or team is null");
            return;
        }

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);

        // Check if player is alive already
        if (playerData.isAlive()) {
            MessageUtils.sendError(player, "You are already alive!");
            return;
        }

        // Check revival limit
        int maxRevivals = plugin.getConfig().getInt("player.max_revivals_per_cycle", 2);
        if (playerData.getRevivalsUsed() >= maxRevivals) {
            MessageUtils.sendError(player, "You have used all your revivals for this cycle!");
            return;
        }

        // Check if team can afford revival
        int currentCycle = plugin.getGameManager().getGameState().getCurrentCycle();
        int revivalCost = getRevivalCost(currentCycle);

        if (team.getQuestPoints() < revivalCost) {
            MessageUtils.sendError(player, "Your team needs " + revivalCost + " quest points to revive you!");
            MessageUtils.sendError(player, "Current points: " + team.getQuestPoints());
            return;
        }

        // Get team's home territory beacon location
        TerritoryData homeTerritory = plugin.getTerritoryManager().getTerritory(team.getHomeTerritory());
        if (homeTerritory == null) {
            plugin.getLogger().severe("Team " + team.getName() + " has invalid home territory!");
            MessageUtils.sendError(player, "Your team's home territory is invalid!");
            return;
        }

        String worldName = plugin.getConfig().getString("game.world_name", "world");
        Location spawnLocation = homeTerritory.getBeaconLocation(worldName);

        // Adjust spawn location to be safe (above beacon, not inside it)
        spawnLocation.add(0, 2, 0);

        // Deduct quest points from team
        team.subtractPoints(revivalCost);
        plugin.getTeamManager().saveTeam(team);

        // Update player data
        playerData.setAlive(true);
        playerData.incrementRevivalsUsed();
        plugin.getGameManager().savePlayerData(player.getUniqueId());

        // Teleport player
        player.teleport(spawnLocation);

        // Set game mode to survival
        player.setGameMode(GameMode.SURVIVAL);

        // Restore health
        setMaxHealth(player);

        // Give spawn protection
        giveSpawnProtection(player);

        // Notify player
        MessageUtils.sendSuccess(player, "You have been revived! (" +
                                playerData.getRevivalsUsed() + "/" + maxRevivals + " revivals used)");
        MessageUtils.sendMessage(player, "&eYou have " + getSpawnProtectionSeconds() + " seconds of spawn protection");
        MessageUtils.sendMessage(player, "&7Revival cost: " + revivalCost + " quest points");

        // Notify team
        for (Player teamPlayer : plugin.getTeamManager().getTeamAlivePlayers(team.getTeamId())) {
            if (!teamPlayer.equals(player)) {
                MessageUtils.sendMessage(teamPlayer, "&a" + player.getName() + " has been revived!");
            }
        }

        plugin.getLogger().info(player.getName() + " was revived by team " + team.getName() +
                               " (Cost: " + revivalCost + " points)");
    }

    /**
     * Get revival cost for current cycle
     */
    private int getRevivalCost(int cycle) {
        String path = "difficulty.cycles." + cycle + ".revival_cost";
        return plugin.getConfig().getInt(path, 500);
    }

    /**
     * Give spawn protection to a player
     */
    private void giveSpawnProtection(Player player) {
        if (player == null) return;

        int protectionSeconds = getSpawnProtectionSeconds();
        long protectionExpiry = System.currentTimeMillis() + (protectionSeconds * 1000L);
        spawnProtection.put(player.getUniqueId(), protectionExpiry);

        // Give resistance and regeneration effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, protectionSeconds * 20, 4, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, protectionSeconds * 20, 2, false, true));

        // Schedule removal of protection
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            spawnProtection.remove(player.getUniqueId());
            if (player.isOnline()) {
                MessageUtils.sendMessage(player, "&cSpawn protection has expired!");
            }
        }, protectionSeconds * 20L);
    }

    /**
     * Get spawn protection duration in seconds
     */
    private int getSpawnProtectionSeconds() {
        return plugin.getConfig().getInt("player.spawn_protection_seconds", 60);
    }

    /**
     * Check if player has spawn protection
     */
    public boolean hasSpawnProtection(Player player) {
        if (player == null) return false;

        Long expiry = spawnProtection.get(player.getUniqueId());
        if (expiry == null) return false;

        long now = System.currentTimeMillis();
        if (now > expiry) {
            spawnProtection.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    /**
     * Handle player death - set to spectator, check team elimination
     */
    public void handleDeath(Player player) {
        if (player == null) return;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        TeamData team = plugin.getTeamManager().getTeam(player);

        // Mark player as dead
        playerData.setAlive(false);
        playerData.incrementTotalDeaths();
        playerData.setKillStreak(0);
        playerData.setBounty(0);
        plugin.getGameManager().savePlayerData(player.getUniqueId());

        // Set to spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        // Notify player
        int maxRevivals = plugin.getConfig().getInt("player.max_revivals_per_cycle", 2);
        int revivalsUsed = playerData.getRevivalsUsed();
        int revivalsLeft = maxRevivals - revivalsUsed;

        if (revivalsLeft > 0) {
            MessageUtils.sendMessage(player, "&cYou have died! Your team can revive you.");
            MessageUtils.sendMessage(player, "&7Revivals remaining: &e" + revivalsLeft + "&7/&e" + maxRevivals);

            if (team != null) {
                int currentCycle = plugin.getGameManager().getGameState().getCurrentCycle();
                int revivalCost = getRevivalCost(currentCycle);
                MessageUtils.sendMessage(player, "&7Revival cost: &e" + revivalCost + " &7quest points");
                MessageUtils.sendMessage(player, "&7Team points: &e" + team.getQuestPoints());

                if (team.getQuestPoints() >= revivalCost) {
                    MessageUtils.sendMessage(player, "&aUse &e/revive &ato respawn at your team's beacon");
                } else {
                    MessageUtils.sendMessage(player, "&cYour team needs more quest points to revive you!");
                }
            }
        } else {
            MessageUtils.sendMessage(player, "&cYou have died! You have no revivals remaining this cycle.");
            MessageUtils.sendMessage(player, "&7You will remain as a spectator until the next cycle or game end.");
        }

        // Check if team should be eliminated
        if (team != null) {
            plugin.getTeamManager().checkTeamElimination(team.getTeamId());
        }

        plugin.getLogger().info(player.getName() + " died. Revivals used: " + revivalsUsed + "/" + maxRevivals);
    }

    /**
     * Reset player death state (used when starting a new cycle)
     */
    public void resetPlayerForNewCycle(Player player) {
        if (player == null) return;

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);

        // Reset revival count
        playerData.setRevivalsUsed(0);

        // If player was dead, revive them automatically for new cycle
        if (!playerData.isAlive()) {
            TeamData team = plugin.getTeamManager().getTeam(player);
            if (team != null && !team.isEliminated()) {
                playerData.setAlive(true);

                // Teleport to team beacon
                TerritoryData homeTerritory = plugin.getTerritoryManager().getTerritory(team.getHomeTerritory());
                if (homeTerritory != null) {
                    String worldName = plugin.getConfig().getString("game.world_name", "world");
                    Location spawnLocation = homeTerritory.getBeaconLocation(worldName);
                    spawnLocation.add(0, 2, 0);
                    player.teleport(spawnLocation);
                }

                // Restore player state
                player.setGameMode(GameMode.SURVIVAL);
                setMaxHealth(player);
                giveSpawnProtection(player);

                MessageUtils.sendSuccess(player, "A new cycle begins! You have been restored to life.");
                MessageUtils.sendMessage(player, "&7You have " + plugin.getConfig().getInt("player.max_revivals_per_cycle", 2) +
                                       " revivals available this cycle.");
            }
        }

        plugin.getGameManager().savePlayerData(player.getUniqueId());
    }

    /**
     * Heal player (used for regeneration effects, not setting max health)
     */
    public void healPlayer(Player player, double amount) {
        if (player == null) return;

        double currentHealth = player.getHealth();
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) return;

        double maxHealth = healthAttribute.getValue();
        double newHealth = Math.min(currentHealth + amount, maxHealth);
        player.setHealth(newHealth);
    }

    /**
     * Remove spawn protection from player (e.g., when they attack)
     */
    public void removeSpawnProtection(Player player) {
        if (player == null) return;

        if (hasSpawnProtection(player)) {
            spawnProtection.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            MessageUtils.sendMessage(player, "&cSpawn protection removed!");
        }
    }

    /**
     * Get remaining spawn protection time in seconds
     */
    public int getSpawnProtectionRemaining(Player player) {
        if (player == null) return 0;

        Long expiry = spawnProtection.get(player.getUniqueId());
        if (expiry == null) return 0;

        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }
}
