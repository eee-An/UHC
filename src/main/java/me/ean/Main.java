package me.ean;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
    @Getter
    private static Main instance;
    private GameState state = GameState.WAITING;
    private List<Player> igraci = new ArrayList<>();
    private World uhcWorld;
    private List<Location> spawnLokacije = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        uhcWorld = Bukkit.getWorld("world");
        spawnLokacije.add(new Location(uhcWorld, 0, 98, 0));
        spawnLokacije.add(new Location(uhcWorld, 1, 98, 1));
        spawnLokacije.add(new Location(uhcWorld, -1, 98, -1));
        spawnLokacije.add(new Location(uhcWorld, 2, 98, 2));
        spawnLokacije.add(new Location(uhcWorld, -2, 98, -2));

        this.getCommand("startuhc").setExecutor(this);
        this.getCommand("resetstate").setExecutor(this);
        this.getCommand("bacisupplydrop").setExecutor(this);
        Objects.requireNonNull(this.getCommand("testborder")).setExecutor(new WorldBorderMover(this));
        Bukkit.getPluginManager().registerEvents(this, this);
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
        state = GameState.PLAYING;

        Collections.shuffle(spawnLokacije);

        Scoreboard srca = Bukkit.getScoreboardManager().getNewScoreboard();
        srca.registerNewObjective("hp", Criteria.HEALTH, "srca", RenderType.HEARTS);

        // TODO: teleportiraj igrace, odradi sve sto treba
        // TODO: Treba pobrisati ec!
        igraci.forEach(p -> {
            p.getInventory().clear();
            p.teleport(spawnLokacije.remove(0));  // ignoriraj "player moved too fast!" u konzoli
            p.setScoreboard(srca);
        });


        WorldBorder border = uhcWorld.getWorldBorder();
        WorldBorderManager borderManager = new WorldBorderManager(this, border);
        border.setCenter(0.0, 0.0);
        border.setSize(50.0);
        borderManager.scheduleBorderResize(10.0, 10);
        borderManager.scheduleBorderMovement(0.0, 10.0, 100);
        borderManager.scheduleBorderMovement(10.0, 10.0, 100);
        borderManager.scheduleBorderMovement(10.0, 0.0, 100);
        borderManager.scheduleBorderMovement(10.0, -10.0, 100);
        borderManager.scheduleBorderMovement(0.0, -10.0, 100);
        borderManager.scheduleBorderMovement(-10.0, -10.0, 100);
        borderManager.scheduleBorderMovement(-10.0, 0.0, 100);
        borderManager.scheduleBorderMovement(-10.0, 10.0, 100);
        borderManager.scheduleBorderMovement(0, 10.0, 100);
        borderManager.scheduleBorderMovement(0, 0.0, 100);


    }
}