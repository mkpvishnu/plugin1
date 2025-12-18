# ⚔️ Seasons of Conflict

**Version:** 1.0.0
**Minecraft:** 1.20.1 (Spigot/Paper)

> A hardcore survival experience where 5 teams battle through changing seasons, escalating difficulty, and territorial warfare. Only one team will survive.

---

## 🎮 What is Seasons of Conflict?

Seasons of Conflict is a team-based survival plugin where strategy matters as much as skill. You'll start with **40 hearts (80 HP)** - and when you die, you're out. Work with your team to control territories, complete quests, and survive through 4 seasons and 7 difficulty cycles before the apocalypse arrives.

### Core Concept
- **5 Teams** compete for dominance
- **5 Territories** provide strategic bonuses
- **4 Seasons** change the world every 30 days
- **7 Difficulty Cycles** make the world progressively deadlier
- **Permadeath** with limited revivals (max 2 per cycle)
- **Last Team Standing Wins**

---

## 🎯 How to Play

### Starting Your Journey

When you first join the server, you'll be **automatically assigned to one of 5 teams**:
- **Team North** (Aqua) - Home: Tundra Peaks
- **Team West** (Green) - Home: Dark Forest
- **Team Center** (Gold) - Contested Badlands (initially neutral)
- **Team East** (Red) - Home: Savanna Plains
- **Team South** (Blue) - Home: Ocean Coast

You spawn at your team's beacon with:
- **40 Hearts (80 HP)** - Your full health pool
- **60 seconds of spawn protection**
- **No gear** - you must gather everything

### Critical Rules

❌ **Permanent Restrictions:**
- **No Nether or End** - Portal creation is blocked
- **No Enchanting Tables** - Cannot be placed
- **Diamond Rarity** - Only 10% of diamond ore drops diamonds (hardcoded)
- **Iron Tier Max** - The game is balanced around iron equipment being endgame

💀 **Death System:**
- You start with **40 hearts (80 HP)**
- Health can be lost through combat, environmental damage, etc.
- **When you die (reach 0 HP), you enter Spectator Mode**
- You are **permanently dead** until revived
- Teammates can revive you using team points (costs 500-10,000 pts depending on cycle)
- **Maximum 2 revivals per player per cycle**
- Revival restores you to full 40 hearts
- When a new cycle begins, your revival count resets

**Important:** There is NO gradual health loss system. You either have full hearts or you're dead!

---

## 🗺️ Territory System

### The 5 Territories

Each territory has a **beacon** at its center and provides bonuses when controlled:

| Territory | Bonus Type | Bonus Amount | Location |
|-----------|-----------|--------------|----------|
| **Tundra Peaks** | Ore Mining | +50% ore drops | North (x:0, z:-1750) |
| **Dark Forest** | Wood Cutting | +50% wood drops | West (x:-1500, z:0) |
| **Contested Badlands** | Experience | +100% XP gains | Center (x:0, z:0) |
| **Savanna Plains** | Crop Farming | +100% crop drops | East (x:1500, z:0) |
| **Ocean Coast** | Fishing | +50% fish drops | South (x:0, z:1750) |

### How Territory Bonuses Work

Bonuses only apply when **ALL** conditions are met:
1. ✅ Your team owns the territory
2. ✅ You are physically inside the territory bounds
3. ✅ You are performing the matching activity (mining for ORE, chopping for WOOD, etc.)
4. ✅ Seasonal modifiers allow it (CROP is disabled in Winter)

### Capturing Territories

**Requirements:**
- Stand within **10 blocks of the enemy beacon**
- Have **3+ teammates** in range
- Hold the position for **5 minutes (300 seconds)**
- Enemy team members within **50 blocks** will pause capture progress

**Rewards:**
- Your team gains control of the territory
- **+100 team points** immediately
- **Steal 25%** of the enemy team's points
- Territory bonuses now apply to your team

**Commands:**
- `/territory info` - Check what territory you're in and who owns it
- `/territory map` - View all 5 territories and their owners

---

## 🌍 Seasonal System

