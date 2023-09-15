package org.avience.avienceclans.ClanChat;

import org.avience.avienceclans.Avienceclans;
import org.avience.avienceclans.ClanChat.ClanChat;
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
        ClanChat clanChat = plugin.getClanChat();  // Assuming you have a getClanChat method in your main class

        if (command.getName().equalsIgnoreCase("clanchat")) {
            clanChat.toggleClanChat(player);
            return true;
        }

        return false;
    }
}

