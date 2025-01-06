package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class WorldBorderManager {
    private final Main plugin;
    private final WorldBorder border;
    private boolean isResizing;
    private final Queue<Runnable> movementQueue = new LinkedList<>();

    public WorldBorderManager(Main plugin, WorldBorder border) {
        this.plugin = plugin;
        this.border = border;
        this.isResizing = false;
    }

    public void scheduleBorderMovement(double centerX, double centerZ, double size, int delay, int duration) {
        movementQueue.add(() -> {

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    int[] warningTimes = plugin.getConfig().getIntegerList("border-movement-start-warning-times").stream().mapToInt(i -> i).toArray();
                    long ticksLeft = (delay - ticks);

                    for (int warningTime : warningTimes) {
                        if (ticksLeft == (long) warningTime * 20) {
                            Bukkit.broadcastMessage(plugin.getConfig().getString("border-movement-start-warning-message").replace("{seconds}", String.valueOf(warningTime)));
                        }
                    }
                    if (ticks == delay) {
                        Bukkit.broadcastMessage(plugin.getConfig().getString("border-movement-start-message"));
                        this.cancel();
                    }ticks++;
                }
            }.runTaskTimer(plugin, 0, 1);

            Vector start = border.getCenter().toVector();
            Vector end = new Vector(centerX, 0, centerZ);
            Vector step = end.subtract(start).multiply(1.0 / (duration + 1));
            Location currentLocation = border.getCenter();

            new BukkitRunnable() {
                int currentTick = 0;
                final int lastTick = delay + duration;

                @Override
                public void run() {
                    if (currentTick == lastTick) {
                        border.setCenter(centerX, centerZ);
                        border.setSize(size);
                        isResizing = false;
                        processNextMovement();
                        cancel();
                        return;
                    } else if (currentTick >= delay) {
                        if (currentTick == delay) {
                            border.setSize(size, duration / 20);
                        }
                        border.setCenter(currentLocation.add(step));
                    }
                    currentTick++;
                }
            }.runTaskTimer(plugin, 0, 1);
        });

        if (!isResizing) {
            processNextMovement();
        }
    }

    public void scheduleBorderResize(double size, long seconds) {
        movementQueue.add(() -> {
            border.setSize(size, seconds);
            new BukkitRunnable() {
                @Override
                public void run() {
                    isResizing = false;
                    processNextMovement();
                }
            }.runTaskLater(plugin, seconds * 20);
        });

        if (!isResizing) {
            processNextMovement();
        }
    }

    private void processNextMovement() {
        Runnable nextMovement = movementQueue.poll();
        if (nextMovement != null) {
            isResizing = true;
            nextMovement.run();
        }
    }
}