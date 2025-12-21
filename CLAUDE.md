# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Seasons of Conflict** is a hardcore survival Minecraft plugin (Spigot 1.20.1, Java 17) featuring team-based gameplay with permadeath, progressive difficulty scaling, seasonal mechanics, and territory control. The plugin creates a strategic survival experience where 5 teams compete as the world becomes increasingly hostile over 7 difficulty cycles, culminating in an "apocalypse mode."

**Core Game Loop:**
- Players spawn into 1 of 5 teams with 40 hearts (80 HP) permadeath
- Teams capture and control territories for resource bonuses (ORE, WOOD, CROP, XP, FISH)
- Complete daily quests to earn team points for revivals and upgrades
- Survive through 4 seasons (Spring/Summer/Fall/Winter) with unique effects
- Progress through 7 difficulty cycles with escalating mob strength and decreasing resources
- Last team with living members wins

## Build Commands

```bash
# Standard build (produces target/SeasonsOfConflict-1.0.0.jar)
mvn clean package

# Fast build (skip test compilation)
mvn clean package -DskipTests

# Clean build artifacts
mvn clean

# View dependency tree
mvn dependency:tree

# Alternative: Use provided scripts
./build.sh        # Linux/macOS
.\build.bat       # Windows
```

**Build Output:** `target/SeasonsOfConflict-1.0.0.jar` (~50-100 KB compiled, ~500 KB with SQLite bundled)

**Requirements:** Java 17+, Maven 3.6+

## Architecture Overview

### Initialization Flow (SeasonsOfConflict.java:27-120)

The main plugin class follows this startup sequence:
1. **DataManager** initializes SQLite database (creates 5 tables if needed)
2. **11 Managers** initialize in dependency order (Game → Team → Territory → Season → Quest → Health → Combat → Difficulty → BossBar → WorldEvent)
3. **Data Load**: Teams, Territories, and GameState loaded from database
4. **13 Event Listeners** register for Minecraft events
5. **9 Commands** register (team, quest, territory, revive, shop, stats, leaderboard, soc, compass)
6. **12 Scheduled Tasks** start (capture tick, season check, daily reset, scoreboard update, freezing damage, weather control, seasonal particles, boss bar update, compass update, apocalypse effects, world event check, world event update)

### Core Architecture Pattern: Manager-Driven Design

**Manager Layer** (11 classes in `/managers/`) owns all game logic and state:
- **GameManager**: Overall game state, player data lifecycle, win conditions, scoreboard
- **TeamManager**: Team assignment, elimination detection, team point management
- **TerritoryManager**: Territory ownership, capture mechanics, bonus calculations
- **SeasonManager**: Season transitions (30 real-world days each), seasonal effect modifiers
- **DifficultyManager**: Cycle progression, mob scaling, resource multipliers, world border
- **QuestManager**: Daily/weekly quest generation, completion tracking, point rewards
- **HealthManager**: 40-heart permadeath system, revival logic (max 2/cycle)
- **CombatManager**: PvP kill rewards, bounty system, 12-hour kill cooldown tracking
- **BossBarManager**: Boss bar creation, updates, and cleanup for team/territory info
- **WorldEventManager**: Random world events (meteor showers, supply drops, mob sieges)
- **DataManager**: SQLite persistence, all CRUD operations

**Event Listener Layer** (13 classes in `/listeners/`) handles Minecraft events:
- Listeners are thin adapters: validate event → call manager method → handle result
- Core listeners: PlayerJoin, PlayerDeath, PlayerQuit, PlayerRespawn, PlayerMove, PlayerInteract, BlockBreak, EntityDamage, EntityDeath
- Seasonal effect listeners: CropGrowth, MobSpawn, Fishing
- Feature listeners: CompassTracking
- Example: `PlayerMoveListener` detects territory entry → calls `TerritoryManager.handlePlayerMove()` → applies bonuses

**Command Layer** (9 classes in `/commands/`) exposes functionality to players:
- Commands delegate to managers, perform permission checks, format responses
- Player commands: team, quest, territory, revive, shop, stats, leaderboard, compass
- Admin command: soc (requires `soc.admin` permission)

