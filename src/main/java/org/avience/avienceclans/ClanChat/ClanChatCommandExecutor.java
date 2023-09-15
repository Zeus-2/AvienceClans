package org.avience.avienceclans.ClanChat;

import org.avience.avienceclans.Avienceclans;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanChatCommandExecutor implements CommandExecutor {
    private final Avienceclans plugin;

    public ClanChatCommandExecutor(Avienceclans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        ClanChat clanChat = plugin.getClanChat();

        if (command.getName().equalsIgnoreCase("clanchat")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.AQUA + "/clanchat toggle" + ChatColor.WHITE + " - Toggle clan chat on/off.");
                player.sendMessage(ChatColor.AQUA + "/clanchat spy" + ChatColor.WHITE + " - Toggle clan chat spy mode on/off.");
                return true;
            }

            if (args[0].equalsIgnoreCase("toggle")) {
                ClanChat.toggleClanChat(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("spy")) {
                if (sender.hasPermission("clanchat.spy")) {  // Check for permission
                    ClanChat.toggleSpyMode(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
            }

        }

        return true;
    }
}

