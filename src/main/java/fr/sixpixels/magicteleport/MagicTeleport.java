package fr.sixpixels.magicteleport;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MagicTeleport extends JavaPlugin {

    private YamlConfiguration language;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.loadLanguageFile();

        Bukkit.getServer().getPluginManager().registerEvents(new MagicTeleportListener(this), this);
        PluginCommand cmd = this.getCommand("magicteleport");
        if (cmd != null) {
            cmd.setExecutor(new MagicTeleportCommand(this));
        }
        // TODO get all blocks for all players from config
        // Set block metadata for each block

        restoreTeleportBlocks();
    }

    public void restoreTeleportBlocks() {
        this.reloadConfig();
        ConfigurationSection all_player_blocks = getConfig().getConfigurationSection("player_blocks");
        if (all_player_blocks != null) {
            Set<String> players = all_player_blocks.getKeys(false);

            for(String name: players) {
                ConfigurationSection player_blocks = all_player_blocks.getConfigurationSection(name);
                if (player_blocks != null) {
                    Set<String> block_keys = player_blocks.getKeys(false);
                    for(String ref: block_keys) {
                        ConfigurationSection block = player_blocks.getConfigurationSection(ref);
                        if (block != null) {
                            String world = block.getString("world");
                            Location loc = block.getLocation("location");
                            String display_name = block.getString("display_name");

                            Bukkit.getLogger().warning("[MAgicTeleport] block display name: " + display_name);
                            if (loc != null) {
                                if (world == null) {
                                    World w = loc.getWorld();
                                    if (w != null) {
                                        world = w.getName();
                                    }
                                }
                                if (world != null) {
                                    World locWorld = Bukkit.getServer().getWorld(world);
                                    if (locWorld != null) {
                                        Block b = locWorld.getBlockAt(loc);
                                        if (b.getType() == Material.SHROOMLIGHT) {
                                            OfflinePlayer p = Bukkit.getOfflinePlayer(name);

                                            b.setMetadata("player_id", new FixedMetadataValue(this, p.getUniqueId().toString()));
                                            if (display_name != null) {
                                                b.setMetadata("display_name", new FixedMetadataValue(this, display_name));

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveBlock(Player p, Location l, String displayName) {
        reloadConfig();
        ConfigurationSection all_player_blocks = getConfig().getConfigurationSection("player_blocks");
        if (all_player_blocks == null) {
            all_player_blocks = getConfig().createSection("player_blocks");
        }

        ConfigurationSection player_blocks = all_player_blocks.getConfigurationSection(p.getName());
        if (player_blocks == null) {
            player_blocks = all_player_blocks.createSection(p.getName());
        }

        // Get all existing block keys
        List<Integer> block_keys = player_blocks.getKeys(false)
                .stream()
                .map((s) -> Integer.parseInt(s, 10))
                .collect(Collectors.toList());
        int block_ref = 1;

        while (true) {
            if (!block_keys.contains(block_ref)) {
                break;
            }
        }

        ConfigurationSection block = player_blocks.createSection(String.format("%d", block_ref));

        block.set("world", p.getWorld().getName());
        block.set("location", l);
        block.set("display_name", displayName);
        saveConfig();

        String ce = this.getLanguage().getString("TELEPORT_BLOCK_PLACED");
        if (ce == null) {
            ce = "Teleport block placed";
        }
        Audience a = (Audience) p;
        a.sendMessage(MiniMessage.miniMessage().deserialize(ce));
    }

    public boolean teleport(Player p, String blockRef) {
        Bukkit.getLogger().info("[MagicTeleport] teleporting " + p.getName() + " to  block " + blockRef);
        Location loc = getConfig().getLocation("player_blocks." + p.getName() + "." + blockRef + ".location");
        if (loc != null) {
            loc.add(0.5, 1, 0.5);
            p.teleport(loc);
            return true;
        }
        String m = getLanguage().getString("TELEPORT_ERROR");
        if (m == null) {
            m = "Could not teleport you to this block. This may be a bug";
        }
        p.sendMessage(m);
        return false;
    }


    public void removeBlock(Player p, Location l) {
        Bukkit.getLogger().info("removing block at location " + l);
        reloadConfig();
        ConfigurationSection all_player_blocks = getConfig().getConfigurationSection("player_blocks");
        if (all_player_blocks == null) {
            all_player_blocks = getConfig().createSection("player_blocks");
        }

        ConfigurationSection player_blocks = all_player_blocks.getConfigurationSection(p.getName());
        if (player_blocks == null) {
            Bukkit.getLogger().warning("Player tried to remove block, but did not have any blocks");
            return;
        }

        // Get all existing block keys
        Set<String> block_keys = player_blocks.getKeys(false);

        for(String ref: block_keys) {
            ConfigurationSection block = player_blocks.getConfigurationSection(ref);
            if (block == null) {
                Bukkit.getLogger().warning("Player tried to remove block, but did not have any blocks");
                return;
            }

            Location bl = block.getLocation("location");
            if (bl != null) {
                Bukkit.getLogger().info("config block location " + bl);
                if (bl.getBlockX() == l.getBlockX() && bl.getBlockY() == l.getBlockY() && bl.getBlockX() == l.getBlockX()) {
                    //remove this config section
                    getConfig().set("player_blocks." + p.getName() + "."+ref, null);
                    Bukkit.getLogger().info("Block removed from config");

                }
            }

        }

        saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Configuration getLanguage() {
        if (this.language == null) {
            this.loadLanguageFile();
        }
        return this.language;
    }

    private void loadLanguageFile() {
        String lang = getConfig().getString("language");

        if (lang == null) {
            Bukkit.getLogger().warning("[GPS] Please set a language in config.yml");
            lang = "en_US";
        }

        File langFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            saveResource("lang/" + lang + ".yml", false);

        }

        this.language = new YamlConfiguration();
        try {
            this.language.load(langFile);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().warning("Could not load language file for language " + lang);
        }
    }

    public static int getAmount(Player player) {
        String permissionPrefix = "magicteleport.quantity.";
        int max = 1;
        for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions()) {
            String permission = attachmentInfo.getPermission();

            if (permission.startsWith(permissionPrefix)) {
                Bukkit.getLogger().info("perm check " + permission);
                int qty = Integer.parseInt(permission.substring(permission.lastIndexOf(".") + 1));
                if (qty > max) {
                    max = qty;
                }
            }
        }

        return max;
    }
}
