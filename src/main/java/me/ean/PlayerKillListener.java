package me.ean;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerKillListener implements Listener {
    private final Main plugin;


    public PlayerKillListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.isUhcActive() && event.getEntity().getKiller() != null && event.getEntity().getKiller().getHealth() > 0) {
            Player killer = event.getEntity().getKiller();

            ItemStack specialTotem = SpecialItemCreator.createSpecialTotem();
            event.getEntity().getKiller().getInventory().addItem(specialTotem);

            // Increment killer's kill count
            Map<Player, Integer> playerKills = Main.getPlayerKills();
            playerKills.put(killer, playerKills.getOrDefault(killer, 0) + 1);
            Main.addKill(killer);

        }

        Main.getInstance().getParticleManager().spawnPlayerDeathParticles(event.getEntity().getLocation());
        Main.getInstance().getParticleManager().spawnLightningStrikeParticles(event.getEntity().getLocation());
    }

    @EventHandler
    public void onZombieKill(EntityDeathEvent event) {
        if (plugin.isUhcActive() && event.getEntity().getType() == EntityType.ZOMBIE && event.getEntity().getKiller() != null) {
            if ("§6Totem Zombie".equals(event.getEntity().getCustomName())) {
                ItemStack specialTotem = SpecialItemCreator.createSpecialTotem();
                event.getEntity().getKiller().getInventory().addItem(specialTotem);
            }
        }
    }
}