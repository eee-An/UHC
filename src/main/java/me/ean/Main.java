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
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
    @Getter
    private static Main instance;
    private GameState state = GameState.WAITING;
    private List<Player> igraci = new ArrayList<>();
    private World uhcWorld;
    private List<Location> spawnLokacije = new ArrayList<>();
    private Map<Player, Integer> pojedeneJabuke = new HashMap<>();
    private File configFile = new File(getDataFolder(), "config.yml");
    private YamlDocument config;
    private boolean uhcActive = false;
    private WorldBorderManager borderManager;


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

        String worldName = config.getString("world-name");
        uhcWorld = Bukkit.getWorld(worldName);

        List<Map<?, ?>> spawnLocations = getConfig().getMapList("spawn-locations");
        for (Map<?, ?> loc : spawnLocations) {
            double x = ((Number) loc.get("X")).doubleValue();
            double y = ((Number) loc.get("Y")).doubleValue();
            double z = ((Number) loc.get("Z")).doubleValue();
            spawnLokacije.add(new Location(uhcWorld, x, y, z));
        }

        this.getCommand("startuhc").setExecutor(this);
        this.getCommand("resetstate").setExecutor(this);
        this.getCommand("bacisupplydrop").setExecutor(this);
        Objects.requireNonNull(this.getCommand("testborder")).setExecutor(new WorldBorderMover(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        try {
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Register the item pickup listener
        getServer().getPluginManager().registerEvents(new ItemPickupListener(this), this);

        // Register the player kill listener
        getServer().getPluginManager().registerEvents(new PlayerKillListener(this), this);

        // Register command
        World world = getServer().getWorld("world");
        if (world != null) {
            borderManager = new WorldBorderManager(this, world.getWorldBorder());
        }
        this.getCommand("enduhc").setExecutor(new EndUHCCommand(this, borderManager));
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

    public void endUHC() {
        uhcActive = false;
        // Additional logic to end UHC, if needed
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
        int limit = getConfig().getInt("max-golden-apples");
        String warningMessage = getConfig().getString("gapple-limit-warning-message");
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
            SupplyDrop drop = new SupplyDrop(uhcWorld, 2, 98, -5, 4, 102, -3);
            drop.dropAt(player.getLocation().clone().add(0, 10.0, 0));
            player.sendMessage("Spawnan supply drop");
        }
        
        return true;
    }

    public boolean provjeriJelMoguceStartat(CommandSender sender) {
        if (igraci.size() > spawnLokacije.size()) {
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

        Collections.shuffle(spawnLokacije);

        Scoreboard srca = Bukkit.getScoreboardManager().getNewScoreboard();
        srca.registerNewObjective("hp", Criteria.HEALTH, "srca", RenderType.HEARTS);

        // TODO: teleportiraj igrace, odradi sve sto treba za pocetak igre  (npr. border, supply drops, ...)
        igraci.forEach(p -> {
            p.getInventory().clear();
            p.getEnderChest().clear();
            //  p.teleport(spawnLokacije.remove(0));  // ignoriraj "player moved too fast!" u konzoli
            getServer().getScheduler().runTaskLater(this, () -> p.teleport(spawnLokacije.remove(0)), 1);
            p.setScoreboard(srca);
        });


        WorldBorder border = uhcWorld.getWorldBorder();
        WorldBorderManager borderManager = new WorldBorderManager(this, border);

        List<Map<?, ?>> borderMovements = getConfig().getMapList("border-movements");
        for (Map<?, ?> movement : borderMovements) {
            double centerX = (double) movement.get("X");
            double centerZ = (double) movement.get("Z");
            double size = (double) movement.get("size");
            int delay = (int) movement.get("delay");
            int duration = (int) movement.get("duration");
            borderManager.scheduleBorderMovement(centerX, centerZ, size, delay * 20, duration * 20);
        }

    }


}