**Task Layer** (12 classes in `/tasks/`) runs scheduled background operations:
- **CaptureTickTask** (1s): Updates territory capture progress, checks for captures
- **SeasonCheckTask** (1h): Detects season transitions based on real-world time
- **DailyResetTask** (10m): Detects midnight for daily quest resets
- **ScoreboardUpdateTask** (2s): Refreshes player scoreboards with live data
- **FreezingDamageTask** (30s): Applies winter freezing damage to players outdoors
- **WeatherControlTask** (5m): Controls weather based on current season
- **SeasonalParticlesTask** (3s): Spawns seasonal particle effects
- **BossBarUpdateTask** (2s): Updates boss bar information for all players
- **CompassUpdateTask** (5s): Updates tracking compass targets
- **ApocalypseEffectsTask** (30s): Applies apocalypse world effects (water→lava, fire, ash)
- **WorldEventCheckTask** (2h): Checks if random world events should trigger
- **WorldEventUpdateTask** (5s): Updates active world events

### Critical Data Flows

**Territory Capture Flow:**
1. Player enters territory → `PlayerMoveListener` updates current territory tracking
2. `CaptureTickTask` runs every second → counts alive players from each team within capture radius (10 blocks)
3. If 3+ team members in range and no enemies within defense radius (50 blocks) → progress increments
4. Progress updates sent to nearby players every 30 seconds
5. After 300 seconds → capture completes → `TerritoryManager.captureTerritory()`
6. Updates database, awards points, steals points from previous owner, announces to server
7. If requirements not met → progress decays at 2x speed

**Player Death Flow:**
1. Player dies → `PlayerDeathListener` → `HealthManager.handleDeath()`
2. Health decreases by 20 HP (10 hearts)
3. If health ≤ 0 → permanent death → `GameManager.markPlayerDead()`
4. Check team elimination → if last member → `TeamManager.eliminateTeam()`
5. Check win condition → if 1 team left → `GameManager.endGame()`

**Quest Completion Flow:**
1. Player performs action (mine ore, kill mob, etc.) → event listener
2. Listener calls `QuestManager.checkQuestProgress()` with action type
3. Quest progress increments → if complete → awards points to team
4. Points stored in `TeamData` → used for territory upgrades or revivals

### Database Schema (SQLite)

**5 Tables** managed by DataManager:

```sql
-- Core player data
players: uuid, name, teamId, health, alive, kills, deaths, questPoints, revivals

-- Team state
teams: teamId, name, questPoints, eliminated, territoriesOwned

-- Territory ownership and capture
territories: territoryId, name, owner, captureProgress, capturingTeam

-- Global game state (single row)
game_state: currentSeason, currentCycle, apocalypseMode, dayInSeason

-- PvP cooldown tracking
kill_cooldowns: killerUuid, victimUuid, timestamp
```

All manager methods that modify state call `dataManager.save*()` immediately for persistence.

### Configuration System (config.yml)

Configuration loaded at startup, accessed via `getConfig()` throughout codebase:

**Territory Configuration** (5 territories):
- Each has: name, bounds (rectangular region), beacon coords, bonus_type, base_bonus
- Bonus types: ORE, WOOD, CROP, XP, FISH (enum in `models/BonusType.java`)
- Territory 3 (Badlands) starts neutral (`starting_owner: 0`)

**Difficulty Cycles** (7 cycles before apocalypse):
- Each cycle defines: mob_damage, mob_health, resources (multipliers), border (blocks), revival_cost (points)
- Accessed via `DifficultyManager.getCurrentCycleConfig()`

**Seasonal Modifiers** (hardcoded in SeasonManager):
- Spring: +50% regen, +50% crops, fewer mobs
- Summer: +25% mob damage, +50% ore drops
- Fall: +100% harvest, +25% all drops
- Winter: Freezing damage, -50% food, crops disabled

## Key Implementation Patterns

### Manager Access Pattern
All managers are accessible via singleton: `SeasonsOfConflict.getInstance().get*Manager()`

```java
// Common pattern in listeners and commands
TeamManager teamManager = plugin.getTeamManager();
PlayerData playerData = plugin.getGameManager().getPlayerData(player);
```

### Player Data Caching
`GameManager` maintains in-memory cache of `PlayerData` objects:
- Loaded on player join → `PlayerJoinListener`
- Saved on player quit → `PlayerQuitListener`
- Auto-saved periodically via `DataManager.saveAll()`

### Territory Bonus Calculation
Bonuses only apply when ALL conditions met:
1. Player's team owns the territory
2. Player is physically inside territory bounds (rectangular check)
3. Player is performing matching activity (mining for ORE, breaking logs for WOOD, etc.)
4. Seasonal modifier applied (e.g., CROP disabled in Winter)

Implementation in `TerritoryManager.applyTerritoryBonus()`.

