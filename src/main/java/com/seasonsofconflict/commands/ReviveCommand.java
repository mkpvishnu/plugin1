package com.seasonsofconflict.commands;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerData;
import com.seasonsofconflict.models.TeamData;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReviveCommand implements CommandExecutor {

    private final SeasonsOfConflict plugin;

    public ReviveCommand(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            MessageUtils.sendError(player, "Usage: /revive <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtils.sendError(player, "Player not found!");
            return true;
        }

        PlayerData playerData = plugin.getGameManager().getPlayerData(player);
        PlayerData targetData = plugin.getGameManager().getPlayerData(target);

        if (playerData.getTeamId() != targetData.getTeamId()) {
            MessageUtils.sendError(player, "You can only revive teammates!");
            return true;
        }

        if (targetData.isAlive()) {
            MessageUtils.sendError(player, "That player is already alive!");
            return true;
        }

        TeamData team = plugin.getTeamManager().getTeam(playerData.getTeamId());
        int baseCost = plugin.getGameManager().getGameState().getRevivalCost();

        // Combat Medic: -25% revival cost
        double finalCost = plugin.getSkillEffectManager().applyCombatMedicDiscount(player, baseCost);
        int cost = (int) Math.ceil(finalCost);

        boolean hasCombatMedic = plugin.getSkillEffectManager().hasSkillByName(player, "combat_medic");

        if (team.getQuestPoints() < cost) {
            MessageUtils.sendError(player, "Not enough points! Need " + cost + ", have " + team.getQuestPoints());
            return true;
        }

        if (targetData.getRevivalsUsed() >= 2) {
            MessageUtils.sendError(player, "That player has used all their revivals this cycle!");
            return true;
        }

        plugin.getHealthManager().revivePlayer(target, team);

        // Combat Medic: Grant +5 min damage resistance to revived player
        if (hasCombatMedic) {
            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE,
                6000, // 5 minutes = 300 seconds = 6000 ticks
                1,    // Level II (+25% damage resistance)
                false,
                true,
                true
            ));

            // Visual: Protection particles
            target.getWorld().spawnParticle(
                org.bukkit.Particle.TOTEM,
                target.getLocation().add(0, 1, 0),
                30,
                0.5, 1.0, 0.5,
                0.1
            );

            MessageUtils.sendMessage(target,
                "&a&l✚ &aCombat Medic Protection! &7+5 min damage resistance");
            MessageUtils.sendSuccess(player, "Revived " + target.getName() + " for " + cost + " points (Combat Medic: -25%)!");
        } else {
            MessageUtils.sendSuccess(player, "Revived " + target.getName() + " for " + cost + " points!");
        }

        return true;
    }
}
