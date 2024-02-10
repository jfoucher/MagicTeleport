package fr.sixpixels.magicteleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class MagicTeleport extends JavaPlugin {

    private YamlConfiguration language;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.loadLanguageFile();

        Bukkit.getServer().getPluginManager().registerEvents(new MagicTeleportListener(this), this);

        // TODO get all blocks for all players from config
        // Set block metadata for each block

    }

    public void saveBlock(Player p, Location l) {
        ConfigurationSection all_player_blocks = getConfig().getConfigurationSection("player_blocks");
        if (all_player_blocks == null) {
            all_player_blocks = getConfig().createSection("player_blocks");
        }

        ConfigurationSection player_blocks = all_player_blocks.getConfigurationSection(p.getName());
        if (player_blocks == null) {
            player_blocks = all_player_blocks.createSection(p.getName());
        }

        // Get all existing block keys
        Set<String> block_keys = player_blocks.getKeys(false);

        int block_ref = 0;
        for(String ref: block_keys) {
            int r = Integer.parseInt(ref, 10);
            if (r > block_ref) {
                block_ref = r;
            }
        }

        block_ref += 1;

        ConfigurationSection block = player_blocks.createSection(String.format("%d", block_ref));

        block.set("world", p.getWorld().getName());
        block.set("location", l);
        saveConfig();

        String ce = this.getLanguage().getString("TELEPORT_BLOCK_PLACED");
        if (ce == null) {
            ce = "Teleport block placed";
        }

        p.sendMessage(ce);
    }


    public void removeBlock(Player p, Location l) {
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

            if (bl == l) {
                //remove this config section
                getConfig().set("player_blocks." + p.getName() + "."+ref, null);
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
}