### Quest System Architecture
- **QuestTemplate** defines quest type (MINE_STONE, KILL_ZOMBIES, etc.)
- **PlayerQuest** tracks individual player progress toward a template
- **QuestManager** generates 3 random daily quests per player at midnight
- Weekly team quests worth more points, require collective effort

## Testing and Deployment

### Automated Tests

**Test Framework:** JUnit 5 + MockBukkit (for Spigot mocking)

**Test Coverage:** 3 integration test suites covering critical flows:
- **CaptureTickTaskTest** (8 tests): Territory capture mechanics, progress tracking, contestation
- **HealthManagerTest** (9 tests): Permadeath, revival limits, team elimination
- **QuestManagerTest** (9 tests): Quest generation, progress tracking, completion rewards

**Current Status:** Test structure in place with placeholder implementations. Tests require MockBukkit setup to be fully functional.

**Running Tests:**
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CaptureTickTaskTest

# Skip tests during build
mvn clean package -DskipTests
```

See `src/test/java/com/seasonsofconflict/tests/README.md` for implementation guide.

### Manual Testing

**Test Server Setup:**
1. Minecraft 1.20.1 with Spigot or Paper
2. 5000x5000 world with all biomes represented
3. Configure beacon coordinates in config.yml to match world
4. Minimum 15 players (3 per team) for meaningful testing
5. Use `/soc setcycle` and `/soc setseason` to test different game phases

### Deployment

```bash
mvn clean package
cp target/SeasonsOfConflict-1.0.1.jar /path/to/server/plugins/
# Restart server
```

First run creates `plugins/SeasonsOfConflict/config.yml` and initializes database.

## Common Development Patterns

### Adding a New Manager
1. Create class in `/managers/` extending manager pattern
2. Add field to `SeasonsOfConflict.java:16-24`
3. Initialize in `initializeManagers()` method (mind dependency order)
4. Add getter in `SeasonsOfConflict.java:122-161`

### Adding a New Quest Type
1. Add enum to `QuestCategory.java`
2. Add template to `QuestManager.initializeQuestTemplates()`
3. Implement progress tracking in relevant event listener
4. Call `questManager.checkQuestProgress()` with quest category and amount

### Modifying Seasonal Effects
Seasonal effects are hardcoded in `SeasonManager.applySeasonalEffect()`. To add new effect:
1. Define multiplier or behavior per season (Spring/Summer/Fall/Winter)
2. Call from appropriate event listener (e.g., `BlockBreakListener` for harvest bonuses)

### Database Schema Changes
1. Modify table creation SQL in `DataManager.initialize()`
2. Update corresponding load/save methods in `DataManager`
3. Update data model classes in `/models/` (PlayerData, TeamData, etc.)
4. **Warning:** No migration system exists - schema changes require database reset

## Critical Constraints

- **No Nether/End**: Portal creation blocked in `PlayerInteractListener`
- **No Enchanting**: Enchanting table placement blocked
- **Diamond Rarity**: Only 10% of diamond ore drops diamonds (config: `diamond.drop_chance`)
- **Iron Tier Max**: Game balanced around iron equipment being endgame
- **Permadeath**: Health never regenerates naturally beyond initial 40 hearts
- **Revival Limit**: Max 2 revivals per player per cycle (resets when cycle advances)
- **12h Kill Cooldown**: Same player can't be killed repeatedly for points

## Admin Commands Reference

All require `soc.admin` permission:

```bash
/soc setseason <SPRING|SUMMER|FALL|WINTER>  # Force season change
/soc setcycle <1-7>                         # Set difficulty cycle
/soc apocalypse                             # Force apocalypse mode (cycle 8+)
/soc setpoints <team> <amount>              # Set team quest points
```

## Configuration Files

- **plugin.yml**: Command registration, permissions, plugin metadata
- **config.yml**: All game tuning (territories, cycles, teams, messages)
- **Database**: `plugins/SeasonsOfConflict/seasons.db` (SQLite)

## Troubleshooting

**"Plugin won't load":**
- Check Java version: `java -version` (must be 17+)
- Check Spigot version: Must be 1.20.1
- Check server logs for SQLite errors

**"Territory bonuses not working":**
- Verify player is inside territory bounds (rectangular, not circular)
- Verify team owns territory via `/territory info`
- Check seasonal modifiers (e.g., CROP disabled in Winter)

**"Quests not generating":**
- Daily reset task runs every 10 minutes - wait for next check
- Verify game state not corrupted: check `game_state` table
- Use `/soc setseason` to force reset if needed
