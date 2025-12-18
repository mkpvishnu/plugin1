# ⚔️ Seasons of Conflict

**Version:** 1.0.0
**Minecraft:** 1.20.1 (Spigot/Paper)

> A hardcore survival experience where 5 teams battle through changing seasons, escalating difficulty, and territorial warfare. Only one team will survive.

---

## 🎮 What is Seasons of Conflict?

Seasons of Conflict is a team-based hardcore survival plugin where strategy matters as much as skill. You'll start with **40 hearts** and **no natural regeneration**—every heart you lose brings you closer to permanent death. Work with your team to control territories, complete quests, and survive through 4 seasons and 7 difficulty cycles before the apocalypse arrives.

### Core Concept
- **5 Teams** compete for dominance
- **5 Territories** provide strategic bonuses
- **4 Seasons** change the world every 30 days
- **7 Difficulty Cycles** make the world progressively deadlier
- **Permadeath** with limited revivals
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
- **40 Hearts (80 HP)** - This is your ONLY health pool
- **60 seconds of spawn protection**
- **No gear** - you must gather everything

### Critical Rules

❌ **Permanent Restrictions:**
- **No Nether or End** - Portal creation is blocked
- **No Enchanting Tables** - Cannot be placed
- **Diamond Rarity** - Only 10% of diamond ore drops diamonds
- **Iron Tier Max** - The game is balanced around iron equipment being endgame

💀 **Permadeath System:**
- Each death costs **10 hearts (20 HP)**
- **No natural regeneration** - your hearts never come back
- At 0 HP, you are **permanently dead** (spectator mode)
- Teammates can revive you using team points (max 2 revivals per cycle)

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
- ✅ **Regeneration I** - Passive healing for all players
- ✅ **+50% Crop Growth** - Farms grow faster
- ✅ **Fewer Mobs** - Reduced hostile spawn rates
- 📍 **Best Territories:** Dark Forest (wood), Savanna Plains (crops)

**Strategy:** Focus on base building, farming, and stockpiling resources while healing is available.

---

### ☀️ Summer (Days 31-60)
**Theme:** Heat and Hostility

**Effects:**
- ⚠️ **Mob Damage +50%** - Mobs hit harder
- ✅ **Ore Drops +50%** - Extra ore from mining
- ⚠️ **Aggressive Mobs** - Increased aggression
- 📍 **Best Territories:** Tundra Peaks (ore mining)

**Strategy:** Mine aggressively for resources, avoid unnecessary combat, fortify defenses.

---

### 🍂 Fall (Days 61-90)
**Theme:** Harvest Before Winter

**Effects:**
- ✅ **Harvest Bonus +100%** - Double crop yields
- ✅ **All Drops +25%** - Increased drops from all sources
- ✅ **Resource Abundance** - Best time to gather
- 📍 **Best Territories:** Savanna Plains (crops), Ocean Coast (fishing)

**Strategy:** Gather as much food and resources as possible before Winter. Complete team quests.

---

### ❄️ Winter (Days 91-120)
**Theme:** Survival and Hardship

**Effects:**
- ⚠️ **Freezing Damage** - Take 1 heart damage every 30 seconds outdoors
- ⚠️ **Food Production -50%** - Reduced food from all sources
- ❌ **Crops Disabled** - CROP territory bonuses don't work
- ✅ **Heat Sources** - Stay within 5 blocks of campfire/furnace/lava to avoid freezing
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

Every player receives **3 random daily quests** at midnight (server time). You can complete up to **5 quests per day** total.

**Quest Types:**
- 🪨 **Mining Quests** - Mine 64 stone, 32 iron ore, etc.
- 🗡️ **Combat Quests** - Kill 10 zombies, 5 skeletons, etc.
- 🌾 **Farming Quests** - Harvest 32 wheat, 16 carrots, etc.
- 🐟 **Fishing Quests** - Catch 20 fish
- ⚔️ **PvP Quests** - Kill 1 enemy player
- 🏗️ **Building Quests** - Place 64 blocks, craft items, etc.

**Rewards:**
- Each quest awards **50-200 team points**
- Points are shared across your entire team
- Completing quests early helps save teammates later

**Commands:**
- `/quest` or `/q` - View your active quests and progress
- Quest progress is tracked automatically

---

## 💚 Death & Revival System

### How Death Works

1. **First Death:** Lose 10 hearts (40 HP → 30 HP)
2. **Second Death:** Lose 10 more hearts (30 HP → 20 HP)
3. **Third Death:** Lose 10 more hearts (20 HP → 10 HP)
4. **Fourth Death:** **PERMANENT DEATH** (10 HP → 0 HP)

When permanently dead:
- You enter **Spectator Mode**
- You can watch the game continue
- Teammates can spend points to revive you

### Revival System

**How to Revive:**
- Use `/revive <playername>` while alive
- Costs **500-10,000 team points** (increases each cycle)
- **Max 2 revivals per player per cycle**
- Revival counter resets when difficulty cycle advances

**Revival Process:**
- Revived player spawns at team beacon
- **Full health restored** (40 hearts)
- **No gear** - must be re-equipped
- **60 seconds spawn protection**

**Important:**
- You cannot revive yourself (must be done by living teammate)
- If all teammates are dead, your team is **ELIMINATED**
- Points are shared, so spend wisely

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

**Future Items (Coming Soon):**
- Territory Shield (24hr protection) - 200 pts
- Team Buff: 2x Drops (1hr) - 100 pts

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

1. **Hearts Don't Regenerate** - Every hit matters
2. **Golden Apples** - Your only source of healing in combat
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
- All team members are permanently dead (0 HP)
- No team points remaining to revive anyone
- Server announces: "Team [Name] has been ELIMINATED!"

Eliminated teams:
- Can no longer respawn
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
   - Build iron gear before combat
   - Stockpile food (you'll need it in Winter)
   - Complete daily quests to build team points

2. **Secure Your Home Territory**
   - Fortify your team's starting beacon
   - Build farms and sustainable food sources
   - Create storage for shared resources

3. **Avoid Unnecessary Deaths**
   - Every death costs 10 hearts permanently
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
   - Revivals cost 1500-5000 points
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

## ⚙️ Installation & Setup

### Requirements
- **Java 17+**
- **Spigot or Paper 1.20.1**
- **5000x5000 world** with varied biomes

### Installation

1. Download `SeasonsOfConflict-1.0.0.jar`
2. Place in your server's `plugins/` folder
3. Restart server
4. Configure beacon locations in `plugins/SeasonsOfConflict/config.yml`
5. Adjust team names, colors, and difficulty settings as needed

### Building from Source

```bash
git clone https://github.com/mkpvishnu/plugin1.git
cd plugin1
mvn clean package
cp target/SeasonsOfConflict-1.0.0.jar /path/to/server/plugins/
```

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
- [ ] Avoid dying—hearts don't regenerate!
- [ ] Coordinate with your team in chat
- [ ] Prepare for seasonal changes
- [ ] Survive and outlast the other teams!

---

**Good luck, survivor. May your team be the last one standing.**

*Seasons of Conflict v1.0.0 - Where survival meets strategy*
