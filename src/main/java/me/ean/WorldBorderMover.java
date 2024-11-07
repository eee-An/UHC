package me.ean;

import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class WorldBorderMover implements CommandExecutor {
    private final Main plugin;

    public WorldBorderMover(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player) || (args.length != 3))
            return true;

        double targetX = Double.parseDouble(args[0]);
        double targetZ = Double.parseDouble(args[1]);
        int numTicks = Integer.parseUnsignedInt(args[2]) * 20;

        AtomicInteger i = new AtomicInteger(numTicks);
        WorldBorder border = player.getWorld().getWorldBorder();

        Vector vecStart = border.getCenter().toVector();
        Vector vecEnd = new Vector(targetX, 0, targetZ);
        Vector vec = vecEnd.subtract(vecStart).multiply(1/(float) numTicks);


        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (i.decrementAndGet() /* --i */ > 0)
                border.setCenter(border.getCenter().add(vec));
        }, 0, 1);

        plugin.getServer().getScheduler().runTaskLater(plugin, task::cancel, numTicks+1);
        return true;
    }
}
