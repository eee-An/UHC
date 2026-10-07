package me.ean;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("unused")
public class ItemPickupListener implements Listener {
    private final Main plugin;
    private final List<String> bannedItems;
    private final String removalMessage;

    public ItemPickupListener(Main plugin) {
        this.plugin = plugin;
        bannedItems = plugin.getConfigValues().getBannedItems();
        removalMessage = plugin.getConfigValues().getBannedItemRemovealMessage();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isUhcActive()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (isBannedItem(currentItem) || isBannedItem(cursor)) {
            event.setCancelled(true);
            if (isBannedItem(currentItem)) {
                sendRemovalMessage(player, currentItem);
            }
            if (isBannedItem(cursor)) {
                sendRemovalMessage(player, cursor);
            }
            event.setCurrentItem(null);
            event.setCursor(null);
        }
        removeBannedItems(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!plugin.isUhcActive() || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (isBannedItem(event.getOldCursor()) || isBannedItem(event.getCursor())) {
            event.setCancelled(true);
            event.setCursor(null);
        }
        removeBannedItems(player);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.isUhcActive()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        if (item != null && bannedItems.contains(item.getType().name())) {
            if (NBTUtil.hasCustomTag(item)) {
                return; // Do not remove special items
            }
            event.setCancelled(true);
            event.getItem().remove();
            if (removalMessage != null) {
                player.sendMessage(String.format(removalMessage, item.getType().name()));
            }
        }
    }

    private boolean isBannedItem(ItemStack item) {
        return item != null
                && bannedItems.contains(item.getType().name())
                && !NBTUtil.hasCustomTag(item);
    }

    private void removeBannedItems(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (!isBannedItem(item)) {
                continue;
            }
            player.getInventory().setItem(slot, null);
            sendRemovalMessage(player, item);
        }
    }

    private void sendRemovalMessage(Player player, ItemStack item) {
        if (removalMessage != null) {
            player.sendMessage(String.format(removalMessage, item.getType().name()));
        }
    }
}