package org.avience.avienceclans.ClanChat;

import org.avience.avienceclans.Avienceclans;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




public class ClanChat implements TabCompleter {
    private Avienceclans plugin;  // Reference to your main plugin class
    private final Set<Player> playersInClanChat;  // Set to keep track of players in clan chat

    // Constructor to initialize the plugin and playersInClanChat
    public ClanChat(Avienceclans plugin) {
        this.plugin = plugin;
        this.playersInClanChat = new HashSet<>();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("clanchat")) {
            if (args.length == 1) {
                list.add("toggle");
                list.add("spy");
            }
        }
        return list;
    }

    public boolean isPlayerInClanChat(Player player) {
        return playersInClanChat.contains(player);
    }

    // Method to toggle clan chat for a player
    public void toggleClanChat(Player player) {
        if (playersInClanChat.contains(player)) {
            playersInClanChat.remove(player);
            player.sendMessage(ChatColor.GREEN + "Clan chat disabled.");
        } else {
            playersInClanChat.add(player);
            player.sendMessage(ChatColor.GREEN + "Clan chat enabled.");
        }
    }

    // Method to send a message to all players in the same clan
    public void sendMessageToClan(Player sender, String message) {
        String clanName = plugin.getClanName(sender);  // Assuming you have a getClanName method in your main class

        if (clanName == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a clan.");
            return;
        }

        String formattedMessage = ChatColor.GRAY + "[" + ChatColor.GOLD + "ClanChat" + ChatColor.GRAY + "] " + ChatColor.AQUA + sender.getName() + ": " + message;

        for (Player player : playersInClanChat) {
            if (plugin.getClanName(player).equals(clanName)) {
                player.sendMessage(formattedMessage);
            }
        }
    }
}
