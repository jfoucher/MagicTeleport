package fr.sixpixels.magicteleport;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

public class MagicTeleportListener implements Listener {
    public MagicTeleport plugin;
    public MagicTeleportListener(MagicTeleport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        Block b = e.getBlock();

        if (b.getType().equals(Material.SHROOMLIGHT)
                && !b.getMetadata("player_id").contains(new FixedMetadataValue(this.plugin, p.getUniqueId()))
        ){
            e.setCancelled(true);
        }

        if (b.getType().equals(Material.SHROOMLIGHT)
                && !b.getMetadata("player_id").contains(new FixedMetadataValue(this.plugin, p.getUniqueId()))
        ){
            ItemStack sp = getItemStack();

            p.getWorld().dropItem(b.getLocation(), sp);

            // Remove item from config
            this.plugin.removeBlock(p, b.getLocation());
        }
    }

    private static ItemStack getItemStack() {
        ItemStack sp = new ItemStack(Material.SHROOMLIGHT);
        ItemMeta meta = sp.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Bloc de téléportation");
            meta.setLocalizedName("teleport_block");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Placez ce bloc n'importe où");
            lore.add("pour vous y téléporter plus tard");
            meta.setLore(lore);
        }

        sp.setItemMeta(meta);
        return sp;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {

        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        ItemMeta meta = tool.getItemMeta();

        Block block = e.getBlock();

        if (meta == null) {
            return;
        }

        if (meta.getLocalizedName().equals("teleport_block")) {
            block.setMetadata("player_id", new FixedMetadataValue(this.plugin, p.getUniqueId()));
            // Save block location in config
            this.plugin.saveBlock(p, block.getLocation());
        }
    }
}
