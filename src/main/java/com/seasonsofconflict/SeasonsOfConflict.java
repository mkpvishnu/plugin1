package com.seasonsofconflict;

import com.seasonsofconflict.commands.*;
import com.seasonsofconflict.data.DataManager;
import com.seasonsofconflict.listeners.*;
import com.seasonsofconflict.managers.*;
import com.seasonsofconflict.tasks.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SeasonsOfConflict extends JavaPlugin {

    private static SeasonsOfConflict instance;

    // Managers
    private DataManager dataManager;
    private GameManager gameManager;
    private TeamManager teamManager;
    private TerritoryManager territoryManager;
    private SeasonManager seasonManager;
    private QuestManager questManager;
    private HealthManager healthManager;
    private CombatManager combatManager;
    private DifficultyManager difficultyManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize data manager
        dataManager = new DataManager(this);
        dataManager.initialize();

        // Initialize managers
        initializeManagers();

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Start scheduled tasks
        startTasks();

        getLogger().info("Seasons of Conflict has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (dataManager != null) {
            dataManager.saveAll();
            dataManager.close();
        }

        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("Seasons of Conflict has been disabled!");
    }

    private void initializeManagers() {
        gameManager = new GameManager(this);
        teamManager = new TeamManager(this);
        territoryManager = new TerritoryManager(this);
        seasonManager = new SeasonManager(this);
        questManager = new QuestManager(this);
        healthManager = new HealthManager(this);
        combatManager = new CombatManager(this);
        difficultyManager = new DifficultyManager(this);

        // Load data
        teamManager.loadTeams();
        territoryManager.loadTerritories();
        gameManager.loadGameState();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        // Seasonal effect listeners
        getServer().getPluginManager().registerEvents(new CropGrowthListener(this), this);
        getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
    }

    private void registerCommands() {
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("quest").setExecutor(new QuestCommand(this));
        getCommand("territory").setExecutor(new TerritoryCommand(this));
        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
        getCommand("soc").setExecutor(new AdminCommand(this));
    }

    private void startTasks() {
        // Capture tick task - runs every second
        new CaptureTickTask(this).runTaskTimer(this, 0L, 20L);

        // Season check task - runs every hour
        new SeasonCheckTask(this).runTaskTimer(this, 0L, 20L * 60 * 60);

        // Daily reset task - runs every 10 minutes to check for midnight
        new DailyResetTask(this).runTaskTimer(this, 0L, 20L * 60 * 10);

        // Scoreboard update task - runs every 2 seconds
        new ScoreboardUpdateTask(this).runTaskTimer(this, 0L, 40L);

        // Winter freezing damage task - runs every 30 seconds
        new FreezingDamageTask(this).runTaskTimer(this, 0L, 20L * 30);

        // Weather control task - runs every 5 minutes
        new WeatherControlTask(this).runTaskTimer(this, 0L, 20L * 60 * 5);

        // Seasonal particle effects - runs every 3 seconds
        new SeasonalParticlesTask(this).runTaskTimer(this, 0L, 60L);
    }

    // Getters
    public static SeasonsOfConflict getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public HealthManager getHealthManager() {
        return healthManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }
}
