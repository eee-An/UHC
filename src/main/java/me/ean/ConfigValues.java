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
    private final List<Location> spawnLokacije = new ArrayList<>();
    private Difficulty gameDifficulty;
    private double initialBorderCenterX;
    private double initialBorderCenterZ;
    private double initialBorderSize;

    private boolean clearInventoryOnStart;
    private boolean clearEnderChestOnStart;
    private double startHealth;
    private int startFoodLevel;
    private int startLevel;
    private float startExperience;
    private boolean spawnSlownessEnabled;
    private int spawnSlownessDuration;
    private int spawnSlownessAmplifier;

    private boolean borderWarningsEnabled;

    private int goldenAppleLimit;
    private String goldenAppleLimitWarningMessage;

    private List<Map<?, ?>> borderMovements;
    private String borderMovementStartMessage;
    private String borderMovementStartWarningMessage;
    private List<Integer> borderMovementStartWarningTimes;

    private String bannedItemRemovealMessage;
    private List<String> bannedItems;

    private String supplyDropLootable;
    private String supplyDropSchematic;
    private double supplyDropDroppingSpeed;
    private String supplyDropLandingMessage;
    private String supplyDropOpenedMessage;
    private boolean supplyDropGlowing;
    private boolean supplyDropCompassBarEnabled;
    private boolean supplyDropBeaconEnabled;

    private final List<Location> winnerCeremonyWinnerTeleport = new ArrayList<>();
    private final List<Location> winnerCeremonySpectatorTeleport = new ArrayList<>();
    private boolean winnerFireworksEnabled;
    private int winnerFireworksWaves;
    private int winnerFireworksPerWave;
    private long winnerFireworksIntervalTicks;
    private int winnerFireworkPower;
    private List<Color> winnerFireworkColors;

    private final YamlDocument yamlConfig;

    private final List<ScheduledAction> scheduledActions = new ArrayList<>();
    private String configReloadedMessage;
    private String uhcStartedMessage;
    private String uhcEndedMessage;
    private String notEnoughSpawnsMessage;
    private String lootTableMissingMessage;
    private int startCountdownSeconds;
    private String startCountdownTitle;
    private String startCountdownSubtitle;
    private String uhcStartTitle;
    private String uhcStartSubtitle;
    private String startIntroTitle;
    private String startIntroSubtitle;
    private int startIntroDurationSeconds;
    private String resultsHeader;
    private String resultsTitle;
    private String resultsWinnerLine;
    private String resultsFirstKillerLine;
    private String resultsSecondKillerLine;
    private String resultsThirdKillerLine;
    private String resultsHonorableMentionsTitle;
    private String resultsHonorableMentionLine;
    private String resultsFooter;
    private String winnerAnnouncementTitle;
    private String winnerAnnouncementSubtitle;
    private int winnerAnnouncementFadeInTicks;
    private int winnerAnnouncementStayTicks;
    private int winnerAnnouncementFadeOutTicks;

    public ConfigValues(Main plugin, YamlDocument config) {
        this.plugin = plugin;
        this.yamlConfig = config;
    }

    public void reloadConfig(){
        loadConfigValues();
    }

    public void loadConfigValues() {
        worldName = yamlConfig.getString("world-name");
        gameDifficulty = Difficulty.valueOf(yamlConfig.getString("game.difficulty", "HARD").toUpperCase(Locale.ROOT));
        initialBorderCenterX = yamlConfig.getDouble("game.initial-border.center-x", 477.5);
        initialBorderCenterZ = yamlConfig.getDouble("game.initial-border.center-z", -450.5);
        initialBorderSize = yamlConfig.getDouble("game.initial-border.size", 4000.0);

        clearInventoryOnStart = yamlConfig.getBoolean("start-settings.clear-inventory", true);
        clearEnderChestOnStart = yamlConfig.getBoolean("start-settings.clear-ender-chest", true);
        startHealth = yamlConfig.getDouble("start-settings.health", 20.0);
        startFoodLevel = yamlConfig.getInt("start-settings.food-level", 20);
        startLevel = yamlConfig.getInt("start-settings.level", 0);
        startExperience = yamlConfig.getDouble("start-settings.experience", 0.0).floatValue();
        spawnSlownessEnabled = yamlConfig.getBoolean("start-settings.spawn-slowness.enabled", true);
        spawnSlownessDuration = yamlConfig.getInt("start-settings.spawn-slowness.duration", 10);
        spawnSlownessAmplifier = yamlConfig.getInt("start-settings.spawn-slowness.amplifier", 10);
        borderWarningsEnabled = yamlConfig.getBoolean("border-warnings.enabled", true);

        configReloadedMessage = yamlConfig.getString("messages.config-reloaded", "§aConfig je uspješno učitan.");
        uhcStartedMessage = yamlConfig.getString("messages.uhc-started", "§aUHC je počeo!");
        uhcEndedMessage = yamlConfig.getString("messages.uhc-ended", "§cUHC je završen.");
        notEnoughSpawnsMessage = yamlConfig.getString("messages.not-enough-spawns", "§cNema dovoljno spawn lokacija za sve igrače.");
        lootTableMissingMessage = yamlConfig.getString("messages.loot-table-missing", "§cSupply drop loot table ne postoji.");
        startCountdownSeconds = yamlConfig.getInt("start-countdown.seconds", 5);
        startCountdownTitle = yamlConfig.getString("start-countdown.title", "§6UHC");
        startCountdownSubtitle = yamlConfig.getString("start-countdown.subtitle", "§fPočinje za §e{seconds} §fsekundi");
        uhcStartTitle = yamlConfig.getString("start-countdown.started-title", "§aUHC JE POČEO!");
        uhcStartSubtitle = yamlConfig.getString("start-countdown.started-subtitle", "§fSretno svima!");
        startIntroTitle = yamlConfig.getString("start-countdown.intro-title", "§6Dobro dosli u UHC sezone 6");
        startIntroSubtitle = yamlConfig.getString("start-countdown.intro-subtitle", "§eFloxyCrafta");
        startIntroDurationSeconds = yamlConfig.getInt("start-countdown.intro-duration-seconds", 3);
        resultsHeader = yamlConfig.getString("results.header", "§6§l----- Floxy UHC - REZULTATI -----");
        resultsTitle = yamlConfig.getString("results.title", "        §6§lFloxyCraft Sezona 6 UHC");
        resultsWinnerLine = yamlConfig.getString("results.winner-line", "§e§lPobjednik: §f{player}");
        resultsFirstKillerLine = yamlConfig.getString("results.first-killer-line", "§6§l1. §f{player} §7- §c{kills} killova");
        resultsSecondKillerLine = yamlConfig.getString("results.second-killer-line", "§f§l2. §f{player} §7- §c{kills} killova");
        resultsThirdKillerLine = yamlConfig.getString("results.third-killer-line", "§c§l3. §f{player} §7- §c{kills} killova");
        resultsHonorableMentionsTitle = yamlConfig.getString("results.honorable-mentions-title", "§6§lHonorable mentions:");
        resultsHonorableMentionLine = yamlConfig.getString("results.honorable-mention-line", "§7- §f{player} §7- §c{kills} killova");
        resultsFooter = yamlConfig.getString("results.footer", "§6§l--------------------------------");
        winnerAnnouncementTitle = yamlConfig.getString("winner-announcement.title", "{player}");
        winnerAnnouncementSubtitle = yamlConfig.getString("winner-announcement.subtitle", "je osvojio Floxy UHC Sezona 6");
        winnerAnnouncementFadeInTicks = yamlConfig.getInt("winner-announcement.fade-in-ticks", 5);
        winnerAnnouncementStayTicks = yamlConfig.getInt("winner-announcement.stay-ticks", 100);
        winnerAnnouncementFadeOutTicks = yamlConfig.getInt("winner-announcement.fade-out-ticks", 5);

        spawnLokacije.clear();
        List<Map<?, ?>> spawnLocations = yamlConfig.getMapList("spawn-locations");
        World world = Bukkit.getWorld(worldName);
        for (Map<?, ?> loc : spawnLocations) {
            double x = ((Number) loc.get("X")).doubleValue();
            double y = ((Number) loc.get("Y")).doubleValue();
            double z = ((Number) loc.get("Z")).doubleValue();
            float yaw = loc.containsKey("yaw") && loc.get("yaw") instanceof Number ? ((Number) loc.get("yaw")).floatValue() : 0f;
            float pitch = loc.containsKey("pitch") && loc.get("pitch") instanceof Number ? ((Number) loc.get("pitch")).floatValue() : 0f;
            spawnLokacije.add(new Location(world, x, y, z, yaw, pitch));
        }
        goldenAppleLimit = yamlConfig.getInt("golden-apple-limit");
        goldenAppleLimitWarningMessage = yamlConfig.getString("golden-apple-limit-warning-message");

        borderMovements = yamlConfig.getMapList("border-movements");
        borderMovementStartMessage = yamlConfig.getString("border-movement-start-message");
        borderMovementStartWarningMessage = yamlConfig.getString("border-movement-start-warning-message");
        borderMovementStartWarningTimes = yamlConfig.getIntList(
                "border-warnings.times",
                yamlConfig.getIntList("border-movement-start-warning-times", List.of(30, 10, 3, 2, 1)));

        bannedItemRemovealMessage = yamlConfig.getString("banned-item-removal-message");
        bannedItems = yamlConfig.getStringList("banned-items");

        supplyDropLootable = yamlConfig.getString("supply-drop-loot-table");
        String configuredSchematic = yamlConfig.getString("supply-drop-schematic");
        supplyDropSchematic = configuredSchematic == null || configuredSchematic.isBlank()
                ? "balon2.schem"
                : configuredSchematic;
        supplyDropDroppingSpeed = yamlConfig.getDouble("supply-drop-droping-speed");
        supplyDropLandingMessage = yamlConfig.getString("supply-drop-landing-message");
        supplyDropOpenedMessage = yamlConfig.getString("supply-drop-opened-message");
        supplyDropGlowing = yamlConfig.getBoolean("supply-drop.glowing", true);
        supplyDropCompassBarEnabled = yamlConfig.getBoolean("supply-drop.compass-bar-enabled", true);
        supplyDropBeaconEnabled = yamlConfig.getBoolean("supply-drop.beacon-enabled", true);

        winnerCeremonyWinnerTeleport.clear();
        winnerCeremonySpectatorTeleport.clear();
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

        winnerFireworksEnabled = yamlConfig.getBoolean("winner-ceremony.fireworks.enabled", true);
        winnerFireworksWaves = yamlConfig.getInt("winner-ceremony.fireworks.waves", 10);
        winnerFireworksPerWave = yamlConfig.getInt("winner-ceremony.fireworks.fireworks-per-wave", 6);
        winnerFireworksIntervalTicks = yamlConfig.getLong("winner-ceremony.fireworks.interval-ticks", 40L);
        winnerFireworkPower = yamlConfig.getInt("winner-ceremony.fireworks.power", 0);
        winnerFireworkColors = yamlConfig.getStringList(
                        "winner-ceremony.fireworks.colors",
                        List.of("#FFFF00", "#FF0000", "#FFA500")).stream()
                .map(this::parseColor)
                .toList();
        if (winnerFireworkColors.isEmpty()) {
            throw new IllegalArgumentException("At least one winner firework color must be configured");
        }

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

    private Color parseColor(String value) {
        String hex = value.startsWith("#") ? value.substring(1) : value;
        if (!hex.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Invalid winner firework color: " + value);
        }
        return Color.fromRGB(Integer.parseInt(hex, 16));
    }

}
