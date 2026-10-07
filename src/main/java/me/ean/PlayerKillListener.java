package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PlayerKillListener implements Listener {
    private final Main plugin;


    public PlayerKillListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        if (plugin.isUhcActive() && event.getEntity().getKiller() != null && event.getEntity().getKiller().getHealth() > 0) {
            Player killer = event.getEntity().getKiller();

            ItemStack specialTotem = SpecialItemCreator.createSpecialTotem();
            giveTotem(killer, specialTotem);

            // Increment killer's kill count
//            Map<Player, Integer> playerKills = Main.getPlayerKills();
//            playerKills.put(killer, playerKills.getOrDefault(killer, 0) + 1);
            plugin.addKill(killer);

        }

        if (plugin.isUhcActive()) {
            plugin.getPlayerStates().put(player.getUniqueId(), PlayerState.SPECTATING);
            plugin.getEliminatedPlayers().remove(player.getUniqueId());
            plugin.getEliminatedPlayers().add(player.getUniqueId());
            plugin.getLogger().info("[UHC DEBUG] DEATH player=" + player.getName()
                    + " state=SPECTATING"
                    + " currentGamemode=" + player.getGameMode()
                    + " targetGamemode=" + GameMode.SPECTATOR);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.spigot().respawn(); // Instantly respawn the player
                player.teleport(deathLocation); // Teleport to death location
                player.setGameMode(GameMode.SPECTATOR); // Set to spectator mode
                plugin.getPlayerStates().put(player.getUniqueId(), PlayerState.SPECTATING); // Update player state
                plugin.getLogger().info("[UHC DEBUG] DEATH applied player=" + player.getName()
                        + " gamemode=" + player.getGameMode()
                        + " state=" + plugin.getPlayerStates().get(player.getUniqueId()));

                List<Player> playingPlayers = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> plugin.getPlayerStates().get(p.getUniqueId()) == PlayerState.PLAYING)
                        .collect(Collectors.toList());

                plugin.getLogger().info("Playing players: " + playingPlayers.stream().map(Player::getName).collect(Collectors.joining(", ")));
                if (playingPlayers.size() == 1) {
                    Player winner = playingPlayers.get(0);

                    // posalji svima title: ime winnera
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.sendTitle(
                                plugin.getConfigValues().getWinnerAnnouncementTitle()
                                        .replace("{player}", winner.getName()),
                                plugin.getConfigValues().getWinnerAnnouncementSubtitle()
                                        .replace("{player}", winner.getName()),
                                plugin.getConfigValues().getWinnerAnnouncementFadeInTicks(),
                                plugin.getConfigValues().getWinnerAnnouncementStayTicks(),
                                plugin.getConfigValues().getWinnerAnnouncementFadeOutTicks());
                    });

                    plugin.getPlayerStates().put(winner.getUniqueId(), PlayerState.WINNER);
                    var border = plugin.getUhcWorld().getWorldBorder();
                    border.setCenter(500, -500);
                    border.setSize(20000);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getWinnerCeremonyManager().celebrateWinner();
                    }, 5 * 20L);

                }
            }, 1L);

            plugin.getParticleManager().spawnPlayerDeathParticles(event.getEntity().getLocation());
            plugin.getParticleManager().spawnLightningStrikeParticles(event.getEntity().getLocation());
            return; // If UHC is not active, do nothing
        } else if (plugin.getState() == GameState.ENDED) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.spigot().respawn(); // Instantly respawn the player
                player.teleport(deathLocation); // Teleport to death location
                player.setGameMode(GameMode.SURVIVAL); // Set to spectator mode
                plugin.getPlayerStates().put(player.getUniqueId(), PlayerState.SPECTATING); // Update player state


            }, 1L);
        }
    }




    @EventHandler
    public void onZombieKill(EntityDeathEvent event) {
        if (plugin.isUhcActive() && event.getEntity().getType() == EntityType.ZOMBIE && event.getEntity().getKiller() != null) {
            if ("§6Totem Zombie".equals(event.getEntity().getCustomName())) {
                ItemStack specialTotem = SpecialItemCreator.createSpecialTotem();
                giveTotem(event.getEntity().getKiller(), specialTotem);
            }
        }
    }

    private void giveTotem(Player player, ItemStack totem) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(totem);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}