# 🌟 Skill Trees & Progression System - Design Document

**Version:** 1.0 (Not Yet Implemented)
**Target Release:** Future Update
**Status:** Design Phase

---

## 📋 Table of Contents

- [Overview](#overview)
- [Core Mechanics](#core-mechanics)
- [The 4 Skill Trees](#the-4-skill-trees)
- [Skill Point Economy](#skill-point-economy)
- [GUI Design](#gui-design)
- [Database Schema](#database-schema)
- [Commands](#commands)
- [Implementation Architecture](#implementation-architecture)
- [Balance Considerations](#balance-considerations)
- [Configuration](#configuration)
- [Implementation Phases](#implementation-phases)

---

## Overview

### Concept

**Replace enchantments with permanent skill-based progression** where players earn XP through gameplay and unlock powerful passive/active abilities across 4 specialized skill trees.

### Design Goals

1. **Strategic Depth**: Force meaningful choices through specialization limits
2. **Team Diversity**: Encourage different builds within teams
3. **Cycle Adaptation**: Skills reset on cycle change, forcing meta shifts
4. **Progressive Power**: Players get stronger throughout each cycle
5. **No Grinding**: XP comes naturally from normal gameplay

---

## Core Mechanics

### The 4 Skill Trees

```
🗡️ COMBAT          ⛏️ GATHERING       🛡️ SURVIVAL        👥 TEAMWORK
├─ Tier 1 (5)      ├─ Tier 1 (5)       ├─ Tier 1 (5)       ├─ Tier 1 (5)
├─ Tier 2 (10)     ├─ Tier 2 (10)      ├─ Tier 2 (10)      ├─ Tier 2 (10)
├─ Tier 3 (15)     ├─ Tier 3 (15)      ├─ Tier 3 (15)      ├─ Tier 3 (15)
├─ Tier 4 (20)     ├─ Tier 4 (20)      ├─ Tier 4 (20)      ├─ Tier 4 (20)
└─ Ultimate (25)   └─ Ultimate (25)    └─ Ultimate (25)    └─ Ultimate (25)
```

### Key Rules

- **Max 2 Ultimate Abilities**: Can only reach Ultimate tier in 2 trees
- **Sequential Unlocking**: Must unlock Tier 1 before Tier 2, etc.
- **Choose One Per Tier**: Each tier offers 3 options, pick only 1
- **Skill Points**: Earned via XP (500 XP = 1 Skill Point)
- **Max Skill Points**: 100 total per player
- **Reset on Cycle**: All skills reset when difficulty cycle advances
- **Persist on Death**: Skills remain even after death

---

## The 4 Skill Trees

### 🗡️ COMBAT TREE - "The Berserker"

**Theme:** High damage, aggressive playstyle, risk/reward combat

#### Tier 1 - Aggressive Basics (5 pts each, choose 1)

**1. Swift Strikes**
- Effect: +15% attack speed
- Type: Passive
- Visual: Red particle trail on weapon swings

**2. Iron Skin**
- Effect: +2 hearts (4 HP) max health
- Type: Passive
- Visual: Resistance particles when hit

**3. Bloodlust**
- Effect: +5% damage per consecutive hit on same target (max 3 stacks, 5s duration)
- Type: Passive
- Visual: Red eyes at max stacks

#### Tier 2 - Combat Mastery (10 pts each, choose 1)

**1. Critical Precision**
- Effect: 20% chance to deal +50% damage
- Type: Passive
- Visual: "CRIT!" text + enchanted hit particles

**2. Armor Breaker**
- Effect: 30% armor penetration
- Type: Passive
- Visual: Crack particles on hit

**3. Execution**
- Effect: +25% damage to targets below 30% health
- Type: Passive
- Visual: Target glows red when executable

#### Tier 3 - Deadly Arts (15 pts each, choose 1)

**1. Whirlwind Strike**
- Effect: Spin attack hitting all enemies in 4 block radius
- Type: Active (60s cooldown)
- Cost: 10 hunger
- Visual: 360° sweep particles

**2. Last Stand**
- Effect: Below 10 hearts → +30% damage & +20% damage resist for 10s
- Type: Passive (2 min cooldown)
- Visual: Glowing + flame particles

**3. Lifesteal**
- Effect: Heal 15% of damage dealt (PvP only)
- Type: Passive
- Visual: Green particles from victim to attacker

#### Tier 4 - War Machine (20 pts each, choose 1)

**1. Relentless Assault**
- Effect: Each kill grants +20% attack speed for 10s (stacks 3x)
- Type: Passive
- Visual: Speed lines particle effect

**2. Titan's Grip**
- Effect: +30% knockback resistance, +10% damage
- Type: Passive
- Visual: Stomp particles when landing

**3. Warrior's Resolve**
- Effect: Cannot drop below 1 HP for 5s after fatal damage (3 min cooldown)
- Type: Passive
- Visual: Golden shield particles

#### Ultimate - Tier 5 (25 pts)

**🔥 WARLORD'S RAMPAGE**
- Duration: 15 seconds
- Cooldown: 5 minutes
- Type: Active
- Effects:
  - +50% damage
  - +30% attack speed
  - +20% movement speed
  - Immune to knockback
  - Every kill restores 5 hearts
- Visual: Red aura, flame particles, glowing eyes
- Audio: Ender dragon roar on activation

---

### ⛏️ GATHERING TREE - "The Prospector"

**Theme:** Resource efficiency, wealth generation, exploration

#### Tier 1 - Harvester (5 pts each, choose 1)

**1. Fortune's Touch**
- Effect: +15% chance for double drops (ores/logs/crops)
- Stacks with territory bonuses
- Type: Passive

**2. Swift Hands**
- Effect: -20% block break time
- Type: Passive
- Visual: Haste particle swirls

**3. Treasure Hunter**
- Effect: See diamond/iron/gold ore glow through walls (8 block range)
- Type: Passive
- Visual: Subtle outline effect

#### Tier 2 - Master Gatherer (10 pts each, choose 1)

**1. Vein Miner**
- Effect: Breaking 1 ore breaks up to 5 connected ores
- Type: Active (30s cooldown)
- Cost: 15 hunger
- Visual: Chain explosion particles

**2. Green Thumb**
- Effect: Crops planted by you grow 25% faster, +20% yields
- Type: Passive
- Visual: Bonemeal particles on plant

**3. Lumberjack**
- Effect: Trees drop 50% more wood, breaking bottom log fells tree
- Type: Passive
- Visual: Timber falling particles

#### Tier 3 - Resource Specialist (15 pts each, choose 1)

**1. Diamond Hunter**
- Effect: Diamond ore drop rate 10% → 25%, reveal location within 32 blocks
- Type: Passive
- Visual: Diamond ore glows cyan with beam

**2. Efficient Fishing**
- Effect: -50% fishing time, +30% treasure chance
- Type: Passive
- Visual: Splash particles on bite

**3. Ore Transmutation**
- Effect: Convert 16 iron ore → 1 diamond (must be at home beacon)
- Type: Active (10 min cooldown)
- Visual: Alchemical swirl particles

#### Tier 4 - Master Craftsman (20 pts each, choose 1)

**1. Tool Durability**
- Effect: Tools last 2x longer, won't break (stop at 1 durability)
- Type: Passive

**2. Resource Magnet**
- Effect: Items fly to you from 8 blocks away
- Type: Passive
- Visual: Tracer particles

**3. Bulk Processing**
- Effect: 25% chance to smelt 2x items at once
- Type: Passive
- Visual: Extra furnace smoke

#### Ultimate - Tier 5 (25 pts)

**💎 MIDAS TOUCH**
- Type: Passive + Active
- Passive: +50% drop rate from all gathering
- Active (10 min cooldown): Convert 1 stack of any ore → random rare resources
  - Outputs: Diamond, gold, enchanted golden apples, obsidian
- Visual: Golden particle aura, items shimmer gold

---

### 🛡️ SURVIVAL TREE - "The Endurer"

**Theme:** Tanking, sustain, environmental resistance

#### Tier 1 - Basic Survival (5 pts each, choose 1)

**1. Hardy**
- Effect: +3 hearts (6 HP) max health
- Type: Passive
- Visual: Hearts show shield icon

**2. Thick Skin**
- Effect: -10% damage from all sources
- Type: Passive
- Visual: Iron particles when hit

**3. Hunger Resistance**
- Effect: Food lasts 30% longer, -20% hunger drain
- Type: Passive

#### Tier 2 - Wilderness Expert (10 pts each, choose 1)

**1. Regeneration**
- Effect: Regen 0.5 HP every 5s when out of combat (8s threshold)
- Type: Passive
- Visual: Green + particles

**2. Fire Immunity**
- Effect: Immune to fire/lava damage (still burning DoT after exit)
- Type: Passive
- Visual: Fire resistance particles

**3. Fall Damage Negation**
- Effect: No fall damage under 20 blocks, -50% above
- Type: Passive
- Visual: Cloud particles on landing

#### Tier 3 - Survivor (15 pts each, choose 1)

**1. Second Wind**
- Effect: Below 5 hearts → instantly heal 10 hearts (5 min cooldown)
- Type: Passive
- Visual: Burst of regeneration particles

**2. Poison/Wither Immunity**
- Effect: Immune to poison and wither effects
- Type: Passive
- Visual: Immunity particles

**3. Winter Adaptation**
- Effect: Immune to freezing damage, +20% damage in Winter
- Type: Passive
- Visual: Frost particles (cosmetic)

#### Tier 4 - Tank (20 pts each, choose 1)

**1. Damage Absorption**
- Effect: First hit every 30s deals 50% less damage
- Type: Passive
- Visual: Shield particle flash

**2. Thorns**
- Effect: Attackers take 20% of damage back
- Type: Passive
- Visual: Spike particles when hit

**3. Unstoppable**
- Effect: Immune to slowness/weakness/mining fatigue, -30% knockback
- Type: Passive
- Visual: Resistance particles

#### Ultimate - Tier 5 (25 pts)

**🛡️ IMMORTAL FORTRESS**
- Duration: 20 seconds
- Cooldown: 10 minutes
- Type: Active
- Effects:
  - 70% damage reduction
  - Immune to knockback
  - Immune to all debuffs
  - +5 hearts temporary health
  - Reflect 30% damage
- Visual: Golden dome shield, resistance particles
- Audio: Anvil sound on activation

---

### 👥 TEAMWORK TREE - "The Commander"

**Theme:** Team buffs, support abilities, coordination

#### Tier 1 - Team Player (5 pts each, choose 1)

**1. Shared Victory**
- Effect: +10% team points from your quests
- Type: Passive
- Visual: Gold coin particles on complete

**2. Rally Cry**
- Effect: Teammates in 20 blocks get +10% damage for 15s
- Type: Active (2 min cooldown)
- Visual: Battle cry particles

**3. Guardian Angel**
- Effect: Teleport to teammate taking fatal damage within 10 blocks (5 min cooldown)
- Type: Active (triggered)
- Visual: Angel wing particles

#### Tier 2 - Team Support (10 pts each, choose 1)

**1. Healer's Touch**
- Effect: Heal nearby teammate for 5 hearts (5 block range)
- Type: Active (60s cooldown)
- Cost: 20 hunger
- Visual: Green beam to target

**2. Pack Tactics**
- Effect: +5% damage per nearby teammate (max 20%, 15 block range)
- Type: Passive
- Visual: Link particles between allies

**3. Resource Sharing**
- Effect: 10% of gathered resources duplicated to random teammate
- Type: Passive
- Visual: Item copy particles

#### Tier 3 - Team Coordinator (15 pts each, choose 1)

**1. Tactical Retreat**
- Effect: Teleport to home beacon, can bring 1 teammate within 3 blocks
- Type: Active (5 min cooldown)
- Visual: Ender pearl particles

**2. Combat Medic**
- Effect: Reviving costs -25% team points, revived get +5 min protection
- Type: Passive
- Visual: Medical cross above revived

**3. Supply Drop**
- Effect: Drop care package (16 food, 8 golden apples, iron armor)
- Type: Active (10 min cooldown)
- Visual: Chest drops from sky with beacon

#### Tier 4 - Leader (20 pts each, choose 1)

**1. Inspirational Leader**
- Effect: All teammates get +10% XP, +5% gathering (50 block aura)
- Type: Passive
- Visual: Gold aura particles

**2. Strategic Mind**
- Effect: See all teammates through walls (glowing outline + health bars)
- Type: Passive
- Visual: Outlined silhouettes

**3. Last Stand Protocol**
- Effect: Teammate death in 20 blocks → +30% damage & resist for 30s (stacks 3x)
- Type: Passive
- Visual: Red aura grows per stack

#### Ultimate - Tier 5 (25 pts)

**👑 COMMANDER'S BLESSING**
- Duration: 30 seconds
- Cooldown: 15 minutes
- Type: Active (affects all living teammates server-wide)
- Effects:
  - +25% damage
  - +25% damage resistance
  - Regeneration II
  - Speed II
  - +50% XP gain
- Visual: Crown on caster, royal banner on all teammates
- Audio: Bell toll across server
- Chat: "⚔️ [Name] has rallied the team! Fight with honor! ⚔️"

---

## Skill Point Economy

### Earning Skill Points

**XP → Skill Points Conversion:**
- **500 XP = 1 Skill Point**

**XP Sources:**

| Source | XP Amount | Notes |
|--------|-----------|-------|
| Easy Quest | 100 XP | Daily quests |
| Medium Quest | 250 XP | Daily quests |
| Hard Quest | 500 XP | Weekly team quests |
| Player Kill | 200 XP | PvP combat |
| Zombie Kill | 10 XP | Common mob |
| Skeleton Kill | 15 XP | Common mob |
| Creeper Kill | 20 XP | Common mob |
| Enderman Kill | 50 XP | Rare mob |
| Stone Mined | 1 XP | Basic resource |
| Iron Ore Mined | 3 XP | Valuable resource |
| Diamond Ore | 10 XP | Rare resource |
| Territory Bonus | +100% XP | XP territory doubles gains |
| Blood Moon Event | 2x XP | Applies during event |

### Skill Point Caps

- **Max Total Skill Points**: 100 per player
- **Max Per Tree**: 75 points (can max 1 tree + partial second)
- **Ultimate Limit**: Only 2 Ultimate abilities can be unlocked
- **Tier Costs**: 5 → 10 → 15 → 20 → 25 (total 75 to max tree)

### Reset Mechanics

**Manual Reset:**
- Reset 1 tree: 500 team points (at home beacon only)
- Reset all trees: 1000 team points
- Refunds all skill points
- Instant reset

**Automatic Reset:**
- Triggers when difficulty cycle advances
- All skills reset to 0
- Skill points retained
- Encourages adaptation each cycle

---

## GUI Design

### Main Menu (`/skills` command)

```
╔═════════════════════════════════════════════════════╗
║         🌟 SKILL TREE MENU 🌟                       ║
║    Available Points: 23        Total Spent: 42      ║
╠═════════════════════════════════════════════════════╣
║                                                     ║
║  [🗡️ COMBAT]      [⛏️ GATHERING]                   ║
║   15 pts spent     8 pts spent                      ║
║   Click to view    Click to view                    ║
║                                                     ║
║  [🛡️ SURVIVAL]    [👥 TEAMWORK]                    ║
║   12 pts spent     7 pts spent                      ║
║   Click to view    Click to view                    ║
║                                                     ║
║                [❌ CLOSE]                           ║
╚═════════════════════════════════════════════════════╝
```

**Implementation:** Chest GUI (54 slots, 6 rows)

**Slot Layout:**
```
Row 1: Decorative (stained glass)
Row 2: [Empty] [Combat Book] [Empty] [Gathering Book] [Empty]
Row 3: [Empty] [15 pts] [Empty] [8 pts] [Empty]
Row 4: [Empty] [Survival Book] [Empty] [Teamwork Book] [Empty]
Row 5: [Empty] [12 pts] [Empty] [7 pts] [Empty]
Row 6: [Empty] [Empty] [Close Button] [Empty] [Empty]
```

### Individual Tree View

```
╔═════════════════════════════════════════════════════╗
║           🗡️ COMBAT SKILL TREE                      ║
║         Available Points: 23                        ║
╠═════════════════════════════════════════════════════╣
║  TIER 1 - Choose 1 (5 pts each)                    ║
║  [✓ Swift]   [○ Iron Skin]   [○ Bloodlust]         ║
║   UNLOCKED      LOCKED          LOCKED              ║
║                                                     ║
║  TIER 2 - Choose 1 (10 pts) [LOCKED - Need Tier 1] ║
║  [○ Critical]  [○ Armor Break]  [○ Execution]      ║
║                                                     ║
║  TIER 3 - Choose 1 (15 pts) [LOCKED]               ║
║  [○ Whirlwind]  [○ Last Stand]  [○ Lifesteal]     ║
║                                                     ║
║  TIER 4 - Choose 1 (20 pts) [LOCKED]               ║
║  [○ Relentless]  [○ Titan]  [○ Resolve]           ║
║                                                     ║
║  ULTIMATE (25 pts) [LOCKED]                        ║
║  [○ WARLORD'S RAMPAGE]                             ║
║                                                     ║
║           [⬅️ BACK]      [🔄 RESET - 500pts]       ║
╚═════════════════════════════════════════════════════╝
```

**Implementation:** Chest GUI (54 slots)

**Visual Indicators:**
- ✓ Green Wool = Unlocked
- ○ Red Wool = Locked (can't afford)
- ○ Gray Wool = Locked (tier requirement)
- ○ Yellow Wool = Available to unlock

### Skill Detail Tooltip

**Hover Over Skill:**
```
╔═════════════════════════════════════════════════════╗
║          🗡️ SWIFT STRIKES                          ║
╠═════════════════════════════════════════════════════╣
║  Cost: 5 Skill Points                              ║
║  Tier: 1                                            ║
║  Type: Passive                                      ║
║                                                     ║
║  Effect:                                            ║
║  • +15% Attack Speed                               ║
║  • Red particle trail on swings                    ║
║                                                     ║
║  Requirements:                                      ║
║  ✓ Combat Tree unlocked                            ║
║  ✓ Have 5 skill points                             ║
║                                                     ║
║        [LEFT-CLICK TO UNLOCK]                      ║
╚═════════════════════════════════════════════════════╝
```

**Implementation:** ItemMeta lore on GUI items

---

## Database Schema

### New Tables

#### `player_skills` Table

```sql
CREATE TABLE player_skills (
    player_uuid TEXT PRIMARY KEY,
    skill_points_available INTEGER DEFAULT 0,
    skill_points_spent INTEGER DEFAULT 0,
    total_xp_earned INTEGER DEFAULT 0,

    -- Combat tree (store skill names as TEXT)
    combat_tier1 TEXT DEFAULT NULL,
    combat_tier2 TEXT DEFAULT NULL,
    combat_tier3 TEXT DEFAULT NULL,
    combat_tier4 TEXT DEFAULT NULL,
    combat_ultimate BOOLEAN DEFAULT 0,

    -- Gathering tree
    gathering_tier1 TEXT DEFAULT NULL,
    gathering_tier2 TEXT DEFAULT NULL,
    gathering_tier3 TEXT DEFAULT NULL,
    gathering_tier4 TEXT DEFAULT NULL,
    gathering_ultimate BOOLEAN DEFAULT 0,

    -- Survival tree
    survival_tier1 TEXT DEFAULT NULL,
    survival_tier2 TEXT DEFAULT NULL,
    survival_tier3 TEXT DEFAULT NULL,
    survival_tier4 TEXT DEFAULT NULL,
    survival_ultimate BOOLEAN DEFAULT 0,

    -- Teamwork tree
    teamwork_tier1 TEXT DEFAULT NULL,
    teamwork_tier2 TEXT DEFAULT NULL,
    teamwork_tier3 TEXT DEFAULT NULL,
    teamwork_tier4 TEXT DEFAULT NULL,
    teamwork_ultimate BOOLEAN DEFAULT 0,

    -- Metadata
    last_reset_time LONG DEFAULT 0,
    ultimate_count INTEGER DEFAULT 0,  -- max 2

    FOREIGN KEY(player_uuid) REFERENCES players(uuid)
);
```

#### `skill_cooldowns` Table

```sql
CREATE TABLE skill_cooldowns (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid TEXT NOT NULL,
    skill_name TEXT NOT NULL,
    cooldown_end LONG NOT NULL,  -- System.currentTimeMillis()
    FOREIGN KEY(player_uuid) REFERENCES players(uuid)
);
```

#### `player_xp` Table (Optional - track XP separately)

```sql
CREATE TABLE player_xp (
    player_uuid TEXT PRIMARY KEY,
    total_xp INTEGER DEFAULT 0,
    current_xp INTEGER DEFAULT 0,  -- XP toward next skill point
    xp_multiplier DOUBLE DEFAULT 1.0,  -- From skills/events
    FOREIGN KEY(player_uuid) REFERENCES players(uuid)
);
```

---

## Commands

### Player Commands

```
/skills                    - Open skill tree GUI
/skills info <skill>       - View skill details in chat
/skills reset <tree>       - Reset specific tree (costs team points)
/skills reset all          - Reset all trees (costs team points)
/skills stats              - View your skill statistics
/skills active             - List active skills with cooldowns
/skills xp                 - Check XP and progress to next skill point
```

### Admin Commands (Add to `/soc`)

```
/soc skills give <player> <amount>        - Give skill points
/soc skills set <player> <amount>         - Set skill points
/soc skills reset <player> [tree]         - Force reset (free)
/soc skills unlock <player> <skill>       - Force unlock skill
/soc skills lock <player> <skill>         - Force lock skill
/soc skills info <player>                 - View player's skills
/soc skills reload                        - Reload skill configs
/soc skills clearall                      - Reset all players' skills
/soc skills givexp <player> <amount>      - Give XP to player
```

**Examples:**
```
/soc skills give Steve 10
/soc skills reset Alex combat
/soc skills unlock Notch warlords_rampage
/soc skills givexp Herobrine 5000
```

---

## Implementation Architecture

### New Java Classes

```
/managers/
  ├─ SkillManager.java           - Core skill logic, unlocking, validation
  ├─ SkillTreeGUI.java           - Inventory GUI handler
  ├─ XPManager.java               - XP tracking & conversion to skill points
  └─ SkillEffectManager.java      - Apply skill effects to players

/models/
  ├─ SkillTree.java               - Enum: COMBAT, GATHERING, SURVIVAL, TEAMWORK
  ├─ SkillTier.java               - Enum: TIER1, TIER2, TIER3, TIER4, ULTIMATE
  ├─ Skill.java                   - Skill definition (name, cost, tier, effects)
  ├─ PlayerSkills.java            - Player's skill state (unlocked skills)
  └─ SkillEffect.java             - Skill effect data class

/listeners/
  ├─ SkillGUIListener.java        - Handle GUI clicks (unlock skills)
  ├─ SkillEffectListener.java    - Apply passive skill effects
  ├─ SkillActiveListener.java    - Handle active skill triggers
  └─ XPGainListener.java          - Track XP from all sources

/commands/
  ├─ SkillsCommand.java           - Player /skills command
  └─ AdminSkillsCommand.java      - Admin /soc skills commands

/tasks/
  ├─ SkillCooldownTask.java       - Track and expire cooldowns
  └─ SkillEffectTask.java         - Apply periodic effects (regen, etc.)

/utils/
  ├─ SkillUtils.java               - Helper methods
  └─ XPUtils.java                  - XP calculation helpers
```

---

## Balance Considerations

### Power Curve

**Early Game (Cycle 1-2):**
- 10-20 skill points
- Tier 1-2 accessible
- 10-20% power increase

**Mid Game (Cycle 3-5):**
- 40-60 skill points
- Tier 3-4 accessible
- 30-40% power increase
- Team composition matters

**Late Game (Cycle 6-7):**
- 70-90 skill points
- Ultimates unlocked
- 50-60% power increase
- Coordinated builds dominate

**Apocalypse:**
- Skills reset
- Strategic re-skilling critical
- Fast XP gain in final confrontation

### Counter-Play

**Combat vs Survival:**
- Combat: High burst damage
- Survival: Sustain outlasts burst

**Gathering vs Combat:**
- Gathering: Wealth → better gear
- Combat: Direct power advantage

**Teamwork vs Solo:**
- Teamwork: Multiplies team effectiveness
- Solo builds: Strong 1v1

**Seasonal Optimization:**
- Winter: Survival tree dominant
- Summer: Combat tree strong
- Fall: Gathering maximizes harvest
- Spring: Balanced

---

## Configuration

### config.yml Additions

```yaml
skills:
  enabled: true

  # XP System
  xp_per_skill_point: 500
  max_skill_points: 100
  max_ultimate_unlocks: 2

  # Reset Costs
  reset_costs:
    single_tree: 500      # team points
    all_trees: 1000       # team points

  # Reset Behavior
  reset_on_cycle: true    # auto-reset when cycle advances
  reset_on_death: false   # keep skills on death

  # XP Sources
  xp_sources:
    quest_completion:
      easy: 100
      medium: 250
      hard: 500
    player_kill: 200
    mob_kill:
      zombie: 10
      skeleton: 15
      creeper: 20
      spider: 12
      enderman: 50
      wither_skeleton: 100
    gathering:
      stone: 1
      coal: 2
      iron: 3
      gold: 5
      diamond: 10
      log: 2
      wheat: 1

  # Multipliers
  territory_xp_bonus: 1.0        # Extra XP in XP territory
  blood_moon_xp_multiplier: 2.0  # 2x XP during blood moon
  cooldown_reduction_per_cycle: 0.05  # 5% faster cooldowns per cycle

  # Skill Specific Configs
  combat:
    swift_strikes:
      attack_speed_bonus: 0.15
    critical_precision:
      crit_chance: 0.20
      crit_multiplier: 1.50
    whirlwind_strike:
      radius: 4
      cooldown_seconds: 60
      hunger_cost: 10
    warlords_rampage:
      duration_seconds: 15
      cooldown_seconds: 300
      damage_bonus: 0.50
      attack_speed_bonus: 0.30
      movement_speed_bonus: 0.20
      heal_per_kill: 10
    # ... etc for all skills

  gathering:
    fortunes_touch:
      double_drop_chance: 0.15
    vein_miner:
      max_ores: 5
      cooldown_seconds: 30
      hunger_cost: 15
    diamond_hunter:
      drop_chance: 0.25  # overrides global 10%
      reveal_range: 32
    midas_touch:
      drop_bonus: 0.50
      cooldown_seconds: 600
    # ... etc

  survival:
    hardy:
      extra_hearts: 3
    regeneration:
      heal_amount: 0.5
      heal_interval_seconds: 5
      combat_timeout_seconds: 8
    second_wind:
      trigger_health: 10
      heal_amount: 10
      cooldown_seconds: 300
    immortal_fortress:
      duration_seconds: 20
      cooldown_seconds: 600
      damage_reduction: 0.70
      temp_hearts: 5
      reflect_damage: 0.30
    # ... etc

  teamwork:
    rally_cry:
      radius: 20
      damage_bonus: 0.10
      duration_seconds: 15
      cooldown_seconds: 120
    healers_touch:
      heal_amount: 10
      range: 5
      cooldown_seconds: 60
      hunger_cost: 20
    commanders_blessing:
      duration_seconds: 30
      cooldown_seconds: 900
      damage_bonus: 0.25
      damage_reduction: 0.25
      speed_level: 2
      regen_level: 2
      xp_bonus: 0.50
    # ... etc
```

---

## Implementation Phases

### Phase 1: Core System (Week 1)
**Goal:** Basic XP tracking and skill point system

**Tasks:**
- Create database tables
- Implement XPManager
- Create SkillManager (basic unlock logic)
- Add XP gain listeners (quests, kills, gathering)
- Create `/skills stats` command (text-based)

**Deliverables:**
- Players can earn XP
- XP converts to skill points
- Database saves skill points

---

### Phase 2: Data Models (Week 2)
**Goal:** Define all skills and their effects

**Tasks:**
- Create Skill enum with all 60+ skills
- Define SkillTree and SkillTier enums
- Create PlayerSkills model
- Implement skill validation logic (tier requirements, ultimate limits)
- Create skill config loading system

**Deliverables:**
- All skills defined in code
- Config.yml has all skill parameters
- Validation prevents invalid unlocks

---

### Phase 3: GUI System (Week 3)
**Goal:** Interactive skill tree GUI

**Tasks:**
- Create SkillTreeGUI (main menu)
- Create individual tree views
- Implement click handlers
- Add visual indicators (locked/unlocked)
- Create skill detail tooltips

**Deliverables:**
- `/skills` opens functional GUI
- Players can browse all trees
- Can unlock skills via clicks
- Visual feedback on unlocks

---

### Phase 4: Passive Skills (Week 4)
**Goal:** Implement all passive skill effects

**Tasks:**
- Combat passives (Swift Strikes, Critical, etc.)
- Gathering passives (Fortune's Touch, etc.)
- Survival passives (Hardy, Thick Skin, etc.)
- Teamwork passives (Pack Tactics, etc.)
- Event listeners for all passive triggers

**Deliverables:**
- All Tier 1-2 passives working
- Effects apply automatically
- Visual particles for effects

---

### Phase 5: Active Skills (Week 5)
**Goal:** Implement active abilities with cooldowns

**Tasks:**
- Create cooldown system
- Implement Tier 3-4 active skills
- Add skill trigger detection (right-click, etc.)
- Create visual/audio effects
- Implement hunger costs

**Deliverables:**
- Active skills work with cooldowns
- Cooldowns persist through restarts
- Visual feedback on activation

---

### Phase 6: Ultimate Abilities (Week 6)
**Goal:** Implement 4 ultimate abilities

**Tasks:**
- Warlord's Rampage (Combat)
- Midas Touch (Gathering)
- Immortal Fortress (Survival)
- Commander's Blessing (Teamwork)
- Dramatic effects and announcements

**Deliverables:**
- All 4 ultimates functional
- Server-wide announcements
- Epic visual effects

---

### Phase 7: Admin Tools (Week 7)
**Goal:** Admin commands and management

**Tasks:**
- Implement all `/soc skills` commands
- Create skill reset mechanics
- Add force unlock/lock
- Create XP give commands
- Add reload system

**Deliverables:**
- Admins can manage player skills
- Testing tools available
- Documentation complete

---

### Phase 8: Balance & Polish (Week 8)
**Goal:** Testing and refinement

**Tasks:**
- Balance skill costs
- Tune cooldowns
- Adjust multipliers
- Performance optimization
- Bug fixes
- Documentation

**Deliverables:**
- Balanced gameplay
- No major bugs
- Complete documentation
- Ready for production

---

## Testing Strategy

### Unit Testing

**Test Cases:**
1. XP gain from all sources
2. Skill point conversion (500 XP = 1 pt)
3. Skill unlocking with valid points
4. Tier requirement validation
5. Ultimate limit (max 2)
6. Cooldown tracking
7. Reset mechanics (manual & auto)

### Integration Testing

**Scenarios:**
1. Full cycle progression (earn XP → unlock skills → use abilities → reset on cycle)
2. Team synergy (multiple players with different builds)
3. PvP combat with skills
4. PvE gathering with skills
5. Territory bonuses + skills stacking
6. World events + skill XP bonuses

### Balance Testing

**Metrics to Track:**
1. Average skill points per cycle
2. Most popular skills (prevent must-picks)
3. Win rate by build type
4. Ultimate usage frequency
5. XP gain rates by source
6. Skill reset frequency

---

## Future Enhancements

### Post-Launch Ideas

1. **Prestige System**: After maxing skills, prestige for cosmetic rewards
2. **Skill Synergies**: Unlock special effects when combining specific skills
3. **Legendary Skills**: Ultra-rare tier above Ultimate (1 per server)
4. **Skill Challenges**: Special quests to unlock unique skills
5. **Team Skill Trees**: Shared team-wide skill tree
6. **Seasonal Skills**: Skills only available during specific seasons
7. **Achievement Skills**: Unlock by completing specific achievements

---

## Notes for Implementation

### Critical Design Decisions

1. **Inventory GUI vs Packet-Based**: Use Inventory GUI for simplicity (works on all clients)
2. **Reset on Cycle**: Yes - forces meta adaptation and prevents stale gameplay
3. **Ultimate Limit**: 2 maximum - creates meaningful choice
4. **XP Rate**: 500 per point - allows ~100 points over full cycle
5. **Persistence**: Skills persist on death, reset on cycle

### Performance Considerations

1. **Skill Effect Checks**: Only check for unlocked skills (avoid checking all 60+ skills per event)
2. **Cooldown Storage**: Use in-memory map + periodic DB saves
3. **GUI Rendering**: Cache GUI inventories, only rebuild on change
4. **Particle Effects**: Limit particle count for active skills
5. **Database Queries**: Batch load all skills on login, cache in memory

### Compatibility Notes

1. **Version**: Requires Spigot/Paper 1.20.1 (Particle API, newer GameRules)
2. **Dependencies**: None (uses vanilla Bukkit API)
3. **Database**: SQLite (same as rest of plugin)
4. **Config**: Extends existing config.yml

---

## Appendix: Full Skill List

### Combat Tree (15 skills)
1. Swift Strikes (T1)
2. Iron Skin (T1)
3. Bloodlust (T1)
4. Critical Precision (T2)
5. Armor Breaker (T2)
6. Execution (T2)
7. Whirlwind Strike (T3)
8. Last Stand (T3)
9. Lifesteal (T3)
10. Relentless Assault (T4)
11. Titan's Grip (T4)
12. Warrior's Resolve (T4)
13. Warlord's Rampage (T5)

### Gathering Tree (15 skills)
1. Fortune's Touch (T1)
2. Swift Hands (T1)
3. Treasure Hunter (T1)
4. Vein Miner (T2)
5. Green Thumb (T2)
6. Lumberjack (T2)
7. Diamond Hunter (T3)
8. Efficient Fishing (T3)
9. Ore Transmutation (T3)
10. Tool Durability (T4)
11. Resource Magnet (T4)
12. Bulk Processing (T4)
13. Midas Touch (T5)

### Survival Tree (15 skills)
1. Hardy (T1)
2. Thick Skin (T1)
3. Hunger Resistance (T1)
4. Regeneration (T2)
5. Fire Immunity (T2)
6. Fall Damage Negation (T2)
7. Second Wind (T3)
8. Poison/Wither Immunity (T3)
9. Winter Adaptation (T3)
10. Damage Absorption (T4)
11. Thorns (T4)
12. Unstoppable (T4)
13. Immortal Fortress (T5)

### Teamwork Tree (15 skills)
1. Shared Victory (T1)
2. Rally Cry (T1)
3. Guardian Angel (T1)
4. Healer's Touch (T2)
5. Pack Tactics (T2)
6. Resource Sharing (T2)
7. Tactical Retreat (T3)
8. Combat Medic (T3)
9. Supply Drop (T3)
10. Inspirational Leader (T4)
11. Strategic Mind (T4)
12. Last Stand Protocol (T4)
13. Commander's Blessing (T5)

**Total: 60 Skills**

---

**End of Document**

*This is a design document for future implementation. All mechanics, values, and systems are subject to change during development.*
