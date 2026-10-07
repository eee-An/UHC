package me.ean;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WinnerCeremonyManager {

    private final Main plugin;

    public WinnerCeremonyManager(Main plugin) {
        this.plugin = plugin;
    }

    public void celebrateWinner() {
        broadcastResults();
        plugin.endUhc();

        Map<UUID, Player> spectators = new HashMap<>();
        Player winner = null;

        // Example: assuming PlayerState has a method isWinner()
        for (Map.Entry<UUID, PlayerState> entry : plugin.getPlayerStates().entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null) continue;

            if (entry.getValue() == PlayerState.WINNER) {
                winner = player;
            } else if (entry.getValue() == PlayerState.SPECTATING) {
                spectators.put(player.getUniqueId(), player);
            }
        }

        if (winner == null) return;

        List<Location> winnerLocs = plugin.getConfigValues().getWinnerCeremonyWinnerTeleport();
        if (winnerLocs == null || winnerLocs.isEmpty() || winnerLocs.get(0) == null) {
            plugin.getLogger().severe("Winner teleport location is not set in the config!");
            return;
        }
        Location winnerLoc = winnerLocs.get(0);
        Location spectatorLoc = plugin.getConfigValues().getWinnerCeremonySpectatorTeleport().get(0);

        if (winnerLoc == null) {
            plugin.getLogger().severe("Winner teleport location is not set in the config!");
            return;
        }
        if (spectatorLoc == null) {
            plugin.getLogger().severe("Spectator teleport location is not set in the config!");
            return;
        }

        winner.teleport(winnerLoc);
        winner.setGameMode(GameMode.SURVIVAL);
        for (Player spectator : spectators.values()) {
            spectator.teleport(spectatorLoc);
            spectator.setGameMode(GameMode.SURVIVAL);
        }
        World world = winnerLoc.getWorld();
        if (world == null) return;
        world.setDifficulty(Difficulty.PEACEFUL);

        if (!plugin.getConfigValues().isWinnerFireworksEnabled()) {
            return;
        }
        List<Color> colors = plugin.getConfigValues().getWinnerFireworkColors();

        Player finalWinner = winner;
        BukkitRunnable updater = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= plugin.getConfigValues().getWinnerFireworksWaves()) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < plugin.getConfigValues().getWinnerFireworksPerWave(); i++) {
                    double angle = Math.toRadians(i * 60);
                    double xOffset = Math.cos(angle) * 2;
                    double zOffset = Math.sin(angle) * 2;
                    Location fireworkLoc = finalWinner.getLocation().clone().add(xOffset, 1, zOffset);

                    Firework firework = (Firework) world.spawnEntity(fireworkLoc, EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                            .withColor(colors.get(i % colors.size()))
                            .with(Type.BURST)
                            .trail(true)
                            .flicker(true)
                            .build());
                    meta.setPower(plugin.getConfigValues().getWinnerFireworkPower());
                    firework.setFireworkMeta(meta);
                }
            }
        };
        plugin.registerTask(updater);
        updater.runTaskTimer(plugin, 0L, plugin.getConfigValues().getWinnerFireworksIntervalTicks());
    }

    private void broadcastResults() {
        Player winner = findWinner();
        if (winner == null) {
            return;
        }

        ConfigValues config = plugin.getConfigValues();
        Bukkit.broadcastMessage(config.getResultsHeader());
        Bukkit.broadcastMessage(config.getResultsTitle());
        Bukkit.broadcastMessage(config.getResultsWinnerLine().replace("{player}", winner.getName()));
        Bukkit.broadcastMessage("");

        if (!plugin.getTopKillers().isEmpty()) {
            int topKillersCount = Math.min(3, plugin.getTopKillers().size());
            for (int rank = 0; rank < topKillersCount; rank++) {
                TopKiller killer = plugin.getTopKillers().get(rank);
                String template = switch (rank) {
                    case 0 -> config.getResultsFirstKillerLine();
                    case 1 -> config.getResultsSecondKillerLine();
                    default -> config.getResultsThirdKillerLine();
                };
                Bukkit.broadcastMessage(formatKillerLine(template, rank + 1, killer));
            }

            if (plugin.getTopKillers().size() > topKillersCount) {
                Bukkit.broadcastMessage(config.getResultsHonorableMentionsTitle());
                for (int index = topKillersCount; index < plugin.getTopKillers().size(); index++) {
                    TopKiller killer = plugin.getTopKillers().get(index);
                    Bukkit.broadcastMessage(formatKillerLine(config.getResultsHonorableMentionLine(), 0, killer));
                }
            }
        }
        Bukkit.broadcastMessage(config.getResultsFooter());
    }

    private String formatKillerLine(String template, int rank, TopKiller killer) {
        return template
                .replace("{rank}", String.valueOf(rank))
                .replace("{player}", killer.getPlayerName())
                .replace("{kills}", String.valueOf(killer.getKills()));
    }

    private Player findWinner() {
        for (Map.Entry<UUID, PlayerState> entry : plugin.getPlayerStates().entrySet()) {
            if (entry.getValue() == PlayerState.WINNER) {
                return plugin.getServer().getPlayer(entry.getKey());
            }
        }
        return null;
    }
}