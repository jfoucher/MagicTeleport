package fr.sixpixels.magicteleport;

import org.bukkit.Bukkit;
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
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;

public class MagicTeleportListener implements Listener {
    public MagicTeleport plugin;
    public MagicTeleportListener(MagicTeleport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();

        if (b.getType().equals(Material.SHROOMLIGHT)
                && !b.getMetadata("player_id").isEmpty()
        ){
            Player p = e.getPlayer();
            //check if the right player
            List<MetadataValue> ms = b.getMetadata("player_id");
            boolean isplayer = false;
            for (MetadataValue m: ms) {
                if (m.getOwningPlugin() == this.plugin && m.asString().equals(p.getUniqueId().toString())) {
                    isplayer = true;
                }
            }
            String display_name = null;
            List<MetadataValue> ns = b.getMetadata("display_name");
            for (MetadataValue n: ns) {
                if (n.getOwningPlugin() == this.plugin) {
                    display_name = n.asString();
                }
            }

            if (isplayer) {
                ItemStack sp = TeleportBlock.getBlock(display_name);

                p.getWorld().dropItem(b.getLocation(), sp);
                e.setDropItems(false);
                // Remove item from config
                this.plugin.removeBlock(p, b.getLocation());
            } else {
                e.setCancelled(true);
            }

        }
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
            block.setMetadata("player_id", new FixedMetadataValue(this.plugin, p.getUniqueId().toString()));
            block.setMetadata("display_name", new FixedMetadataValue(this.plugin, meta.getDisplayName()));
            // Save block location in config
            this.plugin.saveBlock(p, block.getLocation(), meta.getDisplayName());
        }
    }
}
