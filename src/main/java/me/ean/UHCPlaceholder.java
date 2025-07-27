package me.ean;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UHCPlaceholder extends PlaceholderExpansion {
    private final Main plugin;

    public UHCPlaceholder(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "uhc";
    }

    @Override
    public String getAuthor() {
        return "eee-An";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if(!plugin.isUhcActive()){
            if (params.equalsIgnoreCase("title"))
                return "";
        }
        else if(params.equalsIgnoreCase("title")){
            return "%animation:logo%";
        }
        else if (params.equalsIgnoreCase("line0")) {
            return "%animation:pruga%";
        }
        else if (params.equalsIgnoreCase("line1")) {
            return " ";
        }
        else if (params.equalsIgnoreCase("line2")) {
            return " §fSupply Drop:";
        } else if (params.equalsIgnoreCase("line3")) {
            if(plugin.getDropState() == DropState.WAITING) {

                return "   " + getDropCountdown();
            } else if (plugin.getDropState() == DropState.FALLING) {
                return "   §eDrop pada!";
            } else if (plugin.getDropState() == DropState.LANDED) {
                return "   §aDrop je pao!";
            } else if (plugin.getDropState() == DropState.OPENED) {
                return "   §aDrop je otvoren!";
            }
        } else if (params.equalsIgnoreCase("line4")) {
            if (plugin.getTopKillers().isEmpty()) {
                return "";
            }
            return " ";
        } else if (params.equalsIgnoreCase("line5")) {
            if (plugin.getTopKillers().isEmpty()) {
                return "";
            }
            return " §fTop Killovi:";
        } else if (params.equalsIgnoreCase("line6")) {
            var killer0 = plugin.getTopKillers().get(0);
            return killer0 != null ? "  §#FFAA001. " + killer0.getPlayer().getName() + ": " + killer0.getKills() : ""
            // TODO: do the same for line7 and line8
        } else if (params.equalsIgnoreCase("line7")) {
            if(plugin.getTopKillers().get(1) != null){
                return "  §#CECACA2. " + plugin.getTopKillers().get(1).getPlayer().getName() + ": " + plugin.getTopKillers().get(1).getKills();
            } else {
                return "";
            }
        } else if (params.equalsIgnoreCase("line8")) {
            if(plugin.getTopKillers().get(2) != null){
                return "  §#B4684D3. " + plugin.getTopKillers().get(2).getPlayer().getName() + ": " + plugin.getTopKillers().get(2).getKills();
            } else {
                return "";
            }
        } else if (params.equalsIgnoreCase("line9")) {
            return " ";
        } else if (params.equalsIgnoreCase("line10")) {
            return "%animation:pruga%";
        }

        return "";
    }

    private String getDropCountdown() {
        if (plugin.getUhcStartTime() == -1) return "Waiting...";
        long secondsSinceStart = (System.currentTimeMillis() - plugin.getUhcStartTime()) / 1000;
        List<Long> dropSeconds = plugin.getDropSeconds(); // List of drop times in seconds

        for (long dropTime : dropSeconds) {
            if (dropTime > secondsSinceStart) {
                int secondsLeft = (int) dropTime - (int) secondsSinceStart;
                return formatTime(secondsLeft);
            }
        }
        return "No more drops";
    }

    private int getKillsForPlayer(Player player) {
        // Your logic here
        return 0;
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return min + ":" + String.format("%02d", sec);
    }

    private long getSecondsSinceStart() {
        if (plugin.getUhcStartTime() == -1) return 0;
        long now = System.currentTimeMillis();
        return (now - plugin.getUhcStartTime()) / 1000;
    }

}