The world cycles through **4 seasons**, each lasting **30 real-world days**. Seasons dramatically change how you play.

### 🌱 Spring (Days 1-30)
**Theme:** Renewal and Growth

**Effects:**
- ✅ **Regeneration I** - Passive healing potion effect for all living players
- ✅ **+50% Crop Growth** - Farms grow faster (config placeholder)
- ✅ **Fewer Mobs** - Reduced hostile spawn rates (config placeholder)
- 📍 **Best Territories:** Dark Forest (wood), Savanna Plains (crops)

**Strategy:** Focus on base building, farming, and stockpiling resources while healing is available.

---

### ☀️ Summer (Days 31-60)
**Theme:** Heat and Hostility

**Effects:**
- ⚠️ **Mob Damage +50%** - Mobs hit 1.5x harder
- ✅ **Ore Drops +50%** - Extra ore from mining (placeholder)
- ⚠️ **Aggressive Mobs** - Increased aggression (placeholder)
- 📍 **Best Territories:** Tundra Peaks (ore mining)

**Strategy:** Mine aggressively for resources, avoid unnecessary combat, fortify defenses.

---

### 🍂 Fall (Days 61-90)
**Theme:** Harvest Before Winter

**Effects:**
- ✅ **Harvest Bonus +50%** - 1.5x crop yields
- ✅ **All Drops +25%** - Increased drops (placeholder)
- ✅ **Resource Abundance** - Best time to gather
- 📍 **Best Territories:** Savanna Plains (crops), Ocean Coast (fishing)

**Strategy:** Gather as much food and resources as possible before Winter. Complete team quests.

---

### ❄️ Winter (Days 91-120)
**Theme:** Survival and Hardship

**Effects:**
- ⚠️ **Freezing Damage** - Take 1 heart (2 HP) damage every 30 seconds outdoors
- ⚠️ **Food Production -50%** - Reduced food from all sources (placeholder)
- ❌ **Crops Disabled** - CROP territory bonuses don't work
- ✅ **Heat Sources** - Stay within 5 blocks of heat sources to avoid freezing
- 📍 **Best Territories:** Tundra Peaks (ore), Contested Badlands (XP)

**Heat Sources (prevent freezing):**
- Campfire / Soul Campfire
- Furnace / Blast Furnace / Smoker
- Lava
- Fire / Soul Fire

**Strategy:** Stay underground or near heat sources. Focus on combat and territory control rather than farming.

---

## 📈 Difficulty Cycles

Every **120 days (after completing all 4 seasons)**, the difficulty increases. The world becomes more dangerous and resources become scarcer.

### Cycle Progression

| Cycle | Mob Damage | Mob Health | Resources | World Border | Revival Cost |
|-------|-----------|-----------|-----------|--------------|--------------|
| **1** | 1.0x | 1.0x | 100% | 5000 blocks | 500 pts |
| **2** | 1.25x | 1.25x | 85% | 4500 blocks | 600 pts |
| **3** | 1.5x | 1.5x | 70% | 4000 blocks | 750 pts |
| **4** | 1.75x | 1.75x | 55% | 3500 blocks | 1000 pts |
| **5** | 2.0x | 2.0x | 40% | 3000 blocks | 1500 pts |
| **6** | 2.5x | 2.5x | 25% | 2500 blocks | 2500 pts |
| **7** | 3.0x | 3.0x | 10% | 2000 blocks | 5000 pts |
| **8+** | **APOCALYPSE MODE** | | | | |

### 💀 Apocalypse Mode (Cycle 8+)

When the 7th cycle ends, **the world begins to collapse**:
- 🔥 **Border Shrinks** - Reduces by **200 blocks every day**
- 🔥 **Extreme Difficulty** - Mobs deal 3.5x damage, have 3.5x health
- 🔥 **Severe Resource Scarcity** - Only 40% of normal resources
- 🔥 **Revival Cost 10,000 pts** - Nearly impossible to revive
- 🔥 **Final Confrontation** - Teams are forced to fight

**The apocalypse continues until only one team remains.**

---

## 📜 Quest System

Quests are your primary source of **team points**, which are used for revivals and shop purchases.

