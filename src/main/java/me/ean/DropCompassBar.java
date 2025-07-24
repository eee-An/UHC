package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class DropCompassBar {
    private final Map<Player, BossBar> playerBossBars = new HashMap<>();
    private final Location dropLocation;
    private BukkitRunnable updater;
    private int ticksElapsed = 0;
    private static final int SHOW_DROP_MESSAGE_TICKS = 140;

    private boolean showOpenedMessage = false;
    private int openedMessageTicks = 0;
    private String openedMessage = "";

    public DropCompassBar(Location dropLocation) {
        this.dropLocation = dropLocation.clone();
        for (Player player : Bukkit.getOnlinePlayers()) {
            createBossBar(player);
        }
        startUpdating();
    }

    private void createBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar("Compass", BarColor.BLUE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        playerBossBars.put(player, bossBar);
    }

    private void startUpdating() {
        updater = new BukkitRunnable() {
            @Override
            public void run() {
                ticksElapsed += 5;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerBossBars.computeIfAbsent(player, p -> {
                        createBossBar(p);
                        return playerBossBars.get(p);
                    });
                    BossBar bossBar = playerBossBars.get(player);

                    if (ticksElapsed <= SHOW_DROP_MESSAGE_TICKS) {
                        bossBar.setTitle(Main.getInstance().getConfigValues().getSupplyDropLandingMessage()
                                .replace("{x}", String.valueOf(dropLocation.getBlockX()))
                                .replace("{y}", String.valueOf(dropLocation.getBlockY()))
                                .replace("{z}", String.valueOf(dropLocation.getBlockZ())));
                    } else {
                        String direction = getPreciseCompassDirection(player, dropLocation);
                        bossBar.setTitle(direction);
                    }

                    if (showOpenedMessage) {
                        bossBar.setTitle(openedMessage);
                        openedMessageTicks -= 5;
                        if (openedMessageTicks <= 0) {
                            showOpenedMessage = false;
                        }
                    }
                }
                // Remove boss bars for players who left
                playerBossBars.keySet().removeIf(player -> !player.isOnline());
            }
        };
        updater.runTaskTimer(Main.getInstance(), 0, 5);
    }

    public void remove() {
        if (updater != null) {
            updater.cancel();
        }
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.removeAll();
        }
        playerBossBars.clear();
    }

    private String getPreciseCompassDirection(Player player, Location target) {
        double dx = target.getX() - player.getLocation().getX();
        double dz = target.getZ() - player.getLocation().getZ();
        double angleToTarget = Math.toDegrees(Math.atan2(dz, dx));
        double playerYaw = player.getLocation().getYaw() + 90;
        angleToTarget = ((angleToTarget + 360) % 360);
        playerYaw = ((playerYaw + 360) % 360);
        double diff = angleToTarget - playerYaw;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;
        int segments = 35;
        int center = segments / 2;
        int markerPos = (int) Math.round(center + (diff / 180.0) * center);
        markerPos = Math.max(0, Math.min(segments - 1, markerPos));
        StringBuilder compass = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            if (i == markerPos) {
                compass.append("§3↑");
            } else {
                compass.append(" ");
            }
        }
        return compass.toString();
    }

    public void setTitle(String title) {
        this.openedMessage = title;
        this.showOpenedMessage = true;
        this.openedMessageTicks = SHOW_DROP_MESSAGE_TICKS;
    }
}