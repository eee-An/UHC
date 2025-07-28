package me.ean;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;


//TODO: Dodati mogucnost da se dodaju itemi kroz config file
public class SpecialItemCreator {
    public static ItemStack createSpecialTotem() {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            meta.setLore(Collections.singletonList("Special UHC Item"));
            totem.setItemMeta(meta);
        }
        return NBTUtil.addCustomTag(totem, "specialitem");
    }
}