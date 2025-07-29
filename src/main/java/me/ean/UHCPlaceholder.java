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
        else if (params.equalsIgnoreCase("line1") || params.equalsIgnoreCase("razmak")) {
            return " ";
        }
        else if (params.equalsIgnoreCase("stoperica")) {
            long totalSeconds = getSecondsSinceStart();
            int totalMinutes = (int) (totalSeconds / 60);
            int seconds = (int) (totalSeconds % 60);
            int totalHours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            return " " + String.format("§fVreme: §a%d:%02d:%02d", totalHours, minutes, seconds);
        }
        else if (params.equalsIgnoreCase("line2")) {
            return " §fSupply Drop:";
        } else if (params.equalsIgnoreCase("line3")) {
            if (plugin.getDrops().isEmpty()) {
                return "   " + getDropCountdown();
            }
            SupplyDrop lastDrop = plugin.getDrops().get(plugin.getDrops().size()-1);
            switch (lastDrop.getDropState()) {
                case WAITING:
                    return "   " + getDropCountdown();
                case FALLING:
                    return "   §eDrop pada!";
                case LANDED:
                    return "   §aDrop je pao!";
                case OPENED:
                    return "   §aDrop je otvoren!";
                case REMOVED:
                    int ukupnoDropova = 0;
                    for (var SA : plugin.getConfigValues().getScheduledActions()) {
                        if (SA.getAction().equalsIgnoreCase("supplydrop")) {
                            ukupnoDropova++;
                        }
                    }
                    if (plugin.getDrops().size() == ukupnoDropova) {
                        return "   §aSvi su pali!";
                    }
                    return "   " + getDropCountdown();

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
            return killer0 != null ? "  §#FFAA001. " + killer0.getPlayerName() + ": " + killer0.getKills() : "";
        } else if (params.equalsIgnoreCase("line7")) {
            var killer1 = plugin.getTopKillers().get(1);
            return killer1 != null ? "  §#CECACA2. " + killer1.getPlayerName() + ": " + killer1.getKills() : "";
        } else if (params.equalsIgnoreCase("line8")) {
            var killer2 = plugin.getTopKillers().get(2);
            return killer2 != null ? "  §#B4684D3. " + killer2.getPlayerName() + ": " + killer2.getKills() : "";
        } else if (params.equalsIgnoreCase("line9")) {
            return " ";
        } else if (params.equalsIgnoreCase("line10")) {
            return "%animation:pruga%";
        }

        return "";
    }

    private String getDropCountdown() {
        if (plugin.getUhcStartTime() == -1) return "Waiting...";
        long secondsSinceStart = getSecondsSinceStart();
        List<Long> dropSeconds = plugin.getDropSeconds(); // List of drop times in seconds

        for (long dropTime : dropSeconds) {
            if (dropTime > secondsSinceStart) {
                int secondsLeft = (int) dropTime - (int) secondsSinceStart;
                return formatTime(secondsLeft);
            }
        }
        return "Nema više dropova!";
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
