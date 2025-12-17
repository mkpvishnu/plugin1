# Seasons of Conflict

**Version:** 1.0.0  
**Minecraft:** 1.20.1  
**API:** Spigot/Paper

A hardcore survival Minecraft plugin featuring progressive difficulty, seasonal mechanics, team-based territory control, and permadeath with revival system.

## Features

### Core Mechanics
- ⚔️ **5 Teams, 5 Territories** - Team-based survival with strategic territory control
- 💚 **40 Hearts Permadeath** - Hardcore mode with team-based revival system (max 2/cycle)
- 🗺️ **Territory Bonuses** - Seasonal modifiers affect territory effectiveness
- 📈 **Progressive Difficulty** - 7 cycles of increasing challenge, then apocalypse mode
- 🌱 **4 Seasons** - Spring, Summer, Fall, Winter with unique effects
- 📜 **Quest System** - Daily solo and weekly team quests for points
- 🏆 **Last Team Standing** - Victory condition based on survival, not conquest

### Game Systems
- **Territory Control**: Beacon-based capture system with 5-minute capture time
- **Seasonal Effects**: Dynamic bonuses that shift optimal strategies
- **PvP Rewards**: Kill rewards, bounty system, 12-hour kill cooldown
- **Revival System**: Spend team quest points to revive fallen teammates
- **World Border**: Shrinks progressively, forces final confrontation

### Restrictions
- ❌ No Nether/End portals
- ❌ No enchanting tables
- 💎 Diamond ore ultra-rare (10% drop rate)
- ⚡ Iron is the practical max tier

## Installation

### Requirements
- Java 17 or higher
- Spigot/Paper 1.20.1 server
- Maven (for building)

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/mkpvishnu/plugin1.git
cd plugin1
```

2. Build with Maven:
```bash
mvn clean package
```

3. The compiled JAR will be in `target/SeasonsOfConflict-1.0.0.jar`

4. Copy to your server's `plugins/` folder:
```bash
cp target/SeasonsOfConflict-1.0.0.jar /path/to/server/plugins/
```

5. Restart your server

## Configuration

On first run, the plugin will create `plugins/SeasonsOfConflict/config.yml`.

### Key Settings

#### Territories
Define 5 territories with beacon locations:
```yaml
territories:
  1:
    name: "Tundra Peaks"
    bounds: {minX: -2500, maxX: 2500, minZ: -2500, maxZ: -1000}
    beacon: {x: 0, y: 100, z: -1750}
    bonus_type: "ORE"
    base_bonus: 50
```

#### Difficulty Cycles
Customize 7 cycles of scaling difficulty:
```yaml
difficulty:
  cycles:
    1:
      mob_damage: 1.0
      mob_health: 1.0
      resources: 1.0
      border: 5000
      revival_cost: 500
```

#### Teams
Configure 5 teams with colors:
```yaml
teams:
  1:
    name: "North"
    color: "AQUA"
```

## Commands

### Player Commands
- `/team [info|list|members]` - View team information
- `/quest [list|progress]` - View active quests
- `/territory [info|map]` - View territory information
- `/revive <player>` - Revive a dead teammate (costs points)
- `/shop` - View purchasable items with quest points
- `/stats [player]` - View player statistics
- `/leaderboard [kills|points|teams]` - View rankings

### Admin Commands (Permission: `soc.admin`)
- `/soc setseason <season>` - Change current season
- `/soc setcycle <number>` - Set difficulty cycle
- `/soc apocalypse` - Force apocalypse mode
- `/soc setpoints <team> <amount>` - Set team points

## Gameplay Guide

### Starting Out
1. **Join the Server** - Auto-assigned to a team, spawn at team beacon
2. **Gather Resources** - Start with 40 hearts, iron-tier max
3. **Complete Quests** - Earn quest points for your team (3 daily quests)
4. **Control Territory** - Capture beacons for bonuses

### Territory Bonuses
Bonuses only apply when:
- Your team owns the territory
- You are physically inside it
- Activity matches the bonus type

**Territories:**
- **Tundra** (+50% ore) - Best in Summer, worst in Winter
- **Forest** (+50% wood) - Best in Spring, worst in Winter
- **Plains** (+100% crops) - Best in Fall, disabled in Winter
- **Badlands** (+100% XP) - Consistent across seasons
- **Coast** (+50% fish) - Best in Fall, worst in Summer/Winter

### Seasons (30 days each)
- **Spring**: Regeneration +50%, crop growth +50%, fewer mobs
- **Summer**: Mob damage +25%, ore drops +50%
- **Fall**: Harvest +100%, all drops +25%, special events
- **Winter**: Freezing damage outdoors, food -50%, crops disabled

### Progressive Difficulty
**Cycles 1-7:**
- Mob damage: 1.0x → 3.0x
- Resources: 1.0x → 0.1x
- Border: 5000 → 2000 blocks
- Revival cost: 500 → 5000 points

**Cycle 8+ (Apocalypse):**
- Border shrinks 200 blocks/day
- Resource spawns stop
- Forces final confrontation

### Revival System
- Costs quest points (increases each cycle)
- Maximum 2 revivals per player per cycle
- Revived players spawn at team beacon with no gear
- 60 seconds of spawn protection

### Victory Condition
**Last team with at least one living member wins**

If border reaches minimum, team with most quest points wins.

## Architecture

### Plugin Structure
```
47 Java files across 7 packages:
- 9 Managers (Game, Team, Territory, Season, Difficulty, Quest, Health, Combat, Data)
- 9 Event Listeners (comprehensive event handling)
- 8 Commands (player + admin commands)
- 5 Scheduled Tasks (capture tick, season check, daily reset, etc.)
- 7 Data Models (player, team, territory, quest, etc.)
- 3 Utilities (messages, locations, math)
```

### Database
SQLite database with 5 tables:
- `players` - Player stats, team assignment, alive status
- `teams` - Team data, quest points, elimination status
- `territories` - Ownership, capture progress
- `game_state` - Current season, cycle, apocalypse mode
- `kill_cooldowns` - PvP cooldown tracking

## Development

### Project Structure
```
src/main/
├── java/com/seasonsofconflict/
│   ├── SeasonsOfConflict.java (Main class)
│   ├── managers/ (9 manager classes)
│   ├── listeners/ (9 event listeners)
│   ├── commands/ (8 command classes)
│   ├── tasks/ (5 scheduled tasks)
│   ├── models/ (7 data models)
│   ├── utils/ (3 utility classes)
│   └── data/ (DataManager)
└── resources/
    ├── plugin.yml
    └── config.yml
```

### Building
```bash
mvn clean package
```

### Testing
Place JAR in a test server with:
- Minecraft 1.20.1
- Spigot or Paper
- 5000x5000 world with all biomes

## License

MIT License - See LICENSE file for details

## Credits

- **Developer**: mkpvishnu
- **Co-Developer**: Claude (Anthropic)
- **Framework**: Spigot API 1.20.1

## Support

Report issues: https://github.com/mkpvishnu/plugin1/issues

---

**Seasons of Conflict v1.0.0** - Where survival meets strategy
