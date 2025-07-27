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
        world.setDifficulty(Difficulty.PEACEFUL);
        if (world == null) return;

        Color[] colors = {Color.YELLOW, Color.RED, Color.ORANGE};

        Player finalWinner = winner;
        BukkitRunnable updater = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= 10) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < 6; i++) {
                    double angle = Math.toRadians(i * 60);
                    double xOffset = Math.cos(angle) * 2;
                    double zOffset = Math.sin(angle) * 2;
                    Location fireworkLoc = finalWinner.getLocation().clone().add(xOffset, 1, zOffset);

                    Firework firework = (Firework) world.spawnEntity(fireworkLoc, EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                            .withColor(colors[i % 3])
                            .with(Type.BURST)
                            .trail(true)
                            .flicker(true)
                            .build());
                    meta.setPower(0);
                    firework.setFireworkMeta(meta);
                }
            }
        };
        plugin.registerTask(updater);
        updater.runTaskTimer(plugin, 0L, 40L);
    }
}