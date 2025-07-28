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
            BukkitRunnable warningRunnable = new BukkitRunnable() {
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
            };
            currentTask = warningRunnable.runTaskTimer(plugin, 0, 1);
            plugin.registerTask(warningRunnable);

            Vector start = border.getCenter().toVector();
            Vector end = new Vector(centerX, 0, centerZ);
            Vector step = end.subtract(start).multiply(1.0 / (duration + 1));
            Location currentLocation = border.getCenter();

            BukkitRunnable moveRunnable = new BukkitRunnable() {
                int currentTick = 0;
                final int lastTick = delay + duration;
                @Override
                public void run() {
                    if(!plugin.isUhcActive()){
                        stopBorderCenterParticles();
                        cancel();
                        return;
                    }
                    if (currentTick == lastTick) {
                        border.setCenter(centerX, centerZ);
                        border.setSize(size);
                        isResizing = false;
//                        stopBorderCenterParticles();
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
            };
            currentTask = moveRunnable.runTaskTimer(plugin, 0, 1);
            plugin.registerTask(moveRunnable);
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
            plugin.registerTask((BukkitRunnable) currentTask);
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
            return;
        }
        BukkitRunnable particleRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                World world = border.getWorld();
//                plugin.getLogger().warning("Spawning particles at world border center: " + border.getCenter() + " in world: " + world);
                if (world != null) {
                    Location center = border.getCenter();
                    particleManager.spawnBorderCenterParticles(world, center);
                }
            }
        };
        particleTask = particleRunnable.runTaskTimer(plugin, 0, 5);
        plugin.registerTask(particleRunnable);
    }

    public void stopBorderCenterParticles() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
}