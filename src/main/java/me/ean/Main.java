package me.ean;

import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
    @Getter
    private static Main instance;
    private GameState state = GameState.WAITING;
    private List<Player> igraci = new ArrayList<>();
    private World uhcWorld;
    private Map<Player, Integer> pojedeneJabuke = new HashMap<>();
    private File configFile = new File(getDataFolder(), "config.yml");
    private YamlDocument config;
    private boolean uhcActive = false;
    private WorldBorderManager borderManager;

    private @Getter ConfigValues configValues;

    @Override
    public void onEnable(){
        instance = this;

        // Copy the default config if it doesn't exist
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }

        try {
            config = YamlDocument.create(configFile, getResource("config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Load the config values
        configValues = new ConfigValues(this, config);
        configValues.loadConfigValues();

        String worldName = configValues.getWorldName();
        uhcWorld = Bukkit.getWorld(worldName);


        this.getCommand("startuhc").setExecutor(this);
        this.getCommand("resetstate").setExecutor(this);
        this.getCommand("bacisupplydrop").setExecutor(this);
        this.getCommand("configreload").setExecutor(this);
        Objects.requireNonNull(this.getCommand("testborder")).setExecutor(new WorldBorderMover(this));
        this.getCommand("enduhc").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        try {
            config.save();
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (state == GameState.WAITING) {
            igraci.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (state == GameState.WAITING || state == GameState.COUNTDOWN) {
            igraci.remove(event.getPlayer());
        }
    }


    public boolean isUhcActive() {
        return uhcActive;
    }

    public void setUhcActive(boolean uhcActive) {
        this.uhcActive = uhcActive;
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("startuhc")) {
            Bukkit.broadcastMessage("evo igraci: " + String.join(", ", igraci.stream().map(Player::getName).toList()));
            state = GameState.COUNTDOWN;
            new BukkitRunnable() {
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
            }.runTaskTimer(this, 0, 1);
        } else if (label.equalsIgnoreCase("resetstate")) {
            state = GameState.WAITING;
        } else if (label.equalsIgnoreCase("bacisupplydrop")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("konzola ne moze bacati dropove");
                return true;
            }
            SupplyDrop drop = new SupplyDrop(uhcWorld);
            try {
                drop.dropAt(player.getLocation().clone().add(0, 10.0, 0));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
//            player.sendMessage("Spawnan supply drop");
        }else if (label.equalsIgnoreCase("enduhc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("uhc.end")) {
                    endUHC();
                    player.sendMessage("UHC has ended and the border has been reset.");
                } else {
                    player.sendMessage("You do not have permission to execute this command.");
                }
            } else {
                endUHC();
                sender.sendMessage("UHC has ended and the border has been reset.");
            }
            return true;
        }else if (label.equalsIgnoreCase("configreload")) {
            configValues.reloadConfig();
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

                // Dodavanje particlesa na blok na kojem igrač stoji
                Location blockLocation = spawnLocation.clone();
                blockLocation.setY(blockLocation.getBlockY() + 1); // Postavite particle malo iznad bloka
                borderManager.getParticleManager().spawnCustomEffect(blockLocation);
            }, 1);


        });


        // Schedule actions

        for (ScheduledAction action : configValues.getScheduledActions()) {
            long delayTicks = (action.getTime().getSeconds()) * 20;
            Bukkit.getScheduler().runTaskLater(this, () -> {
                switch (action.getAction().toLowerCase()) {
                    case "border": {
                        Bukkit.broadcastMessage("Pokretanje bordera!");
                        borderManager.scheduleBorderMovement(
                                (double) action.getParams().get("X"),
                                (double) action.getParams().get("Z"),
                                (double) action.getParams().get("size"),
                                (int) action.getParams().get("delay") * 20,
                                (int) action.getParams().get("duration") * 20);
                        break;
                    }
                    case "supplydrop":{
                        Bukkit.broadcastMessage("Pada Supply Drop!");
                        try {
                            SupplyDrop drop = new SupplyDrop(uhcWorld);
                            drop.dropAt(new Location(uhcWorld,
                                    (double) action.getParams().get("X"),
                                    (double) action.getParams().get("Y"),
                                    (double) action.getParams().get("Z")));

                            Bukkit.broadcastMessage("X,Y,Z: " +
                                    action.getParams().get("X") + ", " +
                                    action.getParams().get("Y") + ", " +
                                    action.getParams().get("Z"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                        // Add more cases as needed
                }
            }, delayTicks);
        }
    }

    private void endUHC(){
        // Reset UHC-specific conditions
        uhcActive = false;

        // Clear scheduled border movements and reset the border
        borderManager.clearScheduledMovements();
        WorldBorder border = Bukkit.getWorld("world").getWorldBorder();
        border.setSize(75); // Set the border to the initial size (e.g., 75 blocks)
        border.setCenter(0, 0); // Set the border center to the initial position (e.g., 0, 0)
    }


    private void saveDefaultSchematic(String fileName) {
        File schematicFile = new File(getDataFolder(), fileName);
        if (!schematicFile.exists()) {
            saveResource(fileName, false);
            getLogger().info("Schematic file '" + fileName + "' has been saved to the plugin data folder.");
        }
    }

}