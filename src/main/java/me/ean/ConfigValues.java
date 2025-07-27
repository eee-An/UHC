package me.ean;


import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.*;
import lombok.Getter;

@Getter
public class ConfigValues {
    private final JavaPlugin plugin;
    private String worldName;
    private final List<Location> spawnLokacije = new ArrayList<>();

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

    private final List<Location> winnerCeremonyWinnerTeleport = new ArrayList<>();
    private final List<Location> winnerCeremonySpectatorTeleport = new ArrayList<>();

    private final YamlDocument yamlConfig;

    private final List<ScheduledAction> scheduledActions = new ArrayList<>();

    public ConfigValues(Main plugin, YamlDocument config) {
        this.plugin = plugin;
        this.yamlConfig = config;
    }

    public void reloadConfig(){
        loadConfigValues();
    }

    public void loadConfigValues() {
        worldName = yamlConfig.getString("world-name");
        spawnLokacije.clear();
        List<Map<?, ?>> spawnLocations = yamlConfig.getMapList("spawn-locations");
        World world = Bukkit.getWorld(worldName);
        for (Map<?, ?> loc : spawnLocations) {
            double x = ((Number) loc.get("X")).doubleValue();
            double y = ((Number) loc.get("Y")).doubleValue();
            double z = ((Number) loc.get("Z")).doubleValue();
            spawnLokacije.add(new Location(world, x, y, z));
        }
        goldenAppleLimit = yamlConfig.getInt("golden-apple-limit");
        goldenAppleLimitWarningMessage = yamlConfig.getString("golden-apple-limit-warning-message");

        borderMovements = yamlConfig.getMapList("border-movements");
        borderMovementStartMessage = yamlConfig.getString("border-movement-start-message");
        borderMovementStartWarningMessage = yamlConfig.getString("border-movement-start-warning-message");
        borderMovementStartWarningTimes = yamlConfig.getIntList("border-movement-start-warning-times");

        bannedItemRemovealMessage = yamlConfig.getString("banned-item-removal-message");
        bannedItems = yamlConfig.getStringList("banned-items");

        supplyDropLootable = yamlConfig.getString("supply-drop-loot-table");
        supplyDropDroppingSpeed = yamlConfig.getDouble("supply-drop-droping-speed");
        supplyDropLandingMessage = yamlConfig.getString("supply-drop-landing-message");
        supplyDropOpenedMessage = yamlConfig.getString("supply-drop-opened-message");

        List<Map<?, ?>> winnerLoc = yamlConfig.getMapList("winner-ceremony-winner-teleport");
        for( Map<?, ?> loc : winnerLoc) {
            double x = ((Number) loc.get("X")).doubleValue();
            double y = ((Number) loc.get("Y")).doubleValue();
            double z = ((Number) loc.get("Z")).doubleValue();
            float yaw = loc.containsKey("yaw") && loc.get("yaw") instanceof Number ? ((Number) loc.get("yaw")).floatValue() : 0f;
            float pitch = loc.containsKey("pitch") && loc.get("pitch") instanceof Number ? ((Number) loc.get("pitch")).floatValue() : 0f;
            winnerCeremonyWinnerTeleport.add(new Location(world, x, y, z, yaw, pitch));
        }

        List<Map<?, ?>> spectatorLoc = yamlConfig.getMapList("winner-ceremony-spectator-teleport");
        for( Map<?, ?> loc : spectatorLoc) {
            double x = ((Number) loc.get("X")).doubleValue();
            double y = ((Number) loc.get("Y")).doubleValue();
            double z = ((Number) loc.get("Z")).doubleValue();
            float yaw = loc.containsKey("yaw") ? ((Number) loc.get("yaw")).floatValue() : 0f;
            float pitch = loc.containsKey("pitch") ? ((Number) loc.get("pitch")).floatValue() : 0f;
            winnerCeremonySpectatorTeleport.add(new Location(world, x, y, z, yaw, pitch));
            }

        plugin.getLogger().info("winnerLoc: " + winnerCeremonyWinnerTeleport);
        plugin.getLogger().info("spectatorLoc: " + winnerCeremonySpectatorTeleport);

        scheduledActions.clear();
        List<Map<?, ?>> actions = yamlConfig.getMapList("scheduled-actions");
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
