package com.seasonsofconflict.models;

import org.bukkit.Material;

/**
 * Defines all 60 skills across 4 skill trees
 * Each skill has a name, description, tree, tier, and effect type
 */
public enum Skill {

    // ========== COMBAT TREE (15 skills) ==========

    // Tier 1 - Aggressive Basics (5 pts each)
    SWIFT_STRIKES(
        "Swift Strikes",
        "swift_strikes",
        "+15% attack speed",
        SkillTree.COMBAT,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.IRON_SWORD
    ),
    IRON_SKIN(
        "Iron Skin",
        "iron_skin",
        "+2 hearts (4 HP) max health",
        SkillTree.COMBAT,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.IRON_CHESTPLATE
    ),
    BLOODLUST(
        "Bloodlust",
        "bloodlust",
        "+5% damage per consecutive hit (max 3 stacks, 5s duration)",
        SkillTree.COMBAT,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.REDSTONE
    ),

    // Tier 2 - Combat Mastery (10 pts each)
    CRITICAL_PRECISION(
        "Critical Precision",
        "critical_precision",
        "20% chance to deal +50% damage",
        SkillTree.COMBAT,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.DIAMOND_SWORD
    ),
    ARMOR_BREAKER(
        "Armor Breaker",
        "armor_breaker",
        "30% armor penetration",
        SkillTree.COMBAT,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.IRON_AXE
    ),
    EXECUTION(
        "Execution",
        "execution",
        "+25% damage to targets below 30% health",
        SkillTree.COMBAT,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.NETHERITE_SWORD
    ),

    // Tier 3 - Deadly Arts (15 pts each)
    WHIRLWIND_STRIKE(
        "Whirlwind Strike",
        "whirlwind_strike",
        "Spin attack hitting all enemies in 4 block radius (60s cooldown)",
        SkillTree.COMBAT,
        SkillTier.TIER_3,
        SkillType.ACTIVE,
        Material.GOLDEN_SWORD
    ),
    LAST_STAND(
        "Last Stand",
        "last_stand",
        "Below 10 hearts: +30% damage & +20% damage resist for 10s (2 min cooldown)",
        SkillTree.COMBAT,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.TOTEM_OF_UNDYING
    ),
    LIFESTEAL(
        "Lifesteal",
        "lifesteal",
        "Heal 15% of damage dealt (PvP only)",
        SkillTree.COMBAT,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.GLISTERING_MELON_SLICE
    ),

    // Tier 4 - War Machine (20 pts each)
    RELENTLESS_ASSAULT(
        "Relentless Assault",
        "relentless_assault",
        "Each kill grants +20% attack speed for 10s (stacks 3x)",
        SkillTree.COMBAT,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.BLAZE_ROD
    ),
    TITANS_GRIP(
        "Titan's Grip",
        "titans_grip",
        "+30% knockback resistance, +10% damage",
        SkillTree.COMBAT,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.IRON_BLOCK
    ),
    WARRIORS_RESOLVE(
        "Warrior's Resolve",
        "warriors_resolve",
        "Cannot drop below 1 HP for 5s after fatal damage (3 min cooldown)",
        SkillTree.COMBAT,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.ENCHANTED_GOLDEN_APPLE
    ),

    // Ultimate (25 pts)
    WARLORDS_RAMPAGE(
        "Warlord's Rampage",
        "warlords_rampage",
        "15s: +50% damage, +30% atk speed, +20% move speed, knockback immunity, 5 hearts per kill (5 min cooldown)",
        SkillTree.COMBAT,
        SkillTier.ULTIMATE,
        SkillType.ACTIVE,
        Material.NETHER_STAR
    ),

    // ========== GATHERING TREE (15 skills) ==========

