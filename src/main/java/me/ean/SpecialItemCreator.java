package me.ean;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class SpecialItemCreator {
    public static ItemStack createSpecialTotem() {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            meta.setLore(Collections.singletonList("Special Item"));
            totem.setItemMeta(meta);
        }
        return totem;
    }
}