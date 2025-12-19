package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.Season;
import com.seasonsofconflict.utils.MessageUtils;
import com.seasonsofconflict.utils.TitleUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Manages random world events: Blood Moon, Meteor Shower, Aurora, Fog, Heatwave
 */
public class WorldEventManager {

    private final SeasonsOfConflict plugin;
    private final Random random;

    // Active events
    private WorldEvent activeEvent;
    private long eventEndTime;

    public enum EventType {
        BLOOD_MOON,
        METEOR_SHOWER,
        AURORA,
        FOG,
        HEATWAVE
    }

    public static class WorldEvent {
        public EventType type;
        public long startTime;
        public long duration;

        public WorldEvent(EventType type, long duration) {
            this.type = type;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
    }

    public WorldEventManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.activeEvent = null;
        this.eventEndTime = 0;
    }

    /**
     * Check if a new event should start
     */
    public void checkForNewEvent() {
        if (!plugin.getConfig().getBoolean("world_events.enabled", true)) {
            return;
        }

        // Don't start new event if one is active
        if (activeEvent != null && System.currentTimeMillis() < eventEndTime) {
            return;
        }

        // Try to start a random event
        EventType eventType = selectRandomEvent();
        if (eventType != null) {
            startEvent(eventType);
        }
    }

    /**
     * Select a random event based on config chances
     */
    private EventType selectRandomEvent() {
        Season currentSeason = plugin.getGameManager().getGameState().getCurrentSeason();
        List<EventType> possibleEvents = new ArrayList<>();
        List<Double> chances = new ArrayList<>();

        // Blood Moon
        if (plugin.getConfig().getBoolean("world_events.blood_moon.enabled", true)) {
            possibleEvents.add(EventType.BLOOD_MOON);
            chances.add(plugin.getConfig().getDouble("world_events.blood_moon.chance", 0.15));
        }

        // Meteor Shower
        if (plugin.getConfig().getBoolean("world_events.meteor_shower.enabled", true)) {
            possibleEvents.add(EventType.METEOR_SHOWER);
            chances.add(plugin.getConfig().getDouble("world_events.meteor_shower.chance", 0.10));
        }

        // Aurora
        if (plugin.getConfig().getBoolean("world_events.aurora.enabled", true)) {
            possibleEvents.add(EventType.AURORA);
            chances.add(plugin.getConfig().getDouble("world_events.aurora.chance", 0.20));
        }

        // Fog
        if (plugin.getConfig().getBoolean("world_events.fog.enabled", true)) {
            possibleEvents.add(EventType.FOG);
            chances.add(plugin.getConfig().getDouble("world_events.fog.chance", 0.15));
        }

        // Heatwave (summer only)
        if (plugin.getConfig().getBoolean("world_events.heatwave.enabled", true) &&
            currentSeason == Season.SUMMER) {
            possibleEvents.add(EventType.HEATWAVE);
            chances.add(plugin.getConfig().getDouble("world_events.heatwave.chance", 0.12));
        }

        // Roll for each event
        for (int i = 0; i < possibleEvents.size(); i++) {
            if (random.nextDouble() < chances.get(i)) {
                return possibleEvents.get(i);
            }
        }

        return null;
    }

    /**
     * Start a world event
     */
    public void startEvent(EventType type) {
        long durationMinutes = switch (type) {
            case BLOOD_MOON -> plugin.getConfig().getLong("world_events.blood_moon.duration_minutes", 30);
            case METEOR_SHOWER -> plugin.getConfig().getLong("world_events.meteor_shower.duration_minutes", 15);
            case AURORA -> plugin.getConfig().getLong("world_events.aurora.duration_minutes", 60);
            case FOG -> plugin.getConfig().getLong("world_events.fog.duration_minutes", 45);
            case HEATWAVE -> plugin.getConfig().getLong("world_events.heatwave.duration_minutes", 40);
        };

        long duration = durationMinutes * 60 * 1000; // Convert to milliseconds
        activeEvent = new WorldEvent(type, duration);
        eventEndTime = System.currentTimeMillis() + duration;

        // Announce event
        announceEvent(type, true);

        plugin.getLogger().info("World event started: " + type.name() + " for " + durationMinutes + " minutes");
    }

    /**
     * Announce event start/end
     */
    private void announceEvent(EventType type, boolean starting) {
        String title = "";
        String subtitle = "";

        if (starting) {
            switch (type) {
                case BLOOD_MOON -> {
                    title = "&4&lBLOOD MOON";
                    subtitle = "&cThe dead grow restless...";
                }
                case METEOR_SHOWER -> {
                    title = "&6&lMETEOR SHOWER";
                    subtitle = "&eThe sky is falling!";
                }
                case AURORA -> {
                    title = "&b&lAURORA";
                    subtitle = "&3Northern lights grant swiftness";
                }
                case FOG -> {
                    title = "&7&lFOG";
                    subtitle = "&8Visibility reduced";
                }
                case HEATWAVE -> {
                    title = "&6&lHEATWAVE";
                    subtitle = "&eSeek shade or suffer";
                }
            }
        } else {
            subtitle = "&7Event ended";
        }

        TitleUtils.broadcastQuickTitle(title, subtitle);
        MessageUtils.broadcast("&e[World Event] " + (starting ? "Started: " : "Ended: ") +
                               type.name().replace("_", " "));
    }