    // Tier 1 - Harvester (5 pts each)
    FORTUNES_TOUCH(
        "Fortune's Touch",
        "fortunes_touch",
        "+15% chance for double drops (ores/logs/crops)",
        SkillTree.GATHERING,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.DIAMOND
    ),
    SWIFT_HANDS(
        "Swift Hands",
        "swift_hands",
        "-20% block break time",
        SkillTree.GATHERING,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.GOLDEN_PICKAXE
    ),
    TREASURE_HUNTER(
        "Treasure Hunter",
        "treasure_hunter",
        "See diamond/iron/gold ore glow through walls (8 block range)",
        SkillTree.GATHERING,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.SPYGLASS
    ),

    // Tier 2 - Master Gatherer (10 pts each)
    VEIN_MINER(
        "Vein Miner",
        "vein_miner",
        "Breaking 1 ore breaks up to 5 connected ores (30s cooldown, 15 hunger)",
        SkillTree.GATHERING,
        SkillTier.TIER_2,
        SkillType.ACTIVE,
        Material.DIAMOND_PICKAXE
    ),
    GREEN_THUMB(
        "Green Thumb",
        "green_thumb",
        "Crops planted by you grow 25% faster, +20% yields",
        SkillTree.GATHERING,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.WHEAT_SEEDS
    ),
    LUMBERJACK(
        "Lumberjack",
        "lumberjack",
        "Trees drop 50% more wood, breaking bottom log fells tree",
        SkillTree.GATHERING,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.IRON_AXE
    ),

    // Tier 3 - Resource Specialist (15 pts each)
    DIAMOND_HUNTER(
        "Diamond Hunter",
        "diamond_hunter",
        "Diamond ore drop rate 10% → 25%, reveal location within 32 blocks",
        SkillTree.GATHERING,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.DIAMOND_ORE
    ),
    EFFICIENT_FISHING(
        "Efficient Fishing",
        "efficient_fishing",
        "-50% fishing time, +30% treasure chance",
        SkillTree.GATHERING,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.FISHING_ROD
    ),
    ORE_TRANSMUTATION(
        "Ore Transmutation",
        "ore_transmutation",
        "Convert 16 iron ore → 1 diamond (at home beacon, 10 min cooldown)",
        SkillTree.GATHERING,
        SkillTier.TIER_3,
        SkillType.ACTIVE,
        Material.GOLD_INGOT
    ),

    // Tier 4 - Master Craftsman (20 pts each)
    TOOL_DURABILITY(
        "Tool Durability",
        "tool_durability",
        "Tools last 2x longer, won't break (stop at 1 durability)",
        SkillTree.GATHERING,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.ANVIL
    ),
    RESOURCE_MAGNET(
        "Resource Magnet",
        "resource_magnet",
        "Items fly to you from 8 blocks away",
        SkillTree.GATHERING,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.HOPPER
    ),
    BULK_PROCESSING(
        "Bulk Processing",
        "bulk_processing",
        "25% chance to smelt 2x items at once",
        SkillTree.GATHERING,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.FURNACE
    ),

    // Ultimate (25 pts)
    MIDAS_TOUCH(
        "Midas Touch",
        "midas_touch",
        "Passive: +50% all gathering drops. Active: Convert 1 stack ore → rare resources (10 min cooldown)",
        SkillTree.GATHERING,
        SkillTier.ULTIMATE,
        SkillType.PASSIVE_ACTIVE,
        Material.GOLD_BLOCK
    ),

    // ========== SURVIVAL TREE (15 skills) ==========

    // Tier 1 - Basic Survival (5 pts each)
    HARDY(
        "Hardy",
        "hardy",
        "+3 hearts (6 HP) max health",
        SkillTree.SURVIVAL,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.GOLDEN_APPLE
    ),
    THICK_SKIN(
        "Thick Skin",
        "thick_skin",
        "-10% damage from all sources",
        SkillTree.SURVIVAL,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.LEATHER_CHESTPLATE
    ),
    HUNGER_RESISTANCE(
        "Hunger Resistance",
        "hunger_resistance",
        "Food lasts 30% longer, -20% hunger drain",
        SkillTree.SURVIVAL,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.COOKED_BEEF
    ),

