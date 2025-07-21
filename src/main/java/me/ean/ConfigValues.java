package me.ean;


import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.*;
import lombok.Getter;

@Getter
public class ConfigValues {
    private final JavaPlugin plugin;
    private String worldName;
    private List<Location> spawnLokacije = new ArrayList<>();

    private int goldenAppleLimit;
    private String goldenAppleLimitWarningMessage;

    private List<Map<?, ?>> borderMovements;
    private String borderMovementStartMessage;
    private String borderMovementStartWarningMessage;
    private List<Integer> borderMovementStartWarningTimes;

    private String bannedItemRemovealMessage;
    private List<String> bannedItems;

    private String supplyDropLootable;
    private double supplyDropDroppingSpeed;
    private String supplyDropLandingMessage;
    private String supplyDropOpenedMessage;

    private YamlDocument config;

    private List<ScheduledAction> scheduledActions = new ArrayList<>();

    public ConfigValues(JavaPlugin plugin, YamlDocument config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reloadConfig(){
        loadConfigValues();
    }

    public void loadConfigValues() {
        worldName = config.getString("world-name");
        spawnLokacije.clear();
        List<Map<?, ?>> spawnLocations = config.getMapList("spawn-locations");
        World world = Bukkit.getWorld(worldName);
        for (Map<?, ?> loc : spawnLocations) {
            double x = ((Number) loc.get("X")).doubleValue();
            double y = ((Number) loc.get("Y")).doubleValue();
            double z = ((Number) loc.get("Z")).doubleValue();
            spawnLokacije.add(new Location(world, x, y, z));
        }
        goldenAppleLimit = config.getInt("golden-apple-limit");
        goldenAppleLimitWarningMessage = config.getString("golden-apple-limit-warning-message");

        borderMovements = config.getMapList("border-movements");
        borderMovementStartMessage = config.getString("border-movement-start-message");
        borderMovementStartWarningMessage = config.getString("border-movement-start-warning-message");
        borderMovementStartWarningTimes = config.getIntList("border-movement-start-warning-times");

        bannedItemRemovealMessage = config.getString("banned-item-removal-message");
        bannedItems = config.getStringList("banned-items");

        supplyDropLootable = config.getString("supply-drop-loot-table");
        supplyDropDroppingSpeed = config.getDouble("supply-drop-droping-speed");
        supplyDropLandingMessage = config.getString("supply-drop-landing-message");
        supplyDropOpenedMessage = config.getString("supply-drop-opened-message");

        scheduledActions.clear();
        List<Map<?, ?>> actions = config.getMapList("scheduled-actions");
        for (Map<?, ?> entry : actions) {
            String timeStr = (String) entry.get("time");
            String action = (String) entry.get("action");
            Map<String, Object> params = (Map<String, Object>) entry.get("params");
            Duration time = parseDuration(timeStr);
            scheduledActions.add(new ScheduledAction(time, action, params));
        }

    }

    private Duration parseDuration(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

}
