package me.ean;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndUHCCommand implements CommandExecutor {
    private final Main plugin;

    public EndUHCCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("enduhc")) {
            plugin.endUHC();
            sender.sendMessage("UHC has been ended. All restrictions are lifted.");
            return true;
        }
        return false;
    }
}