    // Tier 2 - Wilderness Expert (10 pts each)
    REGENERATION(
        "Regeneration",
        "regeneration",
        "Regen 0.5 HP every 5s when out of combat (8s threshold)",
        SkillTree.SURVIVAL,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.GLISTERING_MELON_SLICE
    ),
    FIRE_IMMUNITY(
        "Fire Immunity",
        "fire_immunity",
        "Immune to fire/lava damage (still burning DoT after exit)",
        SkillTree.SURVIVAL,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.MAGMA_CREAM
    ),
    FALL_DAMAGE_NEGATION(
        "Fall Damage Negation",
        "fall_damage_negation",
        "No fall damage under 20 blocks, -50% above",
        SkillTree.SURVIVAL,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.FEATHER
    ),

    // Tier 3 - Survivor (15 pts each)
    SECOND_WIND(
        "Second Wind",
        "second_wind",
        "Below 5 hearts: instantly heal 10 hearts (5 min cooldown)",
        SkillTree.SURVIVAL,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.TOTEM_OF_UNDYING
    ),
    POISON_WITHER_IMMUNITY(
        "Poison/Wither Immunity",
        "poison_wither_immunity",
        "Immune to poison and wither effects",
        SkillTree.SURVIVAL,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.MILK_BUCKET
    ),
    WINTER_ADAPTATION(
        "Winter Adaptation",
        "winter_adaptation",
        "Immune to freezing damage, +20% damage in Winter",
        SkillTree.SURVIVAL,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.SNOWBALL
    ),

    // Tier 4 - Tank (20 pts each)
    DAMAGE_ABSORPTION(
        "Damage Absorption",
        "damage_absorption",
        "First hit every 30s deals 50% less damage",
        SkillTree.SURVIVAL,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.SHIELD
    ),
    THORNS(
        "Thorns",
        "thorns",
        "Attackers take 20% of damage back",
        SkillTree.SURVIVAL,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.CACTUS
    ),
    UNSTOPPABLE(
        "Unstoppable",
        "unstoppable",
        "Immune to slowness/weakness/mining fatigue, -30% knockback",
        SkillTree.SURVIVAL,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.IRON_BOOTS
    ),

    // Ultimate (25 pts)
    IMMORTAL_FORTRESS(
        "Immortal Fortress",
        "immortal_fortress",
        "20s: 70% damage reduction, knockback immunity, debuff immunity, +5 temp hearts, reflect 30% damage (10 min cooldown)",
        SkillTree.SURVIVAL,
        SkillTier.ULTIMATE,
        SkillType.ACTIVE,
        Material.NETHERITE_CHESTPLATE
    ),

    // ========== TEAMWORK TREE (15 skills) ==========

    // Tier 1 - Team Player (5 pts each)
    SHARED_VICTORY(
        "Shared Victory",
        "shared_victory",
        "+10% team points from your quests",
        SkillTree.TEAMWORK,
        SkillTier.TIER_1,
        SkillType.PASSIVE,
        Material.GOLD_INGOT
    ),
    RALLY_CRY(
        "Rally Cry",
        "rally_cry",
        "Teammates in 20 blocks get +10% damage for 15s (2 min cooldown)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_1,
        SkillType.ACTIVE,
        Material.GOAT_HORN
    ),
    GUARDIAN_ANGEL(
        "Guardian Angel",
        "guardian_angel",
        "Teleport to teammate taking fatal damage within 10 blocks (5 min cooldown)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_1,
        SkillType.ACTIVE,
        Material.FEATHER
    ),

    // Tier 2 - Team Support (10 pts each)
    HEALERS_TOUCH(
        "Healer's Touch",
        "healers_touch",
        "Heal nearby teammate for 5 hearts (5 block range, 60s cooldown, 20 hunger)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_2,
        SkillType.ACTIVE,
        Material.GLISTERING_MELON_SLICE
    ),
    PACK_TACTICS(
        "Pack Tactics",
        "pack_tactics",
        "+5% damage per nearby teammate (max 20%, 15 block range)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.WOLF_SPAWN_EGG
    ),
    RESOURCE_SHARING(
        "Resource Sharing",
        "resource_sharing",
        "10% of gathered resources duplicated to random teammate",
        SkillTree.TEAMWORK,
        SkillTier.TIER_2,
        SkillType.PASSIVE,
        Material.CHEST
    ),

