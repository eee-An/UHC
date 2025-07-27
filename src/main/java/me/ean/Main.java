package me.ean;

import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Native;
import java.util.*;

@Getter
@Setter
@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private static Main instance;
    private GameState state = GameState.WAITING;
    private Map<UUID,PlayerState> playerStates = new HashMap<>();
    private List<Player> igraci = new ArrayList<>();
    private World uhcWorld;
    private Map<Player, Integer> pojedeneJabuke = new HashMap<>();
    private File configFile = new File(getDataFolder(), "config.yml");
    private YamlDocument yamlConfig;
    private boolean uhcActive = false;
    private WorldBorderManager borderManager;
    private ConfigValues configValues;
    private ParticleManager particleManager = new ParticleManager(this);
    private WinnerCeremonyManager winnerCeremonyManager;
    private final List<Long> dropSeconds = new ArrayList<>();
    private DropState dropState = DropState.WAITING; // Initial state for supply drop
    private long uhcStartTime = -1;

    private final List<TopKiller> topKillers = new ArrayList<>();
    private final List<BukkitRunnable> activeTasks = new ArrayList<>();



    @Override
    public void onEnable(){
        instance = this;

        new UHCPlaceholder(this).register();
        winnerCeremonyManager = new WinnerCeremonyManager(this);

        // Copy the default config if it doesn't exist
        saveDefaultConfig();

        var _config = getResource("config.yml");
        if (_config == null) {
            // Shouldn't happen.
            throw new RuntimeException("Unable to create config.yml!");
        }
        try {
            yamlConfig = YamlDocument.create(configFile, _config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse config", e);
        }

        // Load the config values
        configValues = new ConfigValues(this, yamlConfig);
        configValues.loadConfigValues();

        String worldName = configValues.getWorldName();
        uhcWorld = Bukkit.getWorld(worldName);

        registerCommands();
        Objects.requireNonNull(this.getCommand("testborder")).setExecutor(new WorldBorderMover(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        try {
            yamlConfig.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Save the schematic file to the plugin's data folder
        saveDefaultSchematic("balon.schem");

        // Register the item pickup listener
        getServer().getPluginManager().registerEvents(new ItemPickupListener(this), this);

        // Register the player kill listener
        getServer().getPluginManager().registerEvents(new PlayerKillListener(this), this);

        World world = getServer().getWorld("world");
        if (world != null) {
            borderManager = new WorldBorderManager(this, world.getWorldBorder());
        }

    }

    private void registerCommands() {
        /* Register commands whose listener is this class (onCommand method) */
        for (String command : Arrays.asList(
                "startuhc", "resetstate", "bacisupplydrop", "configreload", "enduhc", "winnerceremony"
        )) {
            var cmd = getCommand(command);
            if (cmd == null) {
                getLogger().severe("Command '" + command + "' is not defined in plugin.yml!");
            } else {
                cmd.setExecutor(this);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (state == GameState.WAITING) {
            igraci.add(event.getPlayer());
        }
        Player player = event.getPlayer();
        if(!playerStates.containsKey(player.getUniqueId())) {
            playerStates.put(player.getUniqueId(),PlayerState.WAITING);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (state == GameState.WAITING || state == GameState.COUNTDOWN) {
            igraci.remove(event.getPlayer());
        }
    }


    @EventHandler
    public void gappleCounter(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        int limit = configValues.getGoldenAppleLimit();
        String warningMessage = configValues.getGoldenAppleLimitWarningMessage();
        if (igraci.contains(player) && event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            int count = pojedeneJabuke.getOrDefault(player, 0);
            if (count >= limit) {
                event.setCancelled(true);
                player.sendMessage(warningMessage);
            } else {
                pojedeneJabuke.put(player, count + 1);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("startuhc")) {
//            Bukkit.broadcastMessage("evo igraci: " + String.join(", ", igraci.stream().map(Player::getName).toList()));
            state = GameState.COUNTDOWN;
            BukkitRunnable updater = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    ticks++;
                    if (ticks == 100) {
                        Main.this.startUHC(sender);
                        this.cancel();
                    } else if (ticks % 20 == 1) {
                        Bukkit.broadcastMessage("UHC pocinje za " + (101 - ticks) / 20 + " sekundi!");
                        //TODO: Sredi broadcast preko titla
                    }
                }
            };
            registerTask(updater);
            updater.runTaskTimer(this, 0, 1);
        } else if (label.equalsIgnoreCase("resetstate")) {
            state = GameState.WAITING;
        } else if (label.equalsIgnoreCase("bacisupplydrop")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("konzola ne moze bacati dropove");
                return true;
            }
            SupplyDrop drop = new SupplyDrop(uhcWorld,this);
            try {
                drop.dropAt(player.getLocation().clone().add(0, 10.0, 0));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
//            player.sendMessage("Spawnan supply drop");
        }else if (label.equalsIgnoreCase("enduhc")) {
            if (sender instanceof Player player) {
                if (player.hasPermission("uhc.end")) {
                    endUhc();
                    player.sendMessage("UHC has ended and the border has been reset.");
                } else {
                    player.sendMessage("You do not have permission to execute this command.");
                }
            } else {
                endUhc();
                sender.sendMessage("UHC has ended and the border has been reset.");
            }
            return true;
        }else if (label.equalsIgnoreCase("configreload")) {
            sender.sendMessage("§aReloading config...");
            try {
                yamlConfig.reload(); // Reload YAML from disk
                configValues.loadConfigValues(); // Refresh config values
                sender.sendMessage("§aConfig reloaded successfully.");
            } catch (IOException e) {
                sender.sendMessage("§cFailed to reload config: " + e.getMessage());
            }
        } else if (label.equalsIgnoreCase("winnerceremony")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Konzola ne moze pokrenuti ceremoniju pobjednika.");
                return true;
            }
            if (igraci.isEmpty()) {
                player.sendMessage("Nema igraca za ceremoniju pobjednika.");
                return true;
            }
            if (winnerCeremonyManager == null) {
                winnerCeremonyManager = new WinnerCeremonyManager(this);
            }
            winnerCeremonyManager.celebrateWinner();
        }
        return true;
    }

    public boolean provjeriJelMoguceStartat(CommandSender sender) {
        if (igraci.size() > configValues.getSpawnLokacije().size()) {
            sender.sendMessage("ima vise igraca nego spawn lokacija!!");
            return false;
        }
        return true;
    }
    
    public void startUHC(CommandSender sender) {
        if (!provjeriJelMoguceStartat(sender))
            return;
        uhcActive = true;
        state = GameState.PLAYING;
        uhcStartTime = System.currentTimeMillis();

        topKillers.clear();

        Collections.shuffle(configValues.getSpawnLokacije());

        Scoreboard srca = Bukkit.getScoreboardManager().getNewScoreboard();
        srca.registerNewObjective("hp", Criteria.HEALTH, "srca", RenderType.HEARTS);

        // TODO: teleportiraj igrace, odradi sve sto treba za pocetak igre  (npr. border, supply drops, ...)
        igraci.forEach(p -> {
            p.getInventory().clear();
            p.getEnderChest().clear();
            //  p.teleport(spawnLokacije.remove(0));  // ignoriraj "player moved too fast!" u konzoli
            getServer().getScheduler().runTaskLater(this, () -> {
                Location spawnLocation = configValues.getSpawnLokacije().remove(0);
                p.teleport(spawnLocation);
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20);
                p.setFoodLevel(20);
                p.setLevel(0);
                p.setExp(0);
                p.setScoreboard(srca);

                playerStates.put(p.getUniqueId(), PlayerState.PLAYING); // Postavi stanje igrača na PLAYING

//                playerKills.put(p, 0); // Inicijaliziraj broj ubistava za igrača


                // Dodavanje particlesa na blok na kojem igrač stoji
                Location blockLocation = spawnLocation.clone();
                blockLocation.setY(blockLocation.getBlockY() + 1); // Postavite particle malo iznad bloka
                borderManager.getParticleManager().spawnCustomEffect(blockLocation);

            }, 1);


        });


        borderManager.startBorderCenterParticles();

        // Schedule actions


        for(ScheduledAction action : configValues.getScheduledActions()){
            long time = action.getTime().getSeconds();
            switch (action.getAction().toLowerCase()) {
                case "supplydrop": {
                    dropSeconds.add(time);
                    break;
                }
            }
        }

        for (ScheduledAction action : configValues.getScheduledActions()) {
            long delayTicks = (action.getTime().getSeconds()) * 20;
            BukkitRunnable scheduledRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    switch (action.getAction().toLowerCase()) {
                        case "border": {
                            borderManager.scheduleBorderMovement(
                                    (double) action.getParams().get("X"),
                                    (double) action.getParams().get("Z"),
                                    (double) action.getParams().get("size"),
                                    (int) action.getParams().get("delay") * 20,
                                    (int) action.getParams().get("duration") * 20);
                            break;
                        }
                        case "supplydrop": {
                            try {
                                SupplyDrop drop = new SupplyDrop(uhcWorld, Main.this);
                                drop.dropAt(new Location(uhcWorld,
                                        (double) action.getParams().get("X"),
                                        (double) action.getParams().get("Y"),
                                        (double) action.getParams().get("Z")));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            };
            registerTask(scheduledRunnable);
            scheduledRunnable.runTaskLater(this, delayTicks);
        }
    }

    public void endUhc(){
        // Reset UHC-specific conditions:
        uhcActive = false;
        stopAllTasks();
        state = GameState.ENDED;

        // Clear scheduled border movements and reset the border
        borderManager.clearScheduledMovements();
        WorldBorder border = Bukkit.getWorld("world").getWorldBorder();
        border.setSize(75); // Set the border to the initial size (e.g., 75 blocks)
        border.setCenter(0, 0); // Set the border center to the initial position (e.g., 0, 0)

        borderManager.stopBorderCenterParticles();
    }


    private void saveDefaultSchematic(String fileName) {
        File schematicFile = new File(getDataFolder(), fileName);
        if (!schematicFile.exists()) {
            saveResource(fileName, false);
            getLogger().info("Schematic file '" + fileName + "' has been saved to the plugin data folder.");
        }
    }

    public void addKill(Player player) {
        for (TopKiller tk : topKillers) {
            if (tk.getUuid().equals(player.getUniqueId())) {
                tk.incrementKills();
                topKillers.sort(null);
                return;
            }
        }
        topKillers.add(new TopKiller(player, 1));
        topKillers.sort(null);
    }

    public void registerTask(BukkitRunnable task) {
        activeTasks.add(task);
    }

    public void stopAllTasks() {
        for (BukkitRunnable task : activeTasks) {
            task.cancel();
        }
        activeTasks.clear();
    }

}