### Daily Quests

Every player receives **3 random daily quests** at midnight (server time). Quests are weighted based on season - you're more likely to get combat quests in Summer, gathering quests in Fall, etc.

### Complete Quest List

#### 🗡️ Daily Combat Quests (5 types)
| Quest | Target | Reward |
|-------|--------|--------|
| Kill 30 zombies | 30 zombies | 15 pts |
| Kill 15 skeletons | 15 skeletons | 15 pts |
| Kill 10 creepers | 10 creepers | 20 pts |
| Kill an enemy player | 1 player | 50 pts |
| Survive 30 min in enemy territory | 30 minutes | 40 pts |

#### ⛏️ Daily Gathering Quests (5 types)
| Quest | Target | Reward |
|-------|--------|--------|
| Mine 32 iron ore | 32 iron ore | 20 pts |
| Mine 64 coal ore | 64 coal | 10 pts |
| Harvest 64 wheat | 64 wheat | 15 pts |
| Catch 16 fish | 16 fish | 20 pts |
| Chop 64 logs | 64 logs | 15 pts |

#### 🧭 Daily Exploration Quests (3 types)
| Quest | Target | Reward |
|-------|--------|--------|
| Travel 1000 blocks | 1000 blocks | 15 pts |
| Visit 3 different territories | 3 territories | 25 pts |
| Discover a structure | 1 structure | 30 pts |

#### 🛡️ Daily Survival Quests (3 types)
| Quest | Target | Reward |
|-------|--------|--------|
| Survive 24 hours without dying | 24 hours (1440 min) | 50 pts |
| Eat 8 different foods | 8 food types | 20 pts |
| Survive night outdoors | 1 night | 25 pts |

#### 🏆 Weekly Team Quests (5 types)
| Quest | Target | Reward |
|-------|--------|--------|
| Control home territory 7 days | 7 days | 300 pts |
| Capture an enemy territory | 1 territory | 400 pts |
| Kill 5 enemy players as team | 5 kills | 350 pts |
| All members survive the week | 7 days | 500 pts |
| Build 500+ block structure | 500 blocks | 200 pts |

**Total:** 21 different quest types (16 daily + 5 weekly)

**Commands:**
- `/quest` or `/q` - View your active quests and progress
- Quest progress is tracked automatically

---

## 💚 Death & Revival System

### How Death Works

**You have 40 hearts (80 HP) when alive.**

When your health reaches 0:
1. You enter **Spectator Mode**
2. You are marked as **dead** in the system
3. Your kill streak and bounty reset to 0
4. You can watch the game continue
5. Your teammates can revive you (if you have revivals remaining)

**There is NO gradual health loss!** You don't lose hearts permanently per death - you either have full health or you're dead.

### Revival System

**How to Revive:**
- A living teammate uses `/revive <your_name>`
- Costs **500-10,000 team points** (depends on current cycle - see table above)
- **Max 2 revivals per player per cycle**
- Revival counter resets when difficulty cycle advances

**Revival Process:**
1. Team points are deducted from your team
2. You teleport to your team's home beacon
3. **Full health restored** (40 hearts)
4. **No gear** - must be re-equipped by teammates
5. **60 seconds spawn protection** (Resistance V + Regeneration III)
6. Set to Survival gamemode

**Important:**
- You cannot revive yourself (must be done by a living teammate)
- If all teammates are dead, your team is **ELIMINATED**
- If you've used 2 revivals this cycle, you must wait for the next cycle
- Points are shared across the team, so spend wisely

**Commands:**
- `/revive <player>` - Spend team points to revive a dead teammate

---

## 🏪 Team Shop

Spend team points to purchase items and upgrades.

### Shop Items

| Item | Cost | Description | Command |
|------|------|-------------|---------|
| **Golden Apple x2** | 150 pts | Instant healing and absorption | `/shop buy apples` |
| **Iron Armor Set** | 200 pts | Full iron helmet, chest, legs, boots | `/shop buy armor` |

**Note:** Revival costs 500-10,000 pts depending on cycle (see Difficulty Cycles table).