    // Tier 3 - Team Coordinator (15 pts each)
    TACTICAL_RETREAT(
        "Tactical Retreat",
        "tactical_retreat",
        "Teleport to home beacon, can bring 1 teammate within 3 blocks (5 min cooldown)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_3,
        SkillType.ACTIVE,
        Material.ENDER_PEARL
    ),
    COMBAT_MEDIC(
        "Combat Medic",
        "combat_medic",
        "Reviving costs -25% team points, revived get +5 min protection",
        SkillTree.TEAMWORK,
        SkillTier.TIER_3,
        SkillType.PASSIVE,
        Material.GOLDEN_APPLE
    ),
    SUPPLY_DROP(
        "Supply Drop",
        "supply_drop",
        "Drop care package (16 food, 8 golden apples, iron armor) (10 min cooldown)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_3,
        SkillType.ACTIVE,
        Material.CHEST
    ),

    // Tier 4 - Leader (20 pts each)
    INSPIRATIONAL_LEADER(
        "Inspirational Leader",
        "inspirational_leader",
        "All teammates get +10% XP, +5% gathering (50 block aura)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.BEACON
    ),
    STRATEGIC_MIND(
        "Strategic Mind",
        "strategic_mind",
        "See all teammates through walls (glowing outline + health bars)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.ENDER_EYE
    ),
    LAST_STAND_PROTOCOL(
        "Last Stand Protocol",
        "last_stand_protocol",
        "Teammate death in 20 blocks: +30% damage & resist for 30s (stacks 3x)",
        SkillTree.TEAMWORK,
        SkillTier.TIER_4,
        SkillType.PASSIVE,
        Material.WITHER_SKELETON_SKULL
    ),

    // Ultimate (25 pts)
    COMMANDERS_BLESSING(
        "Commander's Blessing",
        "commanders_blessing",
        "30s server-wide: All teammates get +25% damage, +25% resist, Regen II, Speed II, +50% XP (15 min cooldown)",
        SkillTree.TEAMWORK,
        SkillTier.ULTIMATE,
        SkillType.ACTIVE,
        Material.DIAMOND_BLOCK
    );

    // Skill properties
    private final String displayName;
    private final String internalName;  // Used for config and database
    private final String description;
    private final SkillTree tree;
    private final SkillTier tier;
    private final SkillType type;
    private final Material icon;  // For GUI display

    Skill(String displayName, String internalName, String description,
          SkillTree tree, SkillTier tier, SkillType type, Material icon) {
        this.displayName = displayName;
        this.internalName = internalName;
        this.description = description;
        this.tree = tree;
        this.tier = tier;
        this.type = type;
        this.icon = icon;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getDescription() {
        return description;
    }

    public SkillTree getTree() {
        return tree;
    }

    public SkillTier getTier() {
        return tier;
    }

    public SkillType getType() {
        return type;
    }

    public Material getIcon() {
        return icon;
    }

    /**
     * Get skill by internal name (from database)
     */
    public static Skill fromInternalName(String internalName) {
        for (Skill skill : values()) {
            if (skill.getInternalName().equals(internalName)) {
                return skill;
            }
        }
        return null;
    }

    /**
     * Get all skills in a specific tree
     */
    public static Skill[] getSkillsInTree(SkillTree tree) {
        return java.util.Arrays.stream(values())
            .filter(skill -> skill.getTree() == tree)
            .toArray(Skill[]::new);
    }

    /**
     * Get all skills in a specific tier of a tree
     */
    public static Skill[] getSkillsInTier(SkillTree tree, SkillTier tier) {
        return java.util.Arrays.stream(values())
            .filter(skill -> skill.getTree() == tree && skill.getTier() == tier)
            .toArray(Skill[]::new);
    }
}
