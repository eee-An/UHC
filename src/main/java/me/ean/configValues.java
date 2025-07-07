package me.ean;


import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.*;
import org.bukkit.loot.Lootable;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import lombok.Getter;

public class configValues {
    private final @Getter JavaPlugin plugin;
    private @Getter String worldName;
    private @Getter List<Location> spawnLokacije = new ArrayList<>();

    private @Getter int goldenAppleLimit;
    private @Getter String goldenAppleLimitWarningMessage;

    private @Getter List<Map<?, ?>> borderMovements;
    private @Getter String borderMovementStartMessage;
    private @Getter String borderMovementStartWarningMessage;
    private @Getter List<Integer> borderMovementStartWarningTimes;

    private @Getter String bannedItemRemovealMessage;
    private @Getter List<String> bannedItems;

    private @Getter String supplyDropLootable;
    private @Getter String supplyDropLandingMessage;
    private @Getter String supplyDropOpenedMessage;

    private @Getter YamlDocument config;

    public configValues(JavaPlugin plugin, YamlDocument config) {
        this.plugin = plugin;
        this.config = config;
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
        supplyDropLandingMessage = config.getString("supply-drop-landing-message");
        supplyDropOpenedMessage = config.getString("supply-drop-opened-message");


    }

}
