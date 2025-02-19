package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerKillListener implements Listener {
    private final Main plugin;

    public PlayerKillListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.isUhcActive() && event.getEntity().getKiller() != null) {
            ItemStack specialTotem = SpecialItemCreator.createSpecialTotem();
            event.getEntity().getKiller().getInventory().addItem(specialTotem);
        }
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