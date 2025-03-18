package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NBTUtil {
    private static NamespacedKey key;

    static {
        try {
            key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("UHC"), "SpecialItem");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasCustomTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(key, PersistentDataType.STRING);
    }

    public static ItemStack addCustomTag(ItemStack item, String value) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack removeCustomTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(key);
        item.setItemMeta(meta);
        return item;
    }
}