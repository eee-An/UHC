package me.ean;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemPickupListener implements Listener {
    private final Main plugin;

    public ItemPickupListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isUhcActive()) {
            return;
        }

        List<String> bannedItems = plugin.getConfig().getStringList("banned-items");
        String removalMessage = plugin.getConfig().getString("banned-item-removal-message");
        ItemStack item = event.getCurrentItem();
        if (item != null && bannedItems.contains(item.getType().name())) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getLore() != null && meta.getLore().contains("Special Item")) {
                return; // Do not remove special items
            }
            event.setCurrentItem(null);
            assert removalMessage != null;
            event.getWhoClicked().sendMessage(String.format(removalMessage, item.getType().name()));
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.isUhcActive()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        List<String> bannedItems = plugin.getConfig().getStringList("banned-items");
        String removalMessage = plugin.getConfig().getString("banned-item-removal-message");
        ItemStack item = event.getItem().getItemStack();
        if (item != null && bannedItems.contains(item.getType().name())) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getLore() != null && meta.getLore().contains("Special Item")) {
                return; // Do not remove special items
            }
            event.getItem().remove();
            player.getInventory().removeItem(item);
            assert removalMessage != null;
            player.sendMessage(String.format(removalMessage, item.getType().name()));
        }
    }
}