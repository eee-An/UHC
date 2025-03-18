package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndUHCCommand implements CommandExecutor {
    private final Main plugin;
    private final WorldBorderManager borderManager;

    public EndUHCCommand(Main plugin, WorldBorderManager borderManager) {
        this.plugin = plugin;
        this.borderManager = borderManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("enduhc")) {
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
        }
        return false;
    }

    private void endUHC() {
        // Reset UHC-specific conditions
        plugin.setUhcActive(false);

        // Clear scheduled border movements and reset the border
        borderManager.clearScheduledMovements();
        WorldBorder border = Bukkit.getWorld("world").getWorldBorder();
        border.setSize(75); // Set the border to the initial size (e.g., 75 blocks)
        border.setCenter(0, 0); // Set the border center to the initial position (e.g., 0, 0)
    }
}