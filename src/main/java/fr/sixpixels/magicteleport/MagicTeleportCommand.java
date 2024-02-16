package fr.sixpixels.magicteleport;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MagicTeleportCommand  implements CommandExecutor {

    MagicTeleport plugin;
    public MagicTeleportCommand(MagicTeleport plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, String s, String[] args) {
        if (s.equalsIgnoreCase("magicteleport") || s.equalsIgnoreCase("mtp")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if (commandSender instanceof ConsoleCommandSender) {
                        return this.giveItem(commandSender, args);
                    }
                    if (commandSender instanceof Player && commandSender.hasPermission("magicteleport.admin")) {
                        return this.giveItem(commandSender, args);
                    }

                    String permError = this.plugin.getLanguage().getString("NO_PERMISSION_MESSAGE");

                    if (permError == null){
                        permError = "&cYou're not allowed to do this";
                    }

                    commandSender.sendMessage(permError);
                    return true;
                }
                if (commandSender instanceof Player) {
                    // teleport to given block;
                    if (commandSender.hasPermission("magicteleport.self") || commandSender.hasPermission("magicteleport.admin")) {

                        return this.plugin.teleport((Player) commandSender, args[0]);
                    } else {
                        String permError = this.plugin.getLanguage().getString("NO_PERMISSION_MESSAGE");

                        if (permError == null){
                            permError = "&cYou're not allowed to do this";
                        }

                        commandSender.sendMessage(permError);
                        return true;
                    }
                }

                return false;
            }

            // Show the list of possible teleport location

            if (commandSender instanceof Player) {
                if (!commandSender.hasPermission("magicteleport.self") && !commandSender.hasPermission("magicteleport.admin")) {
                    String permError = this.plugin.getLanguage().getString("NO_PERMISSION_MESSAGE");

                    if (permError == null){
                        permError = "&cYou're not allowed to do this";
                    }

                    commandSender.sendMessage(permError);
                    return true;
                }
                MiniMessage mm = MiniMessage.miniMessage();

                Audience p = (Audience) commandSender;


                this.plugin.reloadConfig();
                ConfigurationSection apbs = this.plugin.getConfig().getConfigurationSection("player_blocks");
                if (apbs != null) {
                    ConfigurationSection pbs = apbs.getConfigurationSection(commandSender.getName());
                    if (pbs != null) {
                        Set<String> keys = pbs.getKeys(false);

                        if (keys.isEmpty()) {
                            return this.noBlocks(p);
                        }
                        String m = this.plugin.getLanguage().getString("CHOOSE_DESTINATION");
                        if (m == null) {
                            m = "<yellow>Choose a destination</yellow>";
                        }

                        StringBuilder msg = new StringBuilder(m);

                        msg.append("<newline>");

                        for (String key: keys) {
                            ConfigurationSection block = pbs.getConfigurationSection(key);
                            if (block != null) {
                                Location loc = block.getLocation("location");
                                String world = block.getString("world");
                                String display_name = block.getString("display_name");
                                Bukkit.getLogger().info("[MagicTeleport] got block with name " + display_name);
                                if (display_name == null) {
                                    display_name = this.plugin.getLanguage().getString("TELEPORT_BLOCK_NAME");
                                }
                                if (display_name == null) {
                                    display_name = "Teleport block";
                                }
                                String b_format = this.plugin.getLanguage().getString("BLOCK_LIST_FORMAT");
                                if (b_format == null) {
                                    b_format = "<underlined><color:red>%block_ref%<color:grey></underlined> : <color:yellow>%name% <color:grey><newline><bold>[</bold><color:red>%x%<color:grey>, <color:green>%y%<color:grey>, <color:blue>%z%<color:grey><bold>]</bold> <color:grey>World: <color:yellow>%world%";
                                }
                                if (loc != null && world != null) {
                                    msg.append("<click:run_command:/mtp ").append(key).append(">");
                                    String tt = this.plugin.getLanguage().getString("TELEPORT_TO_BLOCK");
                                    if (tt == null) {
                                        tt = "Click here to teleport to this block";
                                    }

                                    msg.append("<hover:show_text:'<green>").append(tt).append("'>");

                                    msg.append(
                                            b_format.replaceAll("%block_ref%", key)
                                            .replaceAll("%name%", display_name)
                                            .replaceAll("%x%", String.format("%d", loc.getBlockX()))
                                            .replaceAll("%y%", String.format("%d", loc.getBlockY()))
                                            .replaceAll("%z%", String.format("%d", loc.getBlockZ()))
                                            .replaceAll("%world%", world)

                                    );
                                    msg.append("<newline>");
                                }

                            }

                        }
                        Component parsed = mm.deserialize(msg.toString());

                        p.sendMessage(parsed);
                        return true;
                    }
                }



                return this.noBlocks(p);

            }
        }

        return false;
    }

    private boolean noBlocks(Audience p) {
        String m = this.plugin.getLanguage().getString("NO_DESTINATION");
        if (m == null) {
            m = "<red>You don't have any placed teleport blocks</red>";
        }
        MiniMessage mm = MiniMessage.miniMessage();
        Component parsed = mm.deserialize(m);
        p.sendMessage(parsed);
        return true;
    }

    private boolean giveItem(CommandSender commandSender, String[] args) {

        if (args.length > 1) {
            Player p = Bukkit.getPlayer(args[1]);
            if (p != null) {
                // TODO get count of blocks for this player.
                // TODO if > 3 then do not give block
                this.plugin.reloadConfig();
                ConfigurationSection pb = this.plugin.getConfig().getConfigurationSection("player_blocks." + p.getName());
                int count = 0;
                if (pb != null) {
                    count += pb.getKeys(false).size();
                }
                // Also count items in inventory ?
//                for (ItemStack item: p.getInventory()) {
//                    ItemMeta meta = item.getItemMeta();
//                    if (meta != null && meta.getLocalizedName().equals("teleport_block")) {
//                        count ++;
//                    }
//                }

                if (count >= MagicTeleport.getAmount(p)) {
                    //TODO send message
                    commandSender.sendMessage("Player " + p.getName() + " already has too many teleport blocks");
                    Bukkit.getLogger().info("[MagicTeleport] Player " + p.getName() + " already has too many teleport blocks placed");
                    String m = this.plugin.getLanguage().getString("TOO_MANY_BLOCKS");
                    if (m == null) {
                        m = "<bold><grey>[</bold><aqua>MagicTeleport</aqua><bold><grey>]</bold> You are using too many <green>teleport blocks</green>, sorry. Your limit is <red>%limit%";
                    }

                    Audience a = (Audience) p;
                    a.sendMessage(MiniMessage.miniMessage().deserialize(m.replaceAll("%limit%", String.valueOf(MagicTeleport.getAmount(p)))));
                    return true;
                }
                ItemStack blk = TeleportBlock.getBlock(null);
                p.getInventory().addItem(blk);
                commandSender.sendMessage("Player " + args[1] + " received the teleport block");
                Bukkit.getLogger().info("[MagicTeleport] Player " + args[1] + " received the teleport block");
                Audience a = (Audience) p;
                String m = this.plugin.getLanguage().getString("PLAYER_RECEIVED_BLOCK");
                if (m == null) {
                    m = "<bold><grey>[</bold><aqua>MagicTeleport</aqua><bold><grey>]</bold> You now have the <green>teleportation bloc</green>.<newline>Place it anywhere and then use <aqua>/mtp</aqua> to teleport to it";
                }

                a.sendMessage(MiniMessage.miniMessage().deserialize(m));
                return true;
            } else {
                commandSender.sendMessage("Player " + args[1] + " does not exist or is offline");
                return true;
            }
        }

        commandSender.sendMessage("Usage: /mtp give <player_name>");
        return true;
    }
}
