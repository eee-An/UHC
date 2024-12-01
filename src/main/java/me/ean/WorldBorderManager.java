package me.ean;

import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void scheduleBorderMovement(double centerX, double centerZ, long ticks) {
        movementQueue.add(() -> {
            AtomicInteger remainingTicks = new AtomicInteger((int) ticks);
            Vector start = border.getCenter().toVector();
            Vector end = new Vector(centerX, 0, centerZ);
            Vector step = end.subtract(start).multiply(1.0 / ticks);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (remainingTicks.decrementAndGet() > 0) {
                        border.setCenter(border.getCenter().add(step));
                    } else {
                        border.setCenter(centerX, centerZ);
                        isResizing = false;
                        processNextMovement();
                        cancel();
                    }
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
            }.runTaskLater(plugin, seconds*20);
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