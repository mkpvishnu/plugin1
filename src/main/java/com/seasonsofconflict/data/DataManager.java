package com.seasonsofconflict.data;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.*;
import org.bukkit.ChatColor;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DataManager {

    private final SeasonsOfConflict plugin;
    private Connection connection;

    public DataManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/data.db";
            connection = DriverManager.getConnection(url);
            createTables();
            plugin.getLogger().info("Database initialized successfully");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String[] tables = {
            // Players table
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                team_id INTEGER NOT NULL,
                is_alive INTEGER DEFAULT 1,
                revivals_used INTEGER DEFAULT 0,
                total_kills INTEGER DEFAULT 0,
                total_deaths INTEGER DEFAULT 0,
                kill_streak INTEGER DEFAULT 0,
                bounty INTEGER DEFAULT 0,
                daily_quests_completed INTEGER DEFAULT 0,
                daily_mob_kills INTEGER DEFAULT 0,
                daily_ores_mined INTEGER DEFAULT 0,
                first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,

            // Teams table
            """
            CREATE TABLE IF NOT EXISTS teams (
                team_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                color TEXT NOT NULL,
                quest_points INTEGER DEFAULT 0,
                home_territory INTEGER NOT NULL,
                is_eliminated INTEGER DEFAULT 0,
                weekly_quest_id INTEGER DEFAULT 0,
                weekly_quest_progress INTEGER DEFAULT 0,
                last_shield_time INTEGER DEFAULT 0,
                active_shield_expiry INTEGER DEFAULT 0
            )
            """,

            // Territories table
            """
            CREATE TABLE IF NOT EXISTS territories (
                territory_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                owner_team_id INTEGER DEFAULT 0,
                capturing_team_id INTEGER DEFAULT 0,
                capture_progress INTEGER DEFAULT 0
            )
            """,

            // Game state table
            """
            CREATE TABLE IF NOT EXISTS game_state (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                current_season TEXT NOT NULL,
                current_cycle INTEGER DEFAULT 1,
                season_start_date TEXT NOT NULL,
                is_apocalypse INTEGER DEFAULT 0
            )
            """,

            // Kill cooldowns table
            """
            CREATE TABLE IF NOT EXISTS kill_cooldowns (
                killer_uuid TEXT NOT NULL,
                victim_uuid TEXT NOT NULL,
                cooldown_expiry INTEGER NOT NULL,
                PRIMARY KEY (killer_uuid, victim_uuid)
            )
            """
        };

        for (String table : tables) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(table);
            }
        }
    }

    // Player CRUD operations
    public void savePlayer(PlayerData player) {
        String sql = """
            INSERT OR REPLACE INTO players
            (uuid, name, team_id, is_alive, revivals_used, total_kills, total_deaths,
             kill_streak, bounty, daily_quests_completed, daily_mob_kills, daily_ores_mined)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUUID().toString());
            pstmt.setString(2, player.getName());
            pstmt.setInt(3, player.getTeamId());
            pstmt.setInt(4, player.isAlive() ? 1 : 0);
            pstmt.setInt(5, player.getRevivalsUsed());
            pstmt.setInt(6, player.getTotalKills());
            pstmt.setInt(7, player.getTotalDeaths());
            pstmt.setInt(8, player.getKillStreak());
            pstmt.setInt(9, player.getBounty());
            pstmt.setInt(10, player.getDailyQuestsCompleted());
            pstmt.setInt(11, player.getDailyMobKills());
            pstmt.setInt(12, player.getDailyOresMined());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save player " + player.getName() + ": " + e.getMessage());
        }
    }

    public PlayerData loadPlayer(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                PlayerData player = new PlayerData(uuid, name);
                player.setTeamId(rs.getInt("team_id"));
                player.setAlive(rs.getInt("is_alive") == 1);
                player.setRevivalsUsed(rs.getInt("revivals_used"));
                player.setTotalKills(rs.getInt("total_kills"));
                player.setTotalDeaths(rs.getInt("total_deaths"));
                player.setKillStreak(rs.getInt("kill_streak"));
                player.setBounty(rs.getInt("bounty"));
                player.setDailyQuestsCompleted(rs.getInt("daily_quests_completed"));
                player.setDailyMobKills(rs.getInt("daily_mob_kills"));
                player.setDailyOresMined(rs.getInt("daily_ores_mined"));
                return player;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load player " + uuid + ": " + e.getMessage());
        }
        return null;
    }

    public List<PlayerData> loadAllPlayers() {
        List<PlayerData> players = new ArrayList<>();
        String sql = "SELECT * FROM players";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("name");
                PlayerData player = new PlayerData(uuid, name);
                player.setTeamId(rs.getInt("team_id"));
                player.setAlive(rs.getInt("is_alive") == 1);
                player.setRevivalsUsed(rs.getInt("revivals_used"));
                player.setTotalKills(rs.getInt("total_kills"));
                player.setTotalDeaths(rs.getInt("total_deaths"));
                player.setKillStreak(rs.getInt("kill_streak"));
                player.setBounty(rs.getInt("bounty"));
                player.setDailyQuestsCompleted(rs.getInt("daily_quests_completed"));
                player.setDailyMobKills(rs.getInt("daily_mob_kills"));
                player.setDailyOresMined(rs.getInt("daily_ores_mined"));
                players.add(player);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load players: " + e.getMessage());
        }
        return players;
    }

    // Team CRUD operations
    public void saveTeam(TeamData team) {
        String sql = """
            INSERT OR REPLACE INTO teams
            (team_id, name, color, quest_points, home_territory, is_eliminated,
             weekly_quest_id, weekly_quest_progress, last_shield_time, active_shield_expiry)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, team.getTeamId());
            pstmt.setString(2, team.getName());
            pstmt.setString(3, team.getColor().name());
            pstmt.setInt(4, team.getQuestPoints());
            pstmt.setInt(5, team.getHomeTerritory());
            pstmt.setInt(6, team.isEliminated() ? 1 : 0);
            pstmt.setInt(7, team.getWeeklyQuestId());
            pstmt.setInt(8, team.getWeeklyQuestProgress());
            pstmt.setLong(9, team.getLastShieldTime());
            pstmt.setLong(10, team.getActiveShieldExpiry());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save team " + team.getName() + ": " + e.getMessage());
        }
    }

    public TeamData loadTeam(int teamId) {
        String sql = "SELECT * FROM teams WHERE team_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                ChatColor color = ChatColor.valueOf(rs.getString("color"));
                int homeTerritory = rs.getInt("home_territory");
                TeamData team = new TeamData(teamId, name, color, homeTerritory);
                team.setQuestPoints(rs.getInt("quest_points"));
                team.setEliminated(rs.getInt("is_eliminated") == 1);
                team.setWeeklyQuestId(rs.getInt("weekly_quest_id"));
                team.setWeeklyQuestProgress(rs.getInt("weekly_quest_progress"));
                team.setLastShieldTime(rs.getLong("last_shield_time"));
                team.setActiveShieldExpiry(rs.getLong("active_shield_expiry"));
                return team;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load team " + teamId + ": " + e.getMessage());
        }
        return null;
    }

    public List<TeamData> loadAllTeams() {
        List<TeamData> teams = new ArrayList<>();
        String sql = "SELECT * FROM teams ORDER BY team_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int teamId = rs.getInt("team_id");
                String name = rs.getString("name");
                ChatColor color = ChatColor.valueOf(rs.getString("color"));
                int homeTerritory = rs.getInt("home_territory");
                TeamData team = new TeamData(teamId, name, color, homeTerritory);
                team.setQuestPoints(rs.getInt("quest_points"));
                team.setEliminated(rs.getInt("is_eliminated") == 1);
                team.setWeeklyQuestId(rs.getInt("weekly_quest_id"));
                team.setWeeklyQuestProgress(rs.getInt("weekly_quest_progress"));
                team.setLastShieldTime(rs.getLong("last_shield_time"));
                team.setActiveShieldExpiry(rs.getLong("active_shield_expiry"));
                teams.add(team);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load teams: " + e.getMessage());
        }
        return teams;
    }

    // Territory CRUD operations
    public void saveTerritory(TerritoryData territory) {
        String sql = """
            INSERT OR REPLACE INTO territories
            (territory_id, name, owner_team_id, capturing_team_id, capture_progress)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, territory.getTerritoryId());
            pstmt.setString(2, territory.getName());
            pstmt.setInt(3, territory.getOwnerTeamId());
            pstmt.setInt(4, territory.getCapturingTeamId());
            pstmt.setInt(5, territory.getCaptureProgress());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save territory " + territory.getName() + ": " + e.getMessage());
        }
    }

    public TerritoryData loadTerritory(int territoryId) {
        String sql = "SELECT * FROM territories WHERE territory_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, territoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Load from config
                return loadTerritoryFromConfig(territoryId, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load territory " + territoryId + ": " + e.getMessage());
        }
        return null;
    }

    private TerritoryData loadTerritoryFromConfig(int territoryId, ResultSet rs) throws SQLException {
        String basePath = "territories." + territoryId + ".";
        String name = plugin.getConfig().getString(basePath + "name");
        int minX = plugin.getConfig().getInt(basePath + "bounds.minX");
        int maxX = plugin.getConfig().getInt(basePath + "bounds.maxX");
        int minZ = plugin.getConfig().getInt(basePath + "bounds.minZ");
        int maxZ = plugin.getConfig().getInt(basePath + "bounds.maxZ");
        int beaconX = plugin.getConfig().getInt(basePath + "beacon.x");
        int beaconY = plugin.getConfig().getInt(basePath + "beacon.y");
        int beaconZ = plugin.getConfig().getInt(basePath + "beacon.z");
        String bonusTypeStr = plugin.getConfig().getString(basePath + "bonus_type");
        BonusType bonusType = BonusType.valueOf(bonusTypeStr);
        int baseBonusPercent = plugin.getConfig().getInt(basePath + "base_bonus");

        TerritoryData territory = new TerritoryData(territoryId, name, minX, maxX, minZ, maxZ,
                                                      beaconX, beaconY, beaconZ, bonusType, baseBonusPercent);

        if (rs != null) {
            territory.setOwnerTeamId(rs.getInt("owner_team_id"));
            territory.setCapturingTeamId(rs.getInt("capturing_team_id"));
            territory.setCaptureProgress(rs.getInt("capture_progress"));
        }

        return territory;
    }

    public List<TerritoryData> loadAllTerritories() {
        List<TerritoryData> territories = new ArrayList<>();
        String sql = "SELECT * FROM territories ORDER BY territory_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int territoryId = rs.getInt("territory_id");
                TerritoryData territory = loadTerritoryFromConfig(territoryId, rs);
                if (territory != null) {
                    territories.add(territory);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load territories: " + e.getMessage());
        }
        return territories;
    }

    // Game state operations
    public void saveGameState(GameState gameState) {
        String sql = """
            INSERT OR REPLACE INTO game_state
            (id, current_season, current_cycle, season_start_date, is_apocalypse)
            VALUES (1, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, gameState.getCurrentSeason().name());
            pstmt.setInt(2, gameState.getCurrentCycle());
            pstmt.setString(3, gameState.getSeasonStartDate().toString());
            pstmt.setInt(4, gameState.isApocalypse() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save game state: " + e.getMessage());
        }
    }

    public GameState loadGameState() {
        String sql = "SELECT * FROM game_state WHERE id = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                GameState gameState = new GameState();
                gameState.setCurrentSeason(Season.valueOf(rs.getString("current_season")));
                gameState.setCurrentCycle(rs.getInt("current_cycle"));
                gameState.setSeasonStartDate(LocalDate.parse(rs.getString("season_start_date")));
                gameState.setApocalypse(rs.getInt("is_apocalypse") == 1);
                return gameState;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load game state: " + e.getMessage());
        }

        // Return default if not found
        return new GameState();
    }

    // Utility methods
    public void saveAll() {
        // Save is handled by individual manager classes
        plugin.getLogger().info("Saving all data...");
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database connection: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
