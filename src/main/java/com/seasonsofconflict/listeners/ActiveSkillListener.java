package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.managers.CooldownManager;
import com.seasonsofconflict.managers.SkillEffectManager;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles active skill triggers and activations
 */
public class ActiveSkillListener implements Listener {

    private final SeasonsOfConflict plugin;
    private final SkillEffectManager effectManager;
    private final CooldownManager cooldownManager;

    // Track players primed to activate skills (sneaking + right-click pattern)
    private final Map<UUID, Long> skillActivationPrimed;

    // Track active skill effects with durations
    private final Map<UUID, Map<String, Long>> activeSkillEffects;

    public ActiveSkillListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.effectManager = plugin.getSkillEffectManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.skillActivationPrimed = new ConcurrentHashMap<>();
        this.activeSkillEffects = new ConcurrentHashMap<>();
    }

    /**
     * Handle skill activation via sneak + right-click
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player is sneaking to activate skills
        if (!player.isSneaking()) {
            return;
        }

        // Check which skill the player wants to activate based on held item
        ItemStack item = player.getInventory().getItemInMainHand();

        // Combat skills: Weapon in hand
        if (isWeapon(item.getType())) {
            tryActivateCombatSkill(player, item);
        }
        // Gathering skills: Tool in hand
        else if (isTool(item.getType())) {
            tryActivateGatheringSkill(player, item);
        }
        // Survival skills: Food or potion in hand
        else if (isFood(item.getType()) || item.getType() == Material.POTION) {
            tryActivateSurvivalSkill(player, item);
        }
        // Teamwork skills: Empty hand or banner
        else if (item.getType() == Material.AIR || item.getType().name().contains("BANNER")) {
            tryActivateTeamworkSkill(player);
        }
    }

    /**
     * Try to activate combat tree active skills
     */
    private void tryActivateCombatSkill(Player player, ItemStack weapon) {
        // Whirlwind Strike (Tier 3)
        if (effectManager.hasSkillByName(player, "whirlwind_strike")) {
            if (cooldownManager.tryActivateSkill(player, "whirlwind_strike", 60)) {
                activateWhirlwindStrike(player);
            }
            return;
        }

        // Note: Last Stand is passive-triggered, not manual
        // Warlord's Rampage (Ultimate)
        if (effectManager.hasSkillByName(player, "warlords_rampage")) {
            if (cooldownManager.tryActivateSkill(player, "warlords_rampage", 300)) {
                activateWarlordsRampage(player);
            }
            return;
        }
    }

    /**
     * Try to activate gathering tree active skills
     */
    private void tryActivateGatheringSkill(Player player, ItemStack tool) {
        // Vein Miner (Tier 2)
        if (effectManager.hasSkillByName(player, "vein_miner")) {
            if (cooldownManager.tryActivateSkill(player, "vein_miner", 30)) {
                primeVeinMiner(player);
            }
            return;
        }

        // Ore Transmutation (Tier 3)
        if (effectManager.hasSkillByName(player, "ore_transmutation")) {
            if (cooldownManager.tryActivateSkill(player, "ore_transmutation", 600)) {
                activateOreTransmutation(player);
            }
            return;
        }

        // Midas Touch (Ultimate)
        if (effectManager.hasSkillByName(player, "midas_touch")) {
            if (cooldownManager.tryActivateSkill(player, "midas_touch", 600)) {
                activateMidasTouch(player);
            }
            return;
        }
    }

    /**
     * Try to activate survival tree active skills
     */
    private void tryActivateSurvivalSkill(Player player, ItemStack item) {
        // Immortal Fortress (Ultimate)
        if (effectManager.hasSkillByName(player, "immortal_fortress")) {
            if (cooldownManager.tryActivateSkill(player, "immortal_fortress", 600)) {
                activateImmortalFortress(player);
            }
            return;
        }

        // Note: Second Wind is passive-triggered, not manual
    }

    /**
     * Try to activate teamwork tree active skills
     */
    private void tryActivateTeamworkSkill(Player player) {
        // Rally Cry (Tier 1)
        if (effectManager.hasSkillByName(player, "rally_cry")) {
            if (cooldownManager.tryActivateSkill(player, "rally_cry", 120)) {
                activateRallyCry(player);
            }
            return;
        }

        // Healer's Touch (Tier 2)
        if (effectManager.hasSkillByName(player, "healers_touch")) {
            if (cooldownManager.tryActivateSkill(player, "healers_touch", 60)) {
                activateHealersTouch(player);
            }
            return;
        }

        // Tactical Retreat (Tier 3)
        if (effectManager.hasSkillByName(player, "tactical_retreat")) {
            if (cooldownManager.tryActivateSkill(player, "tactical_retreat", 300)) {
                activateTacticalRetreat(player);
            }
            return;
        }

        // Supply Drop (Tier 3)
        if (effectManager.hasSkillByName(player, "supply_drop")) {
            if (cooldownManager.tryActivateSkill(player, "supply_drop", 600)) {
                activateSupplyDrop(player);
            }
            return;
        }

        // Commander's Blessing (Ultimate)
        if (effectManager.hasSkillByName(player, "commanders_blessing")) {
            if (cooldownManager.tryActivateSkill(player, "commanders_blessing", 900)) {
                activateCommandersBlessing(player);
            }
            return;
        }
    }

    // ============================================
    // COMBAT TREE ACTIVATIONS
    // ============================================

    /**
     * Whirlwind Strike: Spin attack hitting all enemies in 4 block radius
     */
    private void activateWhirlwindStrike(Player player) {
        // Check hunger cost
        if (player.getFoodLevel() < 10) {
            MessageUtils.sendMessage(player, "&cNot enough hunger! Need 10 hunger.");
            cooldownManager.clearCooldown(player.getUniqueId(), "whirlwind_strike");
            return;
        }

        // Drain hunger
        player.setFoodLevel(player.getFoodLevel() - 10);

        // Get weapon damage
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double baseDamage = 8.0; // Base iron sword damage

        // Find all enemies in radius
        int hitCount = 0;
        for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
            if (entity instanceof LivingEntity target && !(target instanceof Player)) {
                target.damage(baseDamage, player);
                hitCount++;
            } else if (entity instanceof Player target && !isSameTeam(player, target)) {
                target.damage(baseDamage, player);
                hitCount++;
            }
        }

        // Visual: 360° sweep particles
        Location loc = player.getLocation();
        for (int i = 0; i < 36; i++) {
            double angle = i * 10 * Math.PI / 180;
            double x = Math.cos(angle) * 4;
            double z = Math.sin(angle) * 4;

            player.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                loc.clone().add(x, 1, z),
                3,
                0.2, 0.2, 0.2,
                0.1
            );
        }

        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);

        // Feedback
        MessageUtils.sendMessage(player, "&6⚔ &eWhirlwind Strike! &7Hit " + hitCount + " enemies!");
    }

    /**
     * Warlord's Rampage: 15s ultimate combat buff
     */
    private void activateWarlordsRampage(Player player) {
        int duration = 15; // 15 seconds

        // Apply effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration * 20, 1, false, true)); // +50% damage (Strength II)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 0, false, true)); // +20% speed
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration * 20, 0, false, true)); // +30% attack speed

        // Visual: Red aura + flame particles
        player.getWorld().spawnParticle(
            Particle.FLAME,
            player.getLocation(),
            100,
            1, 1, 1,
            0.3
        );

        // Audio: Ender dragon roar
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);

        // Track active effect
        setActiveEffect(player, "warlords_rampage", duration);

        // Server announcement
        MessageUtils.broadcast("&c⚔ &4" + player.getName() + " &chas activated &4&lWARLORD'S RAMPAGE&c!");

        // Start particle task
        startRampageParticles(player, duration);
    }

    // ============================================
    // GATHERING TREE ACTIVATIONS
    // ============================================

    /**
     * Vein Miner: Prime player to break connected ores
     */
    private void primeVeinMiner(Player player) {
        // Check hunger cost
        if (player.getFoodLevel() < 15) {
            MessageUtils.sendMessage(player, "&cNot enough hunger! Need 15 hunger.");
            cooldownManager.clearCooldown(player.getUniqueId(), "vein_miner");
            return;
        }

        // Drain hunger
        player.setFoodLevel(player.getFoodLevel() - 15);

        // Prime for next ore break (handled in GatheringSkillListener)
        skillActivationPrimed.put(player.getUniqueId(), System.currentTimeMillis());

        // Visual
        player.getWorld().spawnParticle(
            Particle.VILLAGER_HAPPY,
            player.getLocation().add(0, 1, 0),
            30,
            0.5, 0.5, 0.5,
            0.2
        );

        MessageUtils.sendMessage(player, "&6⛏ &eVein Miner activated! Break an ore to mine the vein.");
    }

    /**
     * Ore Transmutation: Convert 16 iron ore → 1 diamond
     */
    private void activateOreTransmutation(Player player) {
        // Check if at home beacon
        if (!isAtHomeBeacon(player)) {
            MessageUtils.sendMessage(player, "&cYou must be at your home beacon to use this skill!");
            cooldownManager.clearCooldown(player.getUniqueId(), "ore_transmutation");
            return;
        }

        // Check inventory for iron ore
        ItemStack ironOre = new ItemStack(Material.IRON_ORE, 16);
        if (!player.getInventory().containsAtLeast(ironOre, 16)) {
            MessageUtils.sendMessage(player, "&cYou need 16 iron ore to transmute!");
            cooldownManager.clearCooldown(player.getUniqueId(), "ore_transmutation");
            return;
        }

        // Remove iron ore
        player.getInventory().removeItem(ironOre);

        // Give diamond
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));

        // Visual: Alchemical swirl
        player.getWorld().spawnParticle(
            Particle.ENCHANTMENT_TABLE,
            player.getLocation().add(0, 2, 0),
            100,
            0.5, 0.5, 0.5,
            1.0
        );

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);

        MessageUtils.sendMessage(player, "&b✨ &eTransmuted 16 iron ore into 1 diamond!");
    }

    /**
     * Midas Touch: Convert ore stack → random rare resources
     */
    private void activateMidasTouch(Player player) {
        // This is handled as a passive bonus + active conversion
        MessageUtils.sendMessage(player, "&6✨ &eMidas Touch activated for 30 seconds!");
        MessageUtils.sendMessage(player, "&7All gathering yields +50% during this time!");

        // Set active effect
        setActiveEffect(player, "midas_touch", 30);

        // Visual
        player.getWorld().spawnParticle(
            Particle.END_ROD,
            player.getLocation(),
            50,
            1, 1, 1,
            0.2
        );
    }

    // ============================================
    // SURVIVAL TREE ACTIVATIONS
    // ============================================

    /**
     * Immortal Fortress: 20s ultimate tank buff
     */
    private void activateImmortalFortress(Player player) {
        int duration = 20; // 20 seconds

        // Apply effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 3, false, true)); // ~70% reduction
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration * 20, 4, false, true)); // +5 hearts

        // Track active effect for damage reflection
        setActiveEffect(player, "immortal_fortress", duration);

        // Visual: Golden dome shield
        player.getWorld().spawnParticle(
            Particle.TOTEM,
            player.getLocation(),
            100,
            2, 2, 2,
            0.5
        );

        // Audio: Anvil sound
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.5f);

        MessageUtils.sendMessage(player, "&6🛡 &eIMMORTAL FORTRESS! 70% damage reduction for 20s!");

        // Start particle shield effect
        startFortressParticles(player, duration);
    }

    // ============================================
    // TEAMWORK TREE ACTIVATIONS
    // ============================================

    /**
     * Rally Cry: Buff nearby teammates
     */
    private void activateRallyCry(Player player) {
        int duration = 15; // 15 seconds
        double radius = 20.0;

        int buffedCount = 0;
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= radius && isSameTeam(player, nearby)) {
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration * 20, 0, false, true));
                buffedCount++;

                // Visual on buffed player
                nearby.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    nearby.getLocation().add(0, 2, 0),
                    20,
                    0.5, 0.5, 0.5,
                    0.1
                );
            }
        }

        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.EVENT_RAID_HORN, 1.5f, 1.0f);

        MessageUtils.sendMessage(player, "&6👥 &eRally Cry! Buffed " + buffedCount + " teammates!");
    }

    /**
     * Healer's Touch: Heal nearby teammate
     */
    private void activateHealersTouch(Player player) {
        // Check hunger cost
        if (player.getFoodLevel() < 20) {
            MessageUtils.sendMessage(player, "&cNot enough hunger! Need full hunger.");
            cooldownManager.clearCooldown(player.getUniqueId(), "healers_touch");
            return;
        }

        // Find nearest injured teammate
        Player target = findNearestInjuredTeammate(player, 5.0);
        if (target == null) {
            MessageUtils.sendMessage(player, "&cNo injured teammates within 5 blocks!");
            cooldownManager.clearCooldown(player.getUniqueId(), "healers_touch");
            return;
        }

        // Drain hunger
        player.setFoodLevel(player.getFoodLevel() - 20);

        // Heal target
        double healAmount = 10.0; // 5 hearts
        target.setHealth(Math.min(target.getHealth() + healAmount, target.getMaxHealth()));

        // Visual: Green beam
        drawBeam(player.getEyeLocation(), target.getEyeLocation(), Particle.VILLAGER_HAPPY);

        // Sound
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        MessageUtils.sendMessage(player, "&a💚 &eHealed " + target.getName() + " for 5 hearts!");
        MessageUtils.sendMessage(target, "&a💚 &e" + player.getName() + " healed you!");
    }

    /**
     * Tactical Retreat: Teleport to home beacon with teammate
     */
    private void activateTacticalRetreat(Player player) {
        Location homeBeacon = getHomeBeaconLocation(player);
        if (homeBeacon == null) {
            MessageUtils.sendMessage(player, "&cCouldn't find home beacon!");
            cooldownManager.clearCooldown(player.getUniqueId(), "tactical_retreat");
            return;
        }

        // Check for nearby teammate to bring
        Player teammate = findNearestTeammate(player, 3.0);

        // Teleport
        player.teleport(homeBeacon);
        if (teammate != null) {
            teammate.teleport(homeBeacon);
            MessageUtils.sendMessage(teammate, "&6🌀 &e" + player.getName() + " brought you to safety!");
        }

        // Visual: Ender pearl particles
        player.getWorld().spawnParticle(
            Particle.PORTAL,
            player.getLocation(),
            100,
            1, 1, 1,
            1.0
        );

        MessageUtils.sendMessage(player, "&6🌀 &eTactical Retreat to home beacon!");
    }

    /**
     * Supply Drop: Drop care package
     */
    private void activateSupplyDrop(Player player) {
        Location dropLoc = player.getLocation().add(0, 50, 0);

        // Spawn chest with items
        dropLoc.getBlock().setType(Material.CHEST);
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) dropLoc.getBlock().getState();

        // Fill with supplies
        chest.getInventory().addItem(
            new ItemStack(Material.COOKED_BEEF, 16),
            new ItemStack(Material.GOLDEN_APPLE, 8),
            new ItemStack(Material.IRON_HELMET),
            new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_LEGGINGS),
            new ItemStack(Material.IRON_BOOTS)
        );

        // Visual: Beacon beam
        player.getWorld().spawnParticle(
            Particle.END_ROD,
            dropLoc,
            100,
            0.5, 30, 0.5,
            0.1
        );

        MessageUtils.sendMessage(player, "&6📦 &eSupply Drop deployed at your location!");
    }

    /**
     * Commander's Blessing: Ultimate team buff
     */
    private void activateCommandersBlessing(Player player) {
        int duration = 30; // 30 seconds

        // Buff ALL living teammates server-wide
        int buffedCount = 0;
        for (Player teammate : Bukkit.getOnlinePlayers()) {
            if (isSameTeam(player, teammate)) {
                teammate.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration * 20, 0, false, true)); // +25% damage
                teammate.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 0, false, true)); // +25% resistance
                teammate.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 1, false, true)); // Regen II
                teammate.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 1, false, true)); // Speed II

                // Visual: Crown + banner
                teammate.getWorld().spawnParticle(
                    Particle.END_ROD,
                    teammate.getLocation().add(0, 3, 0),
                    30,
                    0.3, 0.3, 0.3,
                    0.1
                );

                buffedCount++;
            }
        }

        // Server announcement
        MessageUtils.broadcast("&6👑 &e" + player.getName() + " &6has rallied the team! Fight with honor! &e⚔");

        // Sound: Bell toll
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1.5f, 1.0f);
        }

        MessageUtils.sendMessage(player, "&6👑 &eCOMMANDER'S BLESSING! Buffed " + buffedCount + " teammates!");
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private boolean isWeapon(Material mat) {
        return mat.name().endsWith("_SWORD") || mat.name().endsWith("_AXE");
    }

    private boolean isTool(Material mat) {
        return mat.name().endsWith("_PICKAXE") || mat.name().endsWith("_AXE") ||
               mat.name().endsWith("_SHOVEL") || mat.name().endsWith("_HOE");
    }

    private boolean isFood(Material mat) {
        return mat.isEdible();
    }

    private boolean isSameTeam(Player p1, Player p2) {
        var data1 = plugin.getGameManager().getPlayerData(p1);
        var data2 = plugin.getGameManager().getPlayerData(p2);
        return data1 != null && data2 != null && data1.getTeamId() == data2.getTeamId();
    }

    private boolean isAtHomeBeacon(Player player) {
        var playerData = plugin.getGameManager().getPlayerData(player);
        if (playerData == null) return false;

        Location homeBeacon = getHomeBeaconLocation(player);
        if (homeBeacon == null) return false;

        return player.getLocation().distance(homeBeacon) <= 5.0;
    }

    private Location getHomeBeaconLocation(Player player) {
        var playerData = plugin.getGameManager().getPlayerData(player);
        if (playerData == null) return null;

        // Get home territory beacon location from config
        // This is a placeholder - you'd get from TerritoryManager
        return null; // TODO: Implement
    }

    private Player findNearestInjuredTeammate(Player player, double radius) {
        Player nearest = null;
        double minDistance = radius;

        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (!isSameTeam(player, nearby)) continue;

            double distance = nearby.getLocation().distance(player.getLocation());
            if (distance <= radius && distance < minDistance) {
                if (nearby.getHealth() < nearby.getMaxHealth()) {
                    nearest = nearby;
                    minDistance = distance;
                }
            }
        }

        return nearest;
    }

    private Player findNearestTeammate(Player player, double radius) {
        Player nearest = null;
        double minDistance = radius;

        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (!isSameTeam(player, nearby)) continue;

            double distance = nearby.getLocation().distance(player.getLocation());
            if (distance <= radius && distance < minDistance) {
                nearest = nearby;
                minDistance = distance;
            }
        }

        return nearest;
    }

    private void drawBeam(Location from, Location to, Particle particle) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);

        for (double d = 0; d < distance; d += 0.5) {
            Location particleLoc = from.clone().add(direction.clone().multiply(d));
            from.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void setActiveEffect(Player player, String skillName, int durationSeconds) {
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        activeSkillEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                          .put(skillName, expiryTime);
    }

    public boolean hasActiveEffect(Player player, String skillName) {
        Map<String, Long> effects = activeSkillEffects.get(player.getUniqueId());
        if (effects == null) return false;

        Long expiryTime = effects.get(skillName);
        if (expiryTime == null) return false;

        if (System.currentTimeMillis() >= expiryTime) {
            effects.remove(skillName);
            return false;
        }

        return true;
    }

    public boolean isVeinMinerPrimed(UUID playerUUID) {
        Long primedTime = skillActivationPrimed.get(playerUUID);
        if (primedTime == null) return false;

        // Primed for 10 seconds
        if (System.currentTimeMillis() - primedTime > 10000) {
            skillActivationPrimed.remove(playerUUID);
            return false;
        }

        return true;
    }

    public void consumeVeinMinerPrime(UUID playerUUID) {
        skillActivationPrimed.remove(playerUUID);
    }

    private void startRampageParticles(Player player, int durationSeconds) {
        final int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!hasActiveEffect(player, "warlords_rampage")) {
                Bukkit.getScheduler().cancelTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {}, 0, 1));
                return;
            }

            player.getWorld().spawnParticle(
                Particle.FLAME,
                player.getLocation(),
                5,
                0.5, 1, 0.5,
                0.1
            );
        }, 0L, 10L);

        // Cancel after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskId);
        }, durationSeconds * 20L);
    }

    private void startFortressParticles(Player player, int durationSeconds) {
        final int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!hasActiveEffect(player, "immortal_fortress")) {
                Bukkit.getScheduler().cancelTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {}, 0, 1));
                return;
            }

            // Golden dome particles
            Location loc = player.getLocation();
            for (int i = 0; i < 20; i++) {
                double angle = i * 18 * Math.PI / 180;
                double x = Math.cos(angle) * 2;
                double z = Math.sin(angle) * 2;

                player.getWorld().spawnParticle(
                    Particle.FIREWORKS_SPARK,
                    loc.clone().add(x, 1, z),
                    1,
                    0, 0, 0,
                    0
                );
            }
        }, 0L, 10L);

        // Cancel after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskId);
        }, durationSeconds * 20L);
    }

    /**
     * Clean up on player death
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        activeSkillEffects.remove(uuid);
        skillActivationPrimed.remove(uuid);
    }
}
