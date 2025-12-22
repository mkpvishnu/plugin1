package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.PlayerSkills;
import com.seasonsofconflict.models.Skill;
import com.seasonsofconflict.models.SkillTree;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages skill effect application and passive skill state
 */
public class SkillEffectManager {

    private final SeasonsOfConflict plugin;

    // Track Bloodlust stacks: PlayerUUID -> (TargetUUID -> StackData)
    private final Map<UUID, Map<UUID, BloodlustStack>> bloodlustStacks;

    // Track last combat time for regeneration: PlayerUUID -> timestamp
    private final Map<UUID, Long> lastCombatTime;

    public SkillEffectManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.bloodlustStacks = new ConcurrentHashMap<>();
        this.lastCombatTime = new ConcurrentHashMap<>();
    }

    /**
     * Check if player has a specific skill unlocked
     */
    public boolean hasSkill(Player player, Skill skill) {
        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        return skills.hasSkill(skill.getTree(), skill.getTier());
    }

    /**
     * Get the specific skill a player has unlocked in a tier
     */
    public String getUnlockedSkill(Player player, SkillTree tree, com.seasonsofconflict.models.SkillTier tier) {
        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        return skills.getSkill(tree, tier);
    }

    /**
     * Check if player has a skill by internal name
     */
    public boolean hasSkillByName(Player player, String internalName) {
        Skill skill = Skill.fromInternalName(internalName);
        return skill != null && hasSkill(player, skill);
    }

    // ============================================
    // COMBAT TREE EFFECTS
    // ============================================

    /**
     * Swift Strikes: Apply +15% attack speed
     */
    public void applySwiftStrikes(Player player) {
        if (!hasSkillByName(player, "swift_strikes")) {
            return;
        }

        AttributeInstance attackSpeed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeed != null) {
            // Base attack speed is 4.0, +15% = +0.6
            double baseSpeed = 4.0;
            double bonus = baseSpeed * 0.15;
            attackSpeed.setBaseValue(baseSpeed + bonus);
        }
    }

    /**
     * Iron Skin: Apply +2 hearts (4 HP) max health
     */
    public void applyIronSkin(Player player) {
        if (!hasSkillByName(player, "iron_skin")) {
            return;
        }

        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            // +4 HP (2 hearts)
            double currentMax = maxHealth.getBaseValue();
            maxHealth.setBaseValue(currentMax + 4.0);
        }
    }

    /**
     * Bloodlust: Apply damage stacking (+5% per hit, max 3 stacks)
     */
    public double applyBloodlust(Player attacker, UUID targetUUID, double baseDamage) {
        if (!hasSkillByName(attacker, "bloodlust")) {
            return baseDamage;
        }

        UUID attackerUUID = attacker.getUniqueId();

        // Get or create stack data
        bloodlustStacks.putIfAbsent(attackerUUID, new ConcurrentHashMap<>());
        Map<UUID, BloodlustStack> targetStacks = bloodlustStacks.get(attackerUUID);

        BloodlustStack stack = targetStacks.get(targetUUID);
        long currentTime = System.currentTimeMillis();

        if (stack == null || (currentTime - stack.lastHitTime) > 5000) {
            // New stack or expired (5s duration)
            stack = new BloodlustStack(1, currentTime);
        } else {
            // Increment stack (max 3)
            stack = new BloodlustStack(Math.min(stack.stacks + 1, 3), currentTime);
        }

        targetStacks.put(targetUUID, stack);

        // Apply damage bonus: 5% per stack
        double damageMultiplier = 1.0 + (stack.stacks * 0.05);
        return baseDamage * damageMultiplier;
    }

    /**
     * Get Bloodlust stacks for display (e.g., visual effects)
     */
    public int getBloodlustStacks(Player player, UUID targetUUID) {
        Map<UUID, BloodlustStack> targetStacks = bloodlustStacks.get(player.getUniqueId());
        if (targetStacks == null) {
            return 0;
        }

        BloodlustStack stack = targetStacks.get(targetUUID);
        if (stack == null) {
            return 0;
        }

        // Check if expired
        long currentTime = System.currentTimeMillis();
        if ((currentTime - stack.lastHitTime) > 5000) {
            return 0;
        }

        return stack.stacks;
    }

    /**
     * Critical Precision: Check for crit (20% chance, +50% damage)
     */
    public double applyCriticalPrecision(Player player, double baseDamage) {
        if (!hasSkillByName(player, "critical_precision")) {
            return baseDamage;
        }

        // 20% chance for crit
        if (Math.random() < 0.20) {
            return baseDamage * 1.5; // +50% damage
        }

        return baseDamage;
    }

    /**
     * Armor Breaker: Apply 30% armor penetration
     * Note: This modifies effective armor, handled in damage calculation
     */
    public double applyArmorBreaker(Player player, double damage, double armorReduction) {
        if (!hasSkillByName(player, "armor_breaker")) {
            return damage * armorReduction;
        }

        // 30% armor penetration = reduce armor effectiveness by 30%
        double reducedArmorEffect = armorReduction * 0.70; // 30% less effective
        return damage * (1 - (1 - reducedArmorEffect) * 0.70);
    }

    /**
     * Execution: Bonus damage to low health targets (<30% HP)
     */
    public double applyExecution(Player player, double baseDamage, double targetHealthPercent) {
        if (!hasSkillByName(player, "execution")) {
            return baseDamage;
        }

        // +30% damage if target is below 30% HP
        if (targetHealthPercent < 0.30) {
            return baseDamage * 1.30;
        }

        return baseDamage;
    }

    /**
     * Update last combat time for regeneration tracking
     */
    public void updateCombatTime(Player player) {
        lastCombatTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Check if player is out of combat (8s threshold)
     */
    public boolean isOutOfCombat(Player player) {
        Long lastTime = lastCombatTime.get(player.getUniqueId());
        if (lastTime == null) {
            return true; // Never been in combat
        }

        long timeSinceCombat = System.currentTimeMillis() - lastTime;
        return timeSinceCombat > 8000; // 8 seconds
    }

    /**
     * Clear combat state for player (on logout, death, etc.)
     */
    public void clearPlayerState(UUID playerUUID) {
        bloodlustStacks.remove(playerUUID);
        lastCombatTime.remove(playerUUID);
    }

    // ============================================
    // GATHERING TREE EFFECTS
    // ============================================

    /**
     * Fortune's Touch: +15% chance for double drops
     */
    public boolean applyFortuneTouch(Player player) {
        if (!hasSkillByName(player, "fortunes_touch")) {
            return false;
        }
        return Math.random() < 0.15;
    }

    /**
     * Swift Hands: -20% block break time (Haste I effect)
     */
    public void applySwiftHands(Player player) {
        if (!hasSkillByName(player, "swift_hands")) {
            return;
        }
        // Apply Haste I effect continuously
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.FAST_DIGGING,
            200, // 10 seconds (renewed continuously)
            0,   // Level I
            false,
            false
        ));
    }

    /**
     * Lumberjack: +50% wood drops
     */
    public int applyLumberjack(Player player, int baseDrops) {
        if (!hasSkillByName(player, "lumberjack")) {
            return baseDrops;
        }
        return (int) (baseDrops * 1.5);
    }

    /**
     * Green Thumb: +20% crop yields
     */
    public int applyGreenThumb(Player player, int baseYield) {
        if (!hasSkillByName(player, "green_thumb")) {
            return baseYield;
        }
        return (int) (baseYield * 1.2);
    }

    // ============================================
    // SURVIVAL TREE EFFECTS
    // ============================================

    /**
     * Hardy: Apply +3 hearts (6 HP) max health
     */
    public void applyHardy(Player player) {
        if (!hasSkillByName(player, "hardy")) {
            return;
        }

        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            // +6 HP (3 hearts)
            double currentMax = maxHealth.getBaseValue();
            maxHealth.setBaseValue(currentMax + 6.0);
        }
    }

    /**
     * Thick Skin: Apply -10% damage reduction
     */
    public double applyThickSkin(Player player, double damage) {
        if (!hasSkillByName(player, "thick_skin")) {
            return damage;
        }
        return damage * 0.90; // 10% reduction
    }

    /**
     * Hunger Resistance: +30% food saturation
     */
    public float applyHungerResistance(Player player, float saturation) {
        if (!hasSkillByName(player, "hunger_resistance")) {
            return saturation;
        }
        return saturation * 1.30f; // 30% more saturation
    }

    /**
     * Regeneration: 0.5 HP every 5s when out of combat
     */
    public boolean canRegenerate(Player player) {
        return hasSkillByName(player, "regeneration") && isOutOfCombat(player);
    }

    /**
     * Fire Immunity: Check if player is immune to fire/lava
     */
    public boolean hasFireImmunity(Player player) {
        return hasSkillByName(player, "fire_immunity");
    }

    /**
     * Fall Damage Negation: Reduce/negate fall damage
     */
    public double applyFallDamageNegation(Player player, double damage, double fallDistance) {
        if (!hasSkillByName(player, "fall_damage_negation")) {
            return damage;
        }

        // No fall damage under 20 blocks, 50% reduction above
        if (fallDistance < 20.0) {
            return 0.0;
        } else {
            return damage * 0.50;
        }
    }

    // ============================================
    // TEAMWORK TREE EFFECTS
    // ============================================

    /**
     * Shared Victory: +10% team points from quests
     */
    public int applySharedVictory(Player player, int basePoints) {
        if (!hasSkillByName(player, "shared_victory")) {
            return basePoints;
        }
        return (int) (basePoints * 1.10);
    }

    /**
     * Pack Tactics: +5% damage per nearby teammate (max 20%)
     */
    public double applyPackTactics(Player player, double baseDamage) {
        if (!hasSkillByName(player, "pack_tactics")) {
            return baseDamage;
        }

        // Count nearby teammates (15 block range)
        long nearbyTeammates = player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .filter(p -> p.getLocation().distance(player.getLocation()) <= 15.0)
            .filter(p -> {
                // Check if same team
                var playerTeam = plugin.getGameManager().getPlayerData(player);
                var otherTeam = plugin.getGameManager().getPlayerData(p);
                return playerTeam != null && otherTeam != null &&
                       playerTeam.getTeamId() == otherTeam.getTeamId();
            })
            .count();

        // +5% per ally, max 4 allies (20% total)
        int bonusPercent = (int) Math.min(nearbyTeammates * 5, 20);
        return baseDamage * (1.0 + (bonusPercent / 100.0));
    }

    /**
     * Resource Sharing: Check if drops should be duplicated to teammate
     */
    public boolean applyResourceSharing(Player player) {
        if (!hasSkillByName(player, "resource_sharing")) {
            return false;
        }
        return Math.random() < 0.10; // 10% chance
    }

    /**
     * Get random nearby teammate for resource sharing
     */
    public Player getRandomNearbyTeammate(Player player) {
        List<Player> teammates = player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .filter(p -> p.getLocation().distance(player.getLocation()) <= 30.0)
            .filter(p -> {
                var playerTeam = plugin.getGameManager().getPlayerData(player);
                var otherTeam = plugin.getGameManager().getPlayerData(p);
                return playerTeam != null && otherTeam != null &&
                       playerTeam.getTeamId() == otherTeam.getTeamId();
            })
            .toList();

        if (teammates.isEmpty()) {
            return null;
        }

        return teammates.get(new Random().nextInt(teammates.size()));
    }

    // ============================================
    // TIER 3-4 PASSIVE SKILLS
    // ============================================

    /**
     * Lifesteal: Heal 15% of damage dealt (PvP only)
     */
    public double applyLifesteal(Player attacker, double damageDealt, boolean isPvP) {
        if (!isPvP || !hasSkillByName(attacker, "lifesteal")) {
            return 0;
        }

        double healAmount = damageDealt * 0.15;
        attacker.setHealth(Math.min(attacker.getHealth() + healAmount, attacker.getMaxHealth()));
        return healAmount;
    }

    /**
     * Titan's Grip: Apply +10% damage and knockback resistance
     */
    public double applyTitansGrip(Player player, double baseDamage) {
        if (!hasSkillByName(player, "titans_grip")) {
            return baseDamage;
        }
        return baseDamage * 1.10;
    }

    public void applyTitansGripKnockbackRes(Player player) {
        if (!hasSkillByName(player, "titans_grip")) {
            return;
        }
        // Apply knockback resistance via attribute
        var knockbackRes = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackRes != null) {
            knockbackRes.setBaseValue(knockbackRes.getBaseValue() + 0.30);
        }
    }

    /**
     * Poison/Wither Immunity: Check if immune
     */
    public boolean hasPoisonWitherImmunity(Player player) {
        return hasSkillByName(player, "poison_wither_immunity");
    }

    /**
     * Winter Adaptation: Check if immune to freezing, get damage bonus in Winter
     */
    public boolean hasWinterAdaptation(Player player) {
        return hasSkillByName(player, "winter_adaptation");
    }

    /**
     * Thorns: Reflect damage back to attacker
     */
    public double applyThorns(Player defender, double damageReceived) {
        if (!hasSkillByName(defender, "thorns")) {
            return 0;
        }
        return damageReceived * 0.20; // 20% reflected
    }

    /**
     * Unstoppable: Check if immune to debuffs
     */
    public boolean isUnstoppable(Player player) {
        return hasSkillByName(player, "unstoppable");
    }

    /**
     * Combat Medic: Apply revival discount
     */
    public double applyCombatMedicDiscount(Player healer, double baseCost) {
        if (!hasSkillByName(healer, "combat_medic")) {
            return baseCost;
        }
        return baseCost * 0.75; // 25% discount
    }

    /**
     * Inspirational Leader: Apply XP and gathering bonuses to nearby teammates
     */
    public boolean isNearInspirationalLeader(Player player) {
        // Check if any teammate within 50 blocks has Inspirational Leader
        return player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .filter(p -> p.getLocation().distance(player.getLocation()) <= 50.0)
            .filter(p -> {
                var playerTeam = plugin.getGameManager().getPlayerData(player);
                var otherTeam = plugin.getGameManager().getPlayerData(p);
                return playerTeam != null && otherTeam != null &&
                       playerTeam.getTeamId() == otherTeam.getTeamId();
            })
            .anyMatch(p -> hasSkillByName(p, "inspirational_leader"));
    }

    /**
     * Strategic Mind: Check if player has this skill (enables seeing teammates through walls)
     */
    public boolean hasStrategicMind(Player player) {
        return hasSkillByName(player, "strategic_mind");
    }

    /**
     * Bloodlust stack data
     */
    private static class BloodlustStack {
        final int stacks;
        final long lastHitTime;

        BloodlustStack(int stacks, long lastHitTime) {
            this.stacks = stacks;
            this.lastHitTime = lastHitTime;
        }
    }
}
