# 🛡️ Seasons of Conflict - Admin Guide

**Version:** 1.0.0
**For Server Administrators**

This guide provides comprehensive documentation for server administrators managing a Seasons of Conflict server.

---

## 📋 Table of Contents

- [Admin Commands](#admin-commands)
- [Configuration](#configuration)
- [Database Management](#database-management)
- [Server Setup](#server-setup)
- [Monitoring & Maintenance](#monitoring--maintenance)
- [Common Issues](#common-issues)
- [Advanced Configuration](#advanced-configuration)

---

## 🔧 Admin Commands

All admin commands require the `soc.admin` permission (default: OP only).

### Command Overview

```
/soc                           - Display all admin commands
/soc setseason <season>        - Force season change
/soc setcycle <number>         - Set difficulty cycle
/soc setpoints <team> <amount> - Set team quest points
/soc apocalypse [on|off]       - Toggle apocalypse mode
/soc revive <player>           - Admin revive (bypasses cost/limits)
/soc eliminate <team>          - Force team elimination
/soc territories               - View detailed territory status
/soc teams                     - View detailed team status
/soc gameinfo                  - View current game state
```

---

### Season Management

#### `/soc setseason <season>`

Force the server to change to a specific season immediately.

**Syntax:**
```
/soc setseason <SPRING|SUMMER|FALL|WINTER>
```

**Examples:**
```
/soc setseason SPRING   # Change to Spring
/soc setseason WINTER   # Change to Winter
```

**What it does:**
- Sets the current season immediately
- Applies all seasonal effects (regeneration, mob bonuses, etc.)
- Broadcasts season change to all players
- Resets season start date to today
- Does NOT advance the difficulty cycle

**Use cases:**
- Testing seasonal mechanics
- Correcting season after server downtime
- Event management (force Winter for PvP event, etc.)

---

### Difficulty Cycle Management

#### `/soc setcycle <number>`

Set the difficulty cycle (1-7, or 8+ for apocalypse).

**Syntax:**
```
/soc setcycle <1-7>
```

**Examples:**
```
/soc setcycle 1     # Reset to Cycle 1 (easiest)
/soc setcycle 5     # Jump to Cycle 5 (hard)
/soc setcycle 8     # Trigger apocalypse mode
```

**What it does:**
- Changes difficulty cycle immediately
- Applies mob damage/health multipliers
- Updates resource drop rates
- Adjusts world border size
- Updates revival cost
- Resets player revival counters (max 2 per cycle)

**Cycle Effects:**

| Cycle | Mob Damage | Mob Health | Resources | Border | Revival Cost |
|-------|-----------|-----------|-----------|--------|--------------|
| 1 | 1.0x | 1.0x | 100% | 5000 | 500 pts |
| 2 | 1.25x | 1.25x | 85% | 4500 | 600 pts |
| 3 | 1.5x | 1.5x | 70% | 4000 | 750 pts |
| 4 | 1.75x | 1.75x | 55% | 3500 | 1000 pts |
| 5 | 2.0x | 2.0x | 40% | 3000 | 1500 pts |
| 6 | 2.5x | 2.5x | 25% | 2500 | 2500 pts |
| 7 | 3.0x | 3.0x | 10% | 2000 | 5000 pts |
| 8+ | APOCALYPSE | | | | |

**Use cases:**
- Testing late-game mechanics
- Event setup
- Recovering from server issues

---

### Team Point Management

#### `/soc setpoints <team> <amount>`

Set a team's quest points to a specific value.

**Syntax:**
```
/soc setpoints <team_name|team_id> <amount>
```

**Examples:**
```
/soc setpoints North 5000     # Set Team North to 5000 points
/soc setpoints 1 5000          # Set Team 1 (North) to 5000 points
/soc setpoints east 0          # Reset Team East to 0 points
```

**Team IDs:**
- 1 = North (Aqua)
- 2 = West (Green)
- 3 = Center (Gold)
- 4 = East (Red)
- 5 = South (Blue)

**What it does:**
- Immediately sets team points (overrides current value)
- Logs change to server console
- Notifies online team members
- Saves to database

**Use cases:**
- Correcting point exploits/bugs
- Event rewards
- Testing revival system
- Compensating for server downtime

---

### Apocalypse Mode

#### `/soc apocalypse [on|off]`

Toggle or force apocalypse mode.

**Syntax:**
```
/soc apocalypse          # Toggle apocalypse on/off
/soc apocalypse on       # Force enable
/soc apocalypse start    # Force enable (alias)
/soc apocalypse off      # Force disable
/soc apocalypse stop     # Force disable (alias)
```

**What it does (when enabled):**
- Sets mob damage to 3.5x
- Sets mob health to 3.5x
- Sets resource drops to 40%
- Sets revival cost to 10,000 points
- Broadcasts dramatic announcement
- Sends title screen to all players
- Border begins shrinking 200 blocks/day

**What it does (when disabled):**
- Restores cycle-appropriate difficulty
- Stops border shrinking
- Broadcasts restoration message

**Use cases:**
- Testing endgame mechanics
- Event finale
- Emergency reset if triggered prematurely

---

### Admin Revival

#### `/soc revive <player>`

Revive a dead player without point cost or revival limits.

**Syntax:**
```
/soc revive <playername>
```

**Examples:**
```
/soc revive Steve
/soc revive Notch
```

**What it does:**
- Revives player to full health (40 hearts)
- Does NOT cost team points
- Does NOT count against revival limit (2 per cycle)
- Teleports player to team beacon
- Gives spawn protection (60 seconds)
- Sets gamemode to survival
- Logs admin action to console

**Conditions:**
- Player must be dead (alive status = false)
- Player's team must not be eliminated
- Player must be online

**Use cases:**
- Compensating for bugs/lag deaths
- Testing revival mechanics
- Event management
- Recovering from server crashes

---

### Team Elimination

#### `/soc eliminate <team>`

Force a team to be eliminated from the game.

**Syntax:**
```
/soc eliminate <team_name|team_id>
```

**Examples:**
```
/soc eliminate North
/soc eliminate 3
```

**What it does:**
- Marks team as eliminated
- Kills all living team members
- Sets all team members to spectator mode
- Releases all controlled territories
- Broadcasts elimination message
- Checks for win condition (if only 1 team left)

**Warning:** This action is irreversible without database editing.

**Use cases:**
- Removing inactive teams
- Event management
- Correcting exploits

---

### Territory Status

#### `/soc territories`

View detailed status of all 5 territories.

**Output includes:**
- Territory ID and name
- Current owner (or NEUTRAL)
- Bonus type and percentage
- Beacon coordinates
- Active capture attempts (if any)

**Example output:**
```
=== Territory Overview ===
1. Tundra Peaks
   Owner: North (Aqua)
   Bonus: ORE +50%
   Beacon: 0, 100, -1750
   
2. Dark Forest
   Owner: West (Green)
   Bonus: WOOD +50%
   Beacon: -1500, 80, 0
   
3. Contested Badlands
   Owner: [NEUTRAL]
   Bonus: XP +100%
   Beacon: 0, 90, 0
   ⚠ Being captured by East (75%)
```

**Use cases:**
- Monitoring territory control
- Identifying contested zones
- Event planning

---

### Team Status

#### `/soc teams`

View detailed status of all 5 teams.

**Output includes:**
- Team elimination status (ACTIVE/ELIMINATED)
- Team points
- Player count (alive/total)
- Number of controlled territories
- List of online players (marked if dead)

**Example output:**
```
=== Team Overview ===
[ACTIVE] North (Aqua)
   Players: 5/6 alive
   Points: 2500
   Territories: 2
   Online: Steve, Alex, Notch [DEAD], Herobrine, Jeb
   
[ELIMINATED] Center (Gold)
   Players: 0/4 alive
   Points: 0
   Territories: 0
   Online: Player1 [DEAD]
```

**Use cases:**
- Monitoring team balance
- Identifying inactive players
- Planning events

---

### Game State Info

#### `/soc gameinfo`

View current global game state.

**Output includes:**
- Current season
- Current cycle
- Apocalypse status
- World border size
- Current revival cost
- Number of active teams

**Example output:**
```
=== Game State ===
Season: WINTER
Cycle: 4
Apocalypse: No
World Border: 3500
Revival Cost: 1000
Active Teams: 3
```

**Use cases:**
- Quick status check
- Troubleshooting
- Monitoring game progression

---

## ⚙️ Configuration

Configuration file: `plugins/SeasonsOfConflict/config.yml`

### Game Settings

```yaml
game:
  world_name: "world"              # World to apply mechanics to
  world_border_center:
    x: 0
    z: 0
  initial_border_size: 5000        # Starting border size
  minimum_border_size: 500         # Apocalypse minimum
```

**Important:**
- Change `world_name` if using a custom world
- Border center should be 0,0 for symmetry
- Minimum border should be small to force confrontation

---

### Season Settings

```yaml
seasons:
  days_per_season: 30              # Real-world days per season
  spring:
    crop_growth_multiplier: 1.5    # Crop growth speed (1.5 = 50% faster)
    mob_spawn_multiplier: 0.5      # Mob spawn rate (0.5 = 50% fewer)
  summer:
    ore_drop_multiplier: 1.5       # Ore drop bonus (1.5 = 50% more)
    mob_damage_multiplier: 1.5     # Mob damage (1.5 = 50% more)
  fall:
    harvest_multiplier: 1.5        # Harvest bonus (1.5 = 50% more)
    all_drops_multiplier: 1.25     # All drops (1.25 = 25% more)
  winter:
    food_production_multiplier: 0.5  # Food production (0.5 = 50% less)
    freezing_damage: 2.0           # Outdoor damage (2.0 HP per 30s)
```

**Seasonal Effects:**

**Spring:**
- `crop_growth_multiplier`: Makes crops grow faster (1.5 = 50% chance to skip growth stage)
- `mob_spawn_multiplier`: Reduces hostile mob spawns (0.5 = cancel 50% of spawns)

**Summer:**
- `ore_drop_multiplier`: Bonus ore when mining (1.5 = 50% chance for double drops)
- `mob_damage_multiplier`: Mobs deal more damage (1.5 = 50% increase)

**Fall:**
- `harvest_multiplier`: Bonus crops when harvesting (1.5 = 50% chance for double)
- `all_drops_multiplier`: Bonus from all sources (1.25 = 25% chance for extra)

**Winter:**
- `food_production_multiplier`: Less food from crops/fishing/animals (0.5 = 50% chance items removed)
- `freezing_damage`: Damage to outdoor players (2.0 HP = 1 heart per 30 seconds)

**Tuning Tips:**
- Higher multipliers make seasons more impactful
- Balance risk/reward for each season
- Winter should be harsh to encourage PvP over farming

**Note:** Changing these mid-game takes effect immediately.

---

### Difficulty Cycles

```yaml
difficulty:
  cycles:
    1:
      mob_damage: 1.0              # Multiplier for mob damage
      mob_health: 1.0              # Multiplier for mob health
      resources: 1.0               # Multiplier for resource drops
      border: 5000                 # World border size
      revival_cost: 500            # Quest points to revive
    # ... cycles 2-7
  apocalypse:
    border_shrink_per_day: 200     # Blocks to shrink daily
    water_to_lava_chance: 0.001    # Water→lava (0.001 = 0.1%)
    fire_spawn_chance: 0.0005      # Random fire (0.0005 = 0.05%)
    ash_particle_density: 0.3      # Particle density (0.3 = 30%)
    enable_dark_sky: true          # Dark crimson sky
    enable_end_crystals: false     # End crystal spawns
```

**Tuning tips:**
- Higher `mob_damage` = harder combat
- Lower `resources` = scarcity, more PvP
- Smaller `border` = forced proximity
- Higher `revival_cost` = more permanent deaths

**Apocalypse Effects:**
- `border_shrink_per_day`: Blocks to shrink border daily (200 = aggressive)
- `water_to_lava_chance`: Probability water converts to lava per tick (0.001 = very slow)
- `fire_spawn_chance`: Probability fire spawns on blocks per tick (0.0005 = rare)
- `ash_particle_density`: Particle spawn chance around players (0.3 = moderate)
- `enable_dark_sky`: Forces permanent midnight with red storm clouds
- `enable_end_crystals`: Enables rare end crystal spawns for visual effect

**⚠️ DANGER:** Setting water_to_lava_chance or fire_spawn_chance too high (> 0.01) can destroy the world within minutes! Use extreme caution.

**Atmospheric Immersion:**
- These effects make apocalypse mode truly apocalyptic
- Water→lava makes water extremely valuable
- Random fires force constant vigilance
- Dark sky + ash creates oppressive atmosphere

---

### Player Settings

```yaml
player:
  max_health: 80                   # 40 hearts (20 HP = 10 hearts)
  max_revivals_per_cycle: 2        # Maximum revivals per player
  spawn_protection_seconds: 60     # Invulnerability after spawn/revive
  combat_log_threshold_seconds: 10 # Time to be "in combat"
```

**Important:**
- `max_health: 80` = 40 hearts (don't change unless you want different starting health)
- Increasing `max_revivals_per_cycle` makes the game more forgiving

---

### Territory Configuration

Each territory has:

```yaml
territories:
  1:
    name: "Tundra Peaks"
    bounds:                        # Rectangular region
      minX: -2500
      maxX: 2500
      minZ: -2500
      maxZ: -1000
    beacon:                        # Exact beacon location
      x: 0
      y: 100
      z: -1750
    bonus_type: "ORE"              # ORE, WOOD, CROP, XP, FISH
    base_bonus: 50                 # Percentage bonus
```

**Setup instructions:**
1. Place beacons at the specified coordinates before starting
2. Ensure beacons are powered (9x9 iron/diamond/netherite base)
3. Bounds should be large enough for meaningful control
4. Only territory 3 (Contested Badlands) should have `starting_owner: 0`

**Bonus types:**
- **ORE** - Bonus ore drops when mining
- **WOOD** - Bonus wood when breaking logs
- **CROP** - Bonus crops when harvesting (disabled in Winter)
- **XP** - Bonus experience from all sources
- **FISH** - Bonus fish when fishing

---

### Capture Settings

```yaml
capture:
  time_seconds: 300                # 5 minutes to capture
  radius: 10                       # Blocks from beacon to start capture
  defense_radius: 50               # Enemy presence pauses capture
  shield_duration_hours: 24        # (Future feature)
  shield_cooldown_hours: 72        # (Future feature)
  point_reward: 100                # Points awarded on capture
  point_steal_percent: 25          # % of enemy points stolen
```

**Tuning tips:**
- Lower `time_seconds` = faster captures, more territory changes
- Larger `radius` = easier to capture
- Larger `defense_radius` = easier to defend

---

### Quest Settings

```yaml
quests:
  daily_quest_count: 3             # Quests per player daily
  max_daily_completions: 5         # Maximum completions per day
```

---

### PvP Settings

```yaml
pvp:
  kill_reward_base: 50             # Base points for a kill
  kill_cooldown_hours: 12          # Same-victim cooldown
  bounty_threshold: 3              # Kills to get bounty
  bounty_levels:
    3: 25                          # +25 pts at 3 kills
    5: 50                          # +50 pts at 5 kills
    10: 100                        # +100 pts at 10 kills
```

**Tuning tips:**
- Higher `kill_reward_base` = more PvP incentive
- Lower `kill_cooldown_hours` = more farming potential (not recommended)
- Adjust bounties to encourage hunting skilled players

---

### Diamond Settings

```yaml
diamond:
  drop_chance: 0.10                # 10% chance (0.10 = 10%)
```

**Note:** This makes diamonds extremely rare to prevent diamond gear dominance.

---

### Team Configuration

```yaml
teams:
  1:
    name: "North"
    color: "AQUA"                  # Minecraft color code
  # ... teams 2-5
```

**Available colors:**
- AQUA, BLACK, BLUE, DARK_AQUA, DARK_BLUE, DARK_GRAY
- DARK_GREEN, DARK_PURPLE, DARK_RED, GOLD, GRAY, GREEN
- LIGHT_PURPLE, RED, WHITE, YELLOW

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

**Color codes:**
- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Aqua
- `&4` - Dark Red
- `&5` - Dark Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Aqua
- `&c` - Red
- `&d` - Light Purple
- `&e` - Yellow
- `&f` - White
- `&l` - Bold
- `&r` - Reset

---

### World Events Configuration

```yaml
world_events:
  enabled: true                    # Master toggle
  check_interval_hours: 2          # Check frequency

  blood_moon:
    enabled: true
    chance: 0.15                   # 15% chance
    duration_minutes: 30
    mob_spawn_multiplier: 2.0
    mob_damage_multiplier: 2.0
    mob_health_multiplier: 1.5

  meteor_shower:
    enabled: true
    chance: 0.10                   # 10% chance
    duration_minutes: 15
    meteors_per_minute: 3
    explosion_power: 2.0
    spawn_ores: true               # Create ore deposits

  aurora:
    enabled: true
    chance: 0.20                   # 20% chance
    duration_minutes: 60
    speed_amplifier: 2             # Speed II
    night_only: true

  fog:
    enabled: true
    chance: 0.15                   # 15% chance
    duration_minutes: 45
    blindness_amplifier: 0         # 0 = disabled

  heatwave:
    enabled: true
    chance: 0.12                   # 12% (Summer only)
    duration_minutes: 40
    damage_per_tick: 0.5           # 0.5 HP per 5s
```

**World Event System:**

**General Settings:**
- `enabled`: Master toggle for entire event system
- `check_interval_hours`: How often to check for new events (default: every 2 hours)

**Event Types:**

**1. Blood Moon (Most Dangerous)**
- Multiplies mob spawns and makes them stronger
- Forced nighttime for duration
- Perfect for combat-oriented players
- Most dangerous during higher difficulty cycles

**2. Meteor Shower (Resource Opportunity)**
- Random meteors fall from sky
- Explosions can damage players/structures
- Spawns valuable ore deposits at impact sites
- Risk vs. reward gameplay

**3. Aurora (Speed Boost)**
- Grants Speed effect to all living players
- Beautiful particle effects
- Best for travel and territory captures
- Can be restricted to nighttime only

**4. Fog (Reduced Visibility)**
- Dense cloud particles
- Optional blindness effect
- Creates tense atmosphere
- Good for ambushes

**5. Heatwave (Environmental Hazard)**
- Only occurs in Summer
- Damages players exposed to sky
- Forces indoor gameplay
- Adds seasonal variety

**Tuning Tips:**
- Total `chance` values should sum to < 1.0 (otherwise events too frequent)
- Adjust `check_interval_hours` to control event frequency
  - 1 hour = frequent events (1-3 per day)
  - 2 hours = moderate (1-2 per day)
  - 4 hours = rare (0-1 per day)
- Increase `duration_minutes` for longer-lasting events
- Balance difficulty multipliers with cycle progression
- Disable events during testing: `enabled: false`

**Event Announcements:**
- All events display dramatic title cards
- Chat messages announce start/end
- Players get clear visual/audio feedback

**Performance Notes:**
- Meteor showers can cause lag if `meteors_per_minute` is too high
- Particle-heavy events (aurora, fog) may impact low-end clients
- Consider reducing particle density if TPS drops

---

## 💾 Database Management

Database location: `plugins/SeasonsOfConflict/seasons.db`

The plugin uses **SQLite** for data persistence.

### Database Schema

**5 Tables:**

#### 1. `players`
Stores all player data.

| Column | Type | Description |
|--------|------|-------------|
| uuid | TEXT PRIMARY KEY | Player UUID |
| name | TEXT | Player name |
| teamId | INTEGER | Team ID (1-5) |
| health | DOUBLE | Current health (0-80) |
| alive | BOOLEAN | Living status |
| kills | INTEGER | Total kills |
| deaths | INTEGER | Total deaths |
| questPoints | INTEGER | (Deprecated, use team points) |
| revivals | INTEGER | Revivals used this cycle |

#### 2. `teams`
Stores team data.

| Column | Type | Description |
|--------|------|-------------|
| teamId | INTEGER PRIMARY KEY | Team ID (1-5) |
| name | TEXT | Team name |
| questPoints | INTEGER | Team quest points |
| eliminated | BOOLEAN | Elimination status |
| territoriesOwned | INTEGER | Territory count |

#### 3. `territories`
Stores territory ownership.

| Column | Type | Description |
|--------|------|-------------|
| territoryId | INTEGER PRIMARY KEY | Territory ID (1-5) |
| name | TEXT | Territory name |
| owner | INTEGER | Owning team ID (0 = neutral) |
| captureProgress | INTEGER | Capture % (0-100) |
| capturingTeam | INTEGER | Team attempting capture |

#### 4. `game_state`
Stores global game state (single row).

| Column | Type | Description |
|--------|------|-------------|
| currentSeason | TEXT | SPRING/SUMMER/FALL/WINTER |
| currentCycle | INTEGER | Difficulty cycle (1-7+) |
| apocalypseMode | BOOLEAN | Apocalypse active |
| dayInSeason | INTEGER | (Deprecated) |

#### 5. `kill_cooldowns`
Tracks PvP cooldowns.

| Column | Type | Description |
|--------|------|-------------|
| killerUuid | TEXT | Killer's UUID |
| victimUuid | TEXT | Victim's UUID |
| timestamp | LONG | Kill timestamp (ms) |

---

### Backup & Restore

**Backup:**
```bash
# Stop server first
cp plugins/SeasonsOfConflict/seasons.db seasons_backup.db
```

**Restore:**
```bash
# Stop server
cp seasons_backup.db plugins/SeasonsOfConflict/seasons.db
# Start server
```

**Automated backup (Linux):**
```bash
# Add to crontab
0 */6 * * * cp /path/to/server/plugins/SeasonsOfConflict/seasons.db /path/to/backups/seasons_$(date +\%Y\%m\%d_\%H\%M).db
```

---

### Manual Database Editing

**Using SQLite CLI:**
```bash
sqlite3 plugins/SeasonsOfConflict/seasons.db

# View all players
SELECT name, teamId, health, alive FROM players;

# Reset a player's deaths
UPDATE players SET deaths = 0 WHERE name = 'Steve';

# Give a team points
UPDATE teams SET questPoints = 5000 WHERE teamId = 1;

# Reset game to Cycle 1
UPDATE game_state SET currentCycle = 1, apocalypseMode = 0;

# Exit
.quit
```

**Warning:** Always backup before manual editing. Incorrect changes can corrupt the game state.

---

## 🖥️ Server Setup

### Recommended Server Specs

**Minimum:**
- CPU: 2 cores @ 2.5 GHz
- RAM: 4 GB allocated to Minecraft
- Storage: 10 GB SSD
- Players: 15-20

**Recommended:**
- CPU: 4 cores @ 3.0+ GHz
- RAM: 6-8 GB allocated to Minecraft
- Storage: 20 GB SSD
- Players: 20-30

---

### Installation Steps

1. **Install Java 17+**
```bash
java -version  # Verify Java 17 or higher
```

2. **Download Spigot/Paper 1.20.1**
```bash
wget https://download.getbukkit.org/spigot/spigot-1.20.1.jar
# OR use Paper (recommended):
wget https://api.papermc.io/v2/projects/paper/versions/1.20.1/builds/XXX/downloads/paper-1.20.1-XXX.jar
```

3. **First Server Run** (generates world)
```bash
java -Xmx4G -Xms4G -jar spigot-1.20.1.jar nogui
# Edit eula.txt, set eula=true
# Run again to generate world
```

4. **Install Plugin**
```bash
cp SeasonsOfConflict-1.0.0.jar plugins/
```

5. **Configure Beacons**
- Stop server
- Edit `plugins/SeasonsOfConflict/config.yml`
- Update beacon coordinates to match your world
- Ensure beacons exist at those locations

6. **Place Beacons In-World**
```
/tp 0 100 -1750      # Tundra Peaks (North)
# Place beacon on 9x9 iron/diamond/netherite pyramid

/tp -1500 80 0       # Dark Forest (West)
# Place beacon

/tp 0 90 0           # Contested Badlands (Center)
# Place beacon

/tp 1500 75 0        # Savanna Plains (East)
# Place beacon

/tp 0 65 1750        # Ocean Coast (South)
# Place beacon
```

7. **Start Server**
```bash
java -Xmx6G -Xms6G -jar spigot-1.20.1.jar nogui
```

---

### World Preparation

**Recommended world settings:**
- Seed: Choose one with varied biomes
- Size: 5000x5000 minimum
- Pre-generate chunks:
  ```bash
  # Using Chunky plugin
  /chunky radius 2500
  /chunky start
  ```

**Biome recommendations:**
- North: Snowy biomes (Tundra Peaks)
- West: Forest biomes (Dark Forest)
- Center: Badlands/Mesa (Contested Badlands)
- East: Plains/Savanna (Savanna Plains)
- South: Ocean/Beach (Ocean Coast)

---

## 📊 Monitoring & Maintenance

### Log Monitoring

**Important log messages:**
- `"Season changed to X"` - Season transition
- `"Applied cycle X scaling"` - Cycle advancement
- `"APOCALYPSE MODE ACTIVATED"` - Endgame triggered
- `"Team X eliminated"` - Team elimination
- `"X wins the game!"` - Victory

**Log location:**
```
logs/latest.log
```

---

### Performance Monitoring

**Check TPS (Ticks Per Second):**
```
/tps
```
- Target: 20 TPS
- Acceptable: 18+ TPS
- Poor: <15 TPS

**If TPS drops:**
1. Reduce view distance (`server.properties`)
2. Pre-generate chunks
3. Optimize entity counts
4. Upgrade server hardware

---

### Scheduled Tasks

The plugin runs 12 background tasks:

| Task | Interval | Purpose |
|------|----------|---------|
| CaptureTickTask | 1 second | Update territory captures |
| SeasonCheckTask | 1 hour | Check for season transitions |
| DailyResetTask | 10 minutes | Check for daily quest reset |
| ScoreboardUpdateTask | 2 seconds | Refresh player scoreboards |
| FreezingDamageTask | 30 seconds | Apply winter freezing damage |
| WeatherControlTask | 5 minutes | Control weather based on season |
| SeasonalParticlesTask | 3 seconds | Spawn seasonal particle effects |
| BossBarUpdateTask | 2 seconds | Update capture/season/border boss bars |
| CompassUpdateTask | 5 seconds | Update compass tracking targets |
| ApocalypseEffectsTask | 30 seconds | Apply apocalypse world effects |
| WorldEventCheckTask | 2 hours (configurable) | Check for new random events |
| WorldEventUpdateTask | 5 seconds | Update active event effects |

**New Tasks (v1.0.0):**

**WeatherControlTask:**
- Controls world weather based on season
- Spring: Normal cycle
- Summer: Always sunny
- Fall: Frequent rain
- Winter: Clear skies

**SeasonalParticlesTask:**
- Spawns ambient particles around players
- Spring: Cherry blossom petals
- Summer: Heat shimmer
- Fall: Falling leaves
- Winter: Snowflakes + white ash

**BossBarUpdateTask:**
- Updates 3 boss bar types:
  1. Capture progress (when near enemy beacon)
  2. Season transition timer (when < 7 days remaining)
  3. World border warning (apocalypse, when < 200 blocks)

**CompassUpdateTask:**
- Updates compass targets every 5 seconds
- Tracks: Home territory, nearest enemy territory, or nearest teammate

**ApocalypseEffectsTask:**
- Applies apocalypse atmospheric effects
- Water→lava conversion
- Random fire spawning
- Ash particles
- Dark sky maintenance
- Optional end crystals

**WorldEventCheckTask:**
- Checks if new random events should start
- Runs every 2 hours by default (configurable)
- Selects events based on probability

**WorldEventUpdateTask:**
- Updates active world event effects
- Spawns meteors, applies potion effects, etc.
- Only runs when an event is active

**Performance Impact:**
- Total overhead: ~5-10% CPU with all tasks
- Particle tasks can impact client FPS on low-end machines
- Apocalypse tasks increase load proportional to player count
- Consider disabling world events if TPS < 18

These run automatically. **Do not disable.**

---

### Player Management

**Assign player to team manually:**
- Currently not supported via command
- Must edit database:
  ```sql
  UPDATE players SET teamId = 1 WHERE name = 'Steve';
  ```

**Reset player stats:**
- Use database:
  ```sql
  UPDATE players SET kills = 0, deaths = 0 WHERE name = 'Steve';
  ```

---

## ⚠️ Common Issues

### Issue: Beacons not giving bonuses

**Causes:**
1. Team doesn't own territory
2. Player not inside territory bounds
3. Wrong activity (mining in WOOD territory)
4. Winter season (CROP disabled)

**Solution:**
- Check `/soc territories` for ownership
- Verify player location with F3
- Check season with `/soc gameinfo`

---

### Issue: Quests not generating

**Causes:**
1. Daily reset task not running
2. Database corruption

**Solution:**
- Check `DailyResetTask` in console logs
- Manually trigger: `/soc setseason <current_season>` (forces reset)

---

### Issue: Border not shrinking in apocalypse

**Causes:**
1. Apocalypse not actually active
2. Task not running

**Solution:**
- Check `/soc gameinfo` - should show "Apocalypse: YES"
- Restart server
- Manually trigger: `/soc apocalypse on`

---

### Issue: Players spawn with wrong health

**Causes:**
1. Config `max_health` changed mid-game
2. Database health value corrupted

**Solution:**
- Check config: should be `max_health: 80`
- Fix database:
  ```sql
  UPDATE players SET health = 80 WHERE alive = 1;
  ```

---

### Issue: Database locked errors

**Causes:**
1. Multiple servers running on same database
2. Backup script accessing database while server running

**Solution:**
- Ensure only one server instance
- Stop server before backups
- Check for zombie processes: `ps aux | grep java`

---

## 🔬 Advanced Configuration

### Custom Quest Types

Quests are hardcoded in `QuestManager.java`. To add new quest types:

1. Add enum to `QuestCategory.java`
2. Create template in `QuestManager.initializeQuestTemplates()`
3. Track progress in relevant listener

**Example quest categories:**
- MINE_STONE, MINE_IRON, MINE_DIAMOND
- KILL_ZOMBIES, KILL_SKELETONS, KILL_PLAYERS
- HARVEST_WHEAT, FISH, BREED_ANIMALS

---

### Custom Seasonal Effects

Seasonal effects are in `SeasonManager.java`:

**Current effects:**
- Spring: Regeneration potion effect
- Summer: Mob damage multiplier (1.5x)
- Fall: Harvest bonus multiplier (1.5x)
- Winter: Freezing damage task

**To modify:**
1. Edit `SeasonManager.applySeasonalEffects()`
2. Adjust multipliers in `SeasonManager.getMobSeasonMultiplier()`
3. Change freezing damage in `SeasonManager.applyFreezingDamage()`

---

### Territory Customization

**To add a 6th territory:**
1. Edit `config.yml`, add territory 6
2. Database: territories are auto-created from config
3. Place beacon in-world
4. Restart server

**To change bonus calculations:**
- Edit `TerritoryManager.applyTerritoryBonus()`
- Adjust percentages in config

---

### Win Condition Customization

Win conditions are in `GameManager.java`:

**Current:**
1. Last team with living members
2. (Apocalypse fallback) Most quest points at border minimum

**To add custom win conditions:**
- Edit `GameManager.checkWinCondition()`
- Example: Time-based (team with most points after X days)

---

## 📞 Support

**Plugin Issues:**
- GitHub: https://github.com/mkpvishnu/plugin1/issues
- Include: server logs, config.yml, database backup

**Server Performance:**
- Check Paper documentation: https://docs.papermc.io/
- Use Timings: `/timings on` → `/timings paste`

**Community:**
- Join our Discord: [Link TBD]
- SpigotMC Forums: [Link TBD]

---

## 📚 Additional Resources

**Development:**
- Spigot API: https://hub.spigotmc.org/javadocs/spigot/
- Plugin source: https://github.com/mkpvishnu/plugin1
- CLAUDE.md: Developer documentation in repo

**Minecraft Server:**
- Spigot: https://www.spigotmc.org/
- Paper (recommended): https://papermc.io/
- Server optimization: https://www.spigotmc.org/threads/guide-server-optimization.21726/

---

## 🔐 Permissions

Currently only one permission:

**`soc.admin`** (default: OP)
- All `/soc` commands
- Admin revive
- Game state manipulation

**Future permissions (not yet implemented):**
- `soc.moderator` - View commands only
- `soc.event` - Limited admin commands for events

---

## 📝 Changelog & Versioning

**Current Version: 1.0.0**

See GitHub releases for changelog: https://github.com/mkpvishnu/plugin1/releases

---

## 🛠️ Troubleshooting Checklist

**Before reporting issues:**

- [ ] Server running Java 17+
- [ ] Using Spigot/Paper 1.20.1
- [ ] Plugin version 1.0.0 or later
- [ ] Config.yml properly formatted (YAML validation)
- [ ] Beacons placed at correct coordinates
- [ ] Database not corrupted (can open with SQLite)
- [ ] No conflicting plugins (WorldGuard, GriefPrevention can interfere)
- [ ] Checked `/soc gameinfo` for game state
- [ ] Reviewed server logs for errors
- [ ] Tested on clean server (no other plugins)

---

**Last Updated:** 2024
**Plugin Version:** 1.0.0
**Maintained By:** mkpvishnu

---

**For player documentation, see [README.md](README.md)**
