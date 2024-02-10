package fr.sixpixels.magicteleport;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagicTeleportCommand  implements CommandExecutor {

    MagicTeleport plugin;
    public MagicTeleportCommand(MagicTeleport plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (s.equalsIgnoreCase("magicteleport") || s.equalsIgnoreCase("mtp")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if (commandSender instanceof ConsoleCommandSender) {
                        return this.giveItem(commandSender, args);
                    }
                    if (commandSender instanceof Player && commandSender.hasPermission("magicteleport.admin")) {
                        return this.giveItem(commandSender, args);
                    }
                }
            }

            return true;
        }

        return false;
    }

    private boolean giveItem(CommandSender commandSender, String[] args) {

        if (args.length > 1) {
            Player p = Bukkit.getPlayer(args[1]);
            if (p != null) {
                ItemStack blk = TeleportBlock.getBlock();
                p.getInventory().addItem(blk);
                commandSender.sendMessage("Player " + args[1] + " received the teleport block");

                return true;
            } else {
                commandSender.sendMessage("Player " + args[1] + " does not exist or is offline");
            }
        }

        commandSender.sendMessage("Usage: /mtp give <player_name>");
        return false;
    }
}
