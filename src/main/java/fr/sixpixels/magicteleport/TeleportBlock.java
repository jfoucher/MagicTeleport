package fr.sixpixels.magicteleport;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class TeleportBlock {

    public static ItemStack getBlock() {
        ItemStack block = new ItemStack(Material.SHROOMLIGHT);
        ItemMeta meta = block.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("Bloc de téléportation");
            meta.setLocalizedName("teleport_block");

            ArrayList<String> lore = new ArrayList<>();
            lore.add("Placez ce bloc n'importe où");
            lore.add("pour vous y téléporter plus tard");
            meta.setLore(lore);

            block.setItemMeta(meta);
        }

        return block;
    }

}