**Commands:**
- `/shop` - View available shop items
- `/shop buy <item>` - Purchase an item

**Notes:**
- Only living players can purchase items
- Requires sufficient team points
- Items are given directly to your inventory

---

## ⚔️ PvP & Combat

### Kill Rewards

Killing enemy players grants **team points** based on their kill streak:
- **Base Reward:** 50 points
- **Bounty System:** Players with 3+ kills have bounties
  - 3 kills = +25 pts
  - 5 kills = +50 pts
  - 10 kills = +100 pts

### Kill Cooldown

You can only gain points from killing the same player once every **12 hours**.
- Prevents farming the same player repeatedly
- Cooldown is per-victim, not global
- Killing different players has no cooldown

### Combat Tips

1. **Health Doesn't Regenerate Naturally** - Every hit matters (Spring season gives Regeneration potion effect)
2. **Golden Apples** - Your primary source of healing
3. **Iron Gear** - No enchantments, so skill > gear
4. **Spawn Protection** - Newly spawned/revived players have 60s immunity
5. **Bounties** - High-kill players are valuable targets

---

## 🏆 Victory Conditions

### Primary Win Condition
**Last team with at least one living member wins.**

If your team is the only team with surviving players, you win immediately.

### Secondary Win Condition
If the world border reaches minimum size (100 blocks) during apocalypse:
**Team with the most quest points wins.**

### Team Elimination

Your team is eliminated when:
- All team members are permanently dead (and no revivals remaining or no points to revive)
- Server announces: "Team [Name] has been ELIMINATED!"

Eliminated teams:
- Cannot respawn (even with points)
- Remain in spectator mode
- Lose all territory control

---

## 📊 Commands Reference

### Player Commands

| Command | Aliases | Description | Example |
|---------|---------|-------------|---------|
| `/team` | `/t` | View your team info and members | `/team` |
| `/team list` | `/t list` | View all teams and their status | `/team list` |
| `/quest` | `/q` | View your active quests | `/quest` |
| `/territory info` | `/terr info` | Check current territory | `/territory info` |
| `/territory map` | `/terr map` | View all territories | `/territory map` |
| `/revive <player>` | - | Revive a dead teammate | `/revive Steve` |
| `/shop` | - | View team shop | `/shop` |
| `/shop buy <item>` | - | Purchase shop item | `/shop buy apples` |
| `/stats` | - | View your statistics | `/stats` |
| `/stats <player>` | - | View another player's stats | `/stats Alex` |
| `/leaderboard` | `/lb` | View kill leaderboard | `/leaderboard` |
| `/leaderboard teams` | `/lb teams` | View team rankings | `/lb teams` |

---

## 📈 Statistics & Leaderboards

### Player Stats

View with `/stats [player]`:
- **Team** - Your team name and color
- **Kills** - Total enemy kills
- **Deaths** - Total deaths
- **K/D Ratio** - Kill/Death ratio
- **Kill Streak** - Current consecutive kills without dying
- **Bounty** - Bonus points for killing you
- **Revivals Used** - How many revivals used this cycle (max 2)

### Leaderboards

View with `/leaderboard [type]`:
- **Top Kills** - `/lb` or `/lb kills` - Top 10 players by kills
- **Team Rankings** - `/lb teams` or `/lb points` - Teams by points and alive members

---

## 🎯 Strategy Guide

### Early Game (Cycle 1, Spring/Summer)

