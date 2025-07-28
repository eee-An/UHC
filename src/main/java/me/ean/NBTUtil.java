package me.ean;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public class NBTUtil {
    private static final NamespacedKey key = new NamespacedKey("uhc", "specialitem");

    public static boolean hasCustomTag(ItemStack item) {
        var meta = item == null ? null : item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(key, PersistentDataType.STRING);
    }

    public static ItemStack addCustomTag(ItemStack item, String value) {
        var meta = item == null ? null : item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, value.toLowerCase());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack removeCustomTag(ItemStack item) {
        var meta = item == null ? null : item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(key);
        item.setItemMeta(meta);
        return item;
    }
}