    /**
     * Update active event effects
     */
    public void updateActiveEvent() {
        if (activeEvent == null) return;

        // Check if event ended
        if (System.currentTimeMillis() >= eventEndTime) {
            announceEvent(activeEvent.type, false);
            activeEvent = null;
            eventEndTime = 0;
            return;
        }

        // Apply event effects
        switch (activeEvent.type) {
            case BLOOD_MOON -> applyBloodMoonEffects();
            case METEOR_SHOWER -> applyMeteorShowerEffects();
            case AURORA -> applyAuroraEffects();
            case FOG -> applyFogEffects();
            case HEATWAVE -> applyHeatwaveEffects();
        }
    }

    private void applyBloodMoonEffects() {
        // Blood moon effects are handled in mob spawn/damage listeners
        // Just ensure it's night
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("game.world_name", "world"));
        if (world != null) {
            world.setTime(18000); // Night
        }
    }

    private void applyMeteorShowerEffects() {
        int meteorsPerMinute = plugin.getConfig().getInt("world_events.meteor_shower.meteors_per_minute", 3);

        // Spawn meteors at random locations
        if (random.nextInt(1200) < meteorsPerMinute) { // Every ~20 seconds on average
            spawnMeteor();
        }
    }

    private void spawnMeteor() {
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("game.world_name", "world"));
        if (world == null) return;

        // Random location within world border
        double borderSize = plugin.getGameManager().getGameState().getWorldBorderSize() / 2.0;
        int x = (int) ((random.nextDouble() - 0.5) * borderSize);
        int z = (int) ((random.nextDouble() - 0.5) * borderSize);
        int y = world.getHighestBlockYAt(x, z) + 50;

        Location meteorLoc = new Location(world, x, y, z);

        // Spawn falling block as "meteor"
        world.spawnFallingBlock(meteorLoc, Material.MAGMA_BLOCK.createBlockData());

        // Explosion on impact
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location groundLoc = new Location(world, x, world.getHighestBlockYAt(x, z), z);

            float power = (float) plugin.getConfig().getDouble("world_events.meteor_shower.explosion_power", 2.0);
            world.createExplosion(groundLoc, power, false, true);

            // Spawn ore deposits
            if (plugin.getConfig().getBoolean("world_events.meteor_shower.spawn_ores", true)) {
                spawnOreDeposit(groundLoc);
            }

            // Particles
            world.spawnParticle(Particle.EXPLOSION_LARGE, groundLoc, 3, 1, 1, 1, 0);
            world.spawnParticle(Particle.FLAME, groundLoc, 50, 2, 2, 2, 0.1);
        }, 60L); // 3 seconds fall time
    }

    private void spawnOreDeposit(Location loc) {
        // Spawn random ores in small radius
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (random.nextDouble() < 0.3) {
                        Block block = loc.clone().add(x, y, z).getBlock();
                        if (block.getType().isSolid()) {
                            Material ore = random.nextDouble() < 0.1 ? Material.DIAMOND_ORE : Material.IRON_ORE;
                            block.setType(ore);
                        }
                    }
                }
            }
        }
    }

    private void applyAuroraEffects() {
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("game.world_name", "world"));
        if (world == null) return;

        // Only at night if configured
        boolean nightOnly = plugin.getConfig().getBoolean("world_events.aurora.night_only", true);
        if (nightOnly && (world.getTime() < 13000 || world.getTime() > 23000)) {
            return;
        }

        int speedLevel = plugin.getConfig().getInt("world_events.aurora.speed_amplifier", 1);

        // Give speed to all alive players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().getPlayerData(player).isAlive()) {
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    120, // 6 seconds
                    speedLevel - 1,
                    false,
                    false
                ));

                // Spawn aurora particles
                if (random.nextDouble() < 0.3) {
                    Location loc = player.getLocation().add(0, 10, 0);
                    world.spawnParticle(Particle.END_ROD, loc, 5, 10, 2, 10, 0.05);
                    world.spawnParticle(Particle.GLOW, loc, 3, 8, 2, 8, 0.02);
                }
            }
        }
    }

    private void applyFogEffects() {
        // Spawn fog particles around players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().getPlayerData(player).isAlive()) {
                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 5, 2, 5, 0.02);

                int blindnessLevel = plugin.getConfig().getInt("world_events.fog.blindness_amplifier", 0);
                if (blindnessLevel > 0) {
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        120,
                        blindnessLevel - 1,
                        false,
                        false
                    ));
                }
            }
        }
    }

    private void applyHeatwaveEffects() {
        double damage = plugin.getConfig().getDouble("world_events.heatwave.damage_per_tick", 0.5);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!plugin.getGameManager().getPlayerData(player).isAlive()) continue;

            Location loc = player.getLocation();

            // Check if outdoors (can see sky)
            if (loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY()) {
                player.damage(damage);
                player.sendMessage(ChatColor.RED + "☀ Heatwave! Seek shade!");

                // Heat particles
                if (random.nextDouble() < 0.5) {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }
    }

    /**
     * Get active event (if any)
     */
    public WorldEvent getActiveEvent() {
        return activeEvent;
    }

    /**
     * Check if a specific event type is active
     */
    public boolean isEventActive(EventType type) {
        return activeEvent != null && activeEvent.type == type;
    }
}