1. **Gather Resources Fast**
   - Build iron gear before engaging in combat
   - Stockpile food (you'll need it in Winter)
   - Complete daily quests to build team points for future revivals

2. **Secure Your Home Territory**
   - Fortify your team's starting beacon
   - Build farms and sustainable food sources
   - Create storage for shared resources

3. **Avoid Unnecessary Deaths**
   - You only get 2 revivals per cycle
   - Don't engage in PvP unless you have advantage
   - Golden apples are precious—use wisely

### Mid Game (Cycles 2-4, Fall/Winter)

1. **Territory Control**
   - Attempt to capture Contested Badlands (center, XP bonus)
   - Defend your home territory
   - Use seasonal bonuses strategically

2. **Team Coordination**
   - Share resources with dead teammates after revival
   - Coordinate quest completion for maximum points
   - Plan territory captures (need 3+ players)

3. **Winter Survival**
   - Build underground bases with heat sources
   - Stockpile food during Fall
   - Focus on ore mining and XP grinding

### Late Game (Cycles 5-7, Approaching Apocalypse)

1. **Resource Conservation**
   - Resources spawn at 10-40% normal rates
   - Don't waste materials on unnecessary builds
   - Prioritize iron for armor/weapons over tools

2. **Aggressive Play**
   - Eliminate weak teams before apocalypse
   - Steal territory points from enemies
   - High bounty targets are worth the risk

3. **Point Management**
   - Revivals cost 1500-5000 points in late cycles
   - Keep emergency points for critical revivals
   - Don't waste points on shop items unless necessary

### Apocalypse (Cycle 8+)

1. **Border Awareness**
   - Border shrinks 200 blocks/day
   - Move toward center proactively
   - Don't get caught outside border

2. **Final Confrontation**
   - All remaining teams will be forced to fight
   - Ambush strategies work well in shrinking space
   - Every kill matters—go for bounties

3. **Survival**
   - One living teammate = victory
   - Protect your last living members
   - Coordinate as a team even when dead (spectators can scout)

---
## ⚙️ Configuration Reference

Configuration file: `plugins/SeasonsOfConflict/config.yml`

### Game Settings

```yaml
game:
  world_name: "world"              # World name to apply plugin mechanics to
  world_border_center:
    x: 0                           # Border center X coordinate
    z: 0                           # Border center Z coordinate
  initial_border_size: 5000        # Starting border size in blocks
  minimum_border_size: 500         # Minimum border size (apocalypse)
```

**Explanations:**
- `world_name`: The name of your world. Change if using custom world names.
- `world_border_center`: Center point of the world border (usually 0,0 for symmetry)
- `initial_border_size`: Starting world border diameter in blocks
- `minimum_border_size`: Smallest the border can shrink to during apocalypse

---

### Season Settings

```yaml
seasons:
  days_per_season: 30              # Real-world days per season
```

**Explanation:**
- `days_per_season`: How many real-world days before the season changes (Spring→Summer→Fall→Winter). Default is 30 days.

---

### Difficulty Cycles

```yaml
difficulty:
  cycles:
    1:
      mob_damage: 1.0              # Multiplier for mob attack damage
      mob_health: 1.0              # Multiplier for mob health
      resources: 1.0               # Multiplier for resource drops
      border: 5000                 # World border size in blocks
      revival_cost: 500            # Team points required to revive a player
    2:
      mob_damage: 1.25
      mob_health: 1.25
      resources: 0.85              # 85% of normal resource drops
      border: 4500
      revival_cost: 600
    # ... cycles 3-7 follow same pattern
  apocalypse:
    border_shrink_per_day: 200     # Blocks to shrink border daily in apocalypse
```

**Explanations:**
- `mob_damage`: Damage multiplier for all hostile mobs (1.0 = normal, 2.0 = double damage)
- `mob_health`: Health multiplier for all hostile mobs (1.5 = 50% more health)
- `resources`: Drop rate multiplier (0.85 = 85% of normal drops, 0.10 = 10% of normal)
- `border`: World border diameter in blocks for this cycle
- `revival_cost`: **Team points** required to revive one player (increases per cycle)
- `border_shrink_per_day`: How many blocks the border shrinks each day during apocalypse mode

---

### Player Settings

```yaml
player:
  max_health: 80                   # Maximum health (20 HP = 10 hearts, 80 HP = 40 hearts)
  max_revivals_per_cycle: 2        # Maximum times a player can be revived per cycle
  spawn_protection_seconds: 60     # Invulnerability duration after spawn/revive
  combat_log_threshold_seconds: 10 # Seconds to be considered "in combat"
```

**Explanations:**
- `max_health`: Player's maximum health points (80 = 40 hearts). **DO NOT CHANGE unless you want different starting health!**
- `max_revivals_per_cycle`: How many times a player can be revived before needing a new cycle
- `spawn_protection_seconds`: Duration of invulnerability after spawning or being revived
- `combat_log_threshold_seconds`: Time window to track combat status (future feature)

---

### Territory Configuration

```yaml
territories:
  1:
    name: "Tundra Peaks"           # Display name
    bounds:                        # Rectangular region
      minX: -2500
      maxX: 2500
      minZ: -2500
      maxZ: -1000
    beacon:                        # Exact beacon location (must match in-game beacon)
      x: 0
      y: 100
      z: -1750
    bonus_type: "ORE"              # Bonus type: ORE, WOOD, CROP, XP, FISH
    base_bonus: 50                 # Percentage bonus (50 = +50%)
  # ... territories 2-5 follow same pattern
  3:
    name: "Contested Badlands"
    # ... other settings
    starting_owner: 0              # 0 = neutral, 1-5 = team ID
```

**Explanations:**
- `name`: Territory display name shown to players
- `bounds`: Rectangular boundaries (minX, maxX, minZ, maxZ) defining territory region
- `beacon.x/y/z`: **Exact coordinates where the beacon must be placed in-game**
- `bonus_type`: Type of bonus granted to owner
  - `ORE` - Extra ore drops when mining
  - `WOOD` - Extra wood when breaking logs
  - `CROP` - Extra crops when harvesting (disabled in Winter)
  - `XP` - Bonus experience from all sources
  - `FISH` - Extra fish when fishing
- `base_bonus`: Percentage bonus (50 = +50% drops, 100 = +100% drops)
- `starting_owner`: Which team owns at start (0 = neutral, 1-5 = team ID). **Only territory 3 should be 0!**

---

### Capture Settings

```yaml
capture:
  time_seconds: 300                # Time to fully capture (5 minutes)
  radius: 10                       # Blocks from beacon to start capture
  defense_radius: 50               # Enemy presence within this range pauses capture
  shield_duration_hours: 24        # (Future feature) Shield duration
  shield_cooldown_hours: 72        # (Future feature) Shield cooldown
  point_reward: 100                # Team points awarded on successful capture
  point_steal_percent: 25          # % of enemy team's points stolen on capture
```

**Explanations:**
- `time_seconds`: Total seconds needed to capture (with 3+ players in range)
- `radius`: Distance from beacon center to start capturing
- `defense_radius`: If enemy player is within this distance, capture progress pauses
- `point_reward`: Team points awarded immediately upon successful capture
- `point_steal_percent`: Percentage of the enemy team's points stolen (25 = 25%)

---

### Quest Settings

```yaml
quests:
  daily_quest_count: 3             # Number of quests assigned per player daily
  max_daily_completions: 5         # Maximum quests a player can complete per day
```

**Explanations:**
- `daily_quest_count`: How many random quests each player receives at midnight
- `max_daily_completions`: Total quests a player can complete in one day (prevents farming)

---

### PvP Settings

```yaml
pvp:
  kill_reward_base: 50             # Base points awarded for killing enemy player
  kill_cooldown_hours: 12          # Hours before you can get points from same victim again
  bounty_threshold: 3              # Kill streak needed to get a bounty
  bounty_levels:
    3: 25                          # At 3 kills, add +25 pts to killer's reward
    5: 50                          # At 5 kills, add +50 pts
    10: 100                        # At 10 kills, add +100 pts
```

**Explanations:**
- `kill_reward_base`: Base team points for killing any enemy player
- `kill_cooldown_hours`: Cooldown before gaining points from the same victim (prevents farming)
- `bounty_threshold`: Kill streak needed before a player gets a bounty
- `bounty_levels`: Extra points added to killer's reward based on victim's kill streak

---

### Diamond Settings

```yaml
diamond:
  drop_chance: 0.10                # Chance of diamond ore dropping diamonds (0.10 = 10%)
```

**Explanation:**
- `drop_chance`: Probability that diamond ore will drop diamonds (0.10 = 10%, 1.0 = 100%)

**⚠️ IMPORTANT:** This setting is currently **HARDCODED at 10%** in `BlockBreakListener.java` and does NOT read from config! This will be fixed in a future update.

---

### Team Configuration

```yaml
teams:
  1:
    name: "North"                  # Team name
    color: "AQUA"                  # Minecraft color code
  2:
    name: "West"
    color: "GREEN"
  # ... teams 3-5
```

**Explanations:**
- `name`: Team display name
- `color`: Minecraft color code for team identification

**Available colors:**
- AQUA, BLACK, BLUE, DARK_AQUA, DARK_BLUE, DARK_GRAY, DARK_GREEN
- DARK_PURPLE, DARK_RED, GOLD, GRAY, GREEN, LIGHT_PURPLE, RED, WHITE, YELLOW

---

### Messages

```yaml
messages:
  prefix: "&6[SoC]&r "
  team_eliminated: "&c{team} has been ELIMINATED!"
  territory_captured: "&a{team} captured {territory}!"
  season_change: "&eThe season has changed to {season}!"
  cycle_advance: "&cCycle {cycle} begins. The world grows harsher..."
  apocalypse_start: "&4=== APOCALYPSE MODE ACTIVATED ==="
  game_end: "&6{team} WINS! Congratulations!"
```

**Explanation:**
- These are customizable server messages with color codes
- Use `{team}`, `{territory}`, `{season}`, `{cycle}` as placeholders
- **Color codes:**
  - `&0-9, a-f` - Colors (0=black, 1=dark blue, ... a=green, b=aqua, ... f=white)
  - `&l` - Bold, `&m` - Strikethrough, `&n` - Underline, `&o` - Italic
  - `&r` - Reset formatting

---

## 🖥️ Installation & Setup

### Requirements
- **Java 17+**
- **Spigot or Paper 1.20.1**
- **5000x5000 world** with varied biomes

### Installation

1. Download `SeasonsOfConflict-1.0.0.jar`
2. Place in your server's `plugins/` folder
3. Restart server
4. **Configure beacon locations** in `plugins/SeasonsOfConflict/config.yml`
5. **Place beacons in-world** at configured coordinates (on powered pyramids)
6. Adjust team names, colors, and difficulty settings as needed
7. Restart server again

### Building from Source

```bash
git clone https://github.com/mkpvishnu/plugin1.git
cd plugin1
mvn clean package
cp target/SeasonsOfConflict-1.0.0.jar /path/to/server/plugins/
```

---

## ⚠️ Known Limitations

The following features are **not yet implemented** or have limitations:

1. **Scoreboard/HUD Display** - Team, territory, season, and cycle info is NOT displayed on screen yet (planned feature)
2. **Hardcore Mode** - World is in Survival mode, not Hardcore mode
3. **Food Regeneration System** - Natural regeneration from food is enabled (NOT season-based yet)
4. **Diamond Scarcity Config** - Diamond drop chance is hardcoded at 10%, NOT read from config.yml
5. **Territory Shields** - Shop items for territory shields are placeholders (not functional)

---

## 🐛 Support

**Found a bug?** Report it at: https://github.com/mkpvishnu/plugin1/issues

**Need help?** Check the [ADMIN.md](ADMIN.md) for server administration guide.

---

## 📜 Credits

- **Developer:** mkpvishnu
- **Co-Developer:** Claude (Anthropic)
- **Framework:** Spigot API 1.20.1
- **License:** MIT License

---

## 🔥 Quick Start Checklist

- [ ] Join the server and note your team assignment
- [ ] Read `/team` to see your teammates
- [ ] Check `/quest` for your daily quests
- [ ] Gather basic resources (wood, stone, food)
- [ ] Check `/territory map` to see territory ownership
- [ ] Complete at least 1 quest to earn team points
- [ ] Remember: when you die, you go to spectator mode until revived!
- [ ] Coordinate with your team in chat
- [ ] Prepare for seasonal changes
- [ ] Survive and outlast the other teams!

---

**Good luck, survivor. May your team be the last one standing.**

*Seasons of Conflict v1.0.0 - Where survival meets strategy*
