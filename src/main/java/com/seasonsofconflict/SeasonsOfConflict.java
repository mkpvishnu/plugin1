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
    private BossBarManager bossBarManager;
    private WorldEventManager worldEventManager;
    private XPManager xpManager;
    private SkillManager skillManager;
    private SkillTreeGUI skillTreeGUI;
    private SkillEffectManager skillEffectManager;
    private CooldownManager cooldownManager;

    // Listeners
    private CompassTrackingListener compassTrackingListener;
    private GatheringSkillListener gatheringSkillListener;
    private SurvivalSkillListener survivalSkillListener;
    private ActiveSkillListener activeSkillListener;

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
        // Clean up boss bars
        if (bossBarManager != null) {
            bossBarManager.cleanup();
        }

        // Save skill and XP data
        if (xpManager != null) {
            xpManager.saveAll();
        }
        if (skillManager != null) {
            skillManager.saveAll();
        }

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
        bossBarManager = new BossBarManager(this);
        worldEventManager = new WorldEventManager(this);
        xpManager = new XPManager(this);
        skillManager = new SkillManager(this);
        skillTreeGUI = new SkillTreeGUI(this);
        skillEffectManager = new SkillEffectManager(this);
        cooldownManager = new CooldownManager(this);

        // Initialize listeners that need to be accessed
        compassTrackingListener = new CompassTrackingListener(this);

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
        // Compass tracking listener
        getServer().getPluginManager().registerEvents(compassTrackingListener, this);
        // XP gain listener
        getServer().getPluginManager().registerEvents(new XPGainListener(this), this);
        // Skill GUI listener
        getServer().getPluginManager().registerEvents(new SkillGUIListener(this), this);
        // Combat skill effects listener
        getServer().getPluginManager().registerEvents(new CombatSkillListener(this), this);
        // Gathering skill effects listener
        gatheringSkillListener = new GatheringSkillListener(this);
        getServer().getPluginManager().registerEvents(gatheringSkillListener, this);
        // Survival skill effects listener
        survivalSkillListener = new SurvivalSkillListener(this);
        getServer().getPluginManager().registerEvents(survivalSkillListener, this);
        // Teamwork skill effects listener
        getServer().getPluginManager().registerEvents(new TeamworkSkillListener(this), this);
        // Active skill listener
        activeSkillListener = new ActiveSkillListener(this);
        getServer().getPluginManager().registerEvents(activeSkillListener, this);
    }

    private void registerCommands() {
        // Register command executors
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("quest").setExecutor(new QuestCommand(this));
        getCommand("territory").setExecutor(new TerritoryCommand(this));
        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
        getCommand("soc").setExecutor(new AdminCommand(this));
        getCommand("compass").setExecutor(new CompassCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(this));

        // Register tab completers
        getCommand("team").setTabCompleter(new com.seasonsofconflict.tabcompleters.TeamCommandTabCompleter());
        getCommand("quest").setTabCompleter(new com.seasonsofconflict.tabcompleters.GeneralTabCompleter("quest"));
        getCommand("territory").setTabCompleter(new com.seasonsofconflict.tabcompleters.GeneralTabCompleter("territory"));
        getCommand("revive").setTabCompleter(new com.seasonsofconflict.tabcompleters.GeneralTabCompleter("revive"));
        getCommand("shop").setTabCompleter(new com.seasonsofconflict.tabcompleters.GeneralTabCompleter("shop"));
        getCommand("stats").setTabCompleter(new com.seasonsofconflict.tabcompleters.GeneralTabCompleter("stats"));
        getCommand("leaderboard").setTabCompleter(new com.seasonsofconflict.tabcompleters.GeneralTabCompleter("leaderboard"));
        getCommand("soc").setTabCompleter(new com.seasonsofconflict.tabcompleters.AdminCommandTabCompleter(this));
        getCommand("skills").setTabCompleter(new com.seasonsofconflict.tabcompleters.SkillsCommandTabCompleter());
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

        // Boss bar updates - runs every 2 seconds
        new BossBarUpdateTask(this).runTaskTimer(this, 0L, 40L);

        // Compass tracking updates - runs every 5 seconds
        new CompassUpdateTask(this).runTaskTimer(this, 0L, 100L);

        // Apocalypse world effects - runs every 30 seconds
        new ApocalypseEffectsTask(this).runTaskTimer(this, 0L, 20L * 30);

        // World event check - runs every 2 hours (configurable)
        long checkInterval = getConfig().getLong("world_events.check_interval_hours", 2) * 60 * 60 * 20L;
        new WorldEventCheckTask(this).runTaskTimer(this, 0L, checkInterval);

        // World event updates - runs every 5 seconds
        new WorldEventUpdateTask(this).runTaskTimer(this, 0L, 100L);

        // Passive skill effects - runs every 5 seconds
        new PassiveEffectsTask(this, gatheringSkillListener, survivalSkillListener)
            .runTaskTimer(this, 0L, 100L);
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

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public CompassTrackingListener getCompassTrackingListener() {
        return compassTrackingListener;
    }

    public WorldEventManager getWorldEventManager() {
        return worldEventManager;
    }

    public XPManager getXPManager() {
        return xpManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillTreeGUI getSkillTreeGUI() {
        return skillTreeGUI;
    }

    public SkillEffectManager getSkillEffectManager() {
        return skillEffectManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ActiveSkillListener getActiveSkillListener() {
        return activeSkillListener;
    }
}
