package me.ean;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class WorldBorderManager {
    private final Main plugin;
    private final WorldBorder border;
    @Getter
    private final ParticleManager particleManager;
    private boolean isResizing;
    private final Queue<Runnable> movementQueue = new LinkedList<>();
    private BukkitTask currentTask;
    private BukkitTask particleTask;

    public WorldBorderManager(Main plugin, WorldBorder border) {
        this.plugin = plugin;
        this.border = border;
        this.particleManager = new ParticleManager(plugin); // Initialize ParticleManager
        this.isResizing = false;
    }

    public void scheduleBorderMovement(double centerX, double centerZ, double size, int delay, int duration) {
        movementQueue.add(() -> {
            currentTask = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    int[] warningTimes = plugin.getConfigValues().getBorderMovementStartWarningTimes().stream().mapToInt(i -> i).toArray();
                    long ticksLeft = (delay - ticks);

                    for (int warningTime : warningTimes) {
                        if (ticksLeft == (long) warningTime * 20) {
                            Bukkit.broadcastMessage(plugin.getConfigValues().getBorderMovementStartWarningMessage().replace("{seconds}", String.valueOf(warningTime)));
                        }
                    }
                    if (ticks == delay) {
                        Bukkit.broadcastMessage(plugin.getConfigValues().getBorderMovementStartMessage());
                        this.cancel();
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0, 1);

            Vector start = border.getCenter().toVector();
            Vector end = new Vector(centerX, 0, centerZ);
            Vector step = end.subtract(start).multiply(1.0 / (duration + 1));
            Location currentLocation = border.getCenter();

//            startBorderCenterParticles(); // Start particles when border movement begins
            currentTask = new BukkitRunnable() {
                int currentTick = 0;
                final int lastTick = delay + duration;

                @Override
                public void run() {
                    if(!Main.getInstance().isUhcActive()){
                        stopBorderCenterParticles(); // Stop particles if UHC is not active
                        cancel();
                        return;
                    }
                    if (currentTick == lastTick) {
                        border.setCenter(centerX, centerZ);
                        border.setSize(size);
                        isResizing = false;
                        stopBorderCenterParticles(); // Stop particles when movement ends
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
            currentTask = new BukkitRunnable() {
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

    public void clearScheduledMovements() {
        movementQueue.clear();
        isResizing = false;
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    private void processNextMovement() {
        Runnable nextMovement = movementQueue.poll();
        if (nextMovement != null) {
            isResizing = true;
            nextMovement.run();
        }
    }

    public void startBorderCenterParticles() {
        if (particleTask != null && !particleTask.isCancelled()) {
            return; // Prevent multiple tasks from running
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                World world = border.getWorld();
                if (world != null) {
                    Location center = border.getCenter();
                    particleManager.spawnBorderCenterParticles(world, center); // Use ParticleManager
                }
            }
        }.runTaskTimer(plugin, 0, 5); // Runs every second (20 ticks)
    }

    public void stopBorderCenterParticles() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
}