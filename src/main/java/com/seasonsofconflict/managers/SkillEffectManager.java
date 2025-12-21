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
        return skills.getUnlockedSkills().get(tree).get(tier);
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
