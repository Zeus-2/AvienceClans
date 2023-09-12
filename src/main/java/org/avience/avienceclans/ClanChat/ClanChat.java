package org.avience.avienceclans.ClanChat;

import org.avience.avienceclans.Avienceclans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ClanChat {
    private Avienceclans plugin;
    private final Set<Player> playersInClanChat;
    private final Set<Player> playersInSpyMode;  // Set to keep track of players in spy mode

    // Constructor to initialize the plugin and playersInClanChat
    public ClanChat(Avienceclans plugin) {
        this.plugin = plugin;
        this.playersInClanChat = new HashSet<>();
        this.playersInSpyMode = new HashSet<>();
    }

    // Method to check if a player is in clan chat
    public boolean isPlayerInClanChat(Player player) {
        return playersInClanChat.contains(player);
    }

    // Method to toggle clan chat for a player
    public void toggleClanChat(Player player) {
        if (playersInClanChat.contains(player)) {
            playersInClanChat.remove(player);
            plugin.sendPrefixedMessage(player,ChatColor.GREEN + "Clan chat disabled.");
        } else {
            playersInClanChat.add(player);
            plugin.sendPrefixedMessage(player,ChatColor.GREEN + "Clan chat enabled.");
        }
    }

    // Method to send a message to all players in the same clan
    public void sendMessageToClan(Player sender, String message) {
        String clanName = plugin.getClanName(sender);  // Assuming you have a getClanName method in your main class

        if (clanName == null) {
            plugin.sendPrefixedMessage(sender,ChatColor.RED + "You are not in a clan.");
            return;
        }

        String formattedMessage = ChatColor.GRAY + "[" + ChatColor.GOLD + clanName + ChatColor.GRAY + "] "
                + ChatColor.AQUA + sender.getName() + ": " + message;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (clanName.equals(plugin.getClanName(player))) {
                player.sendMessage(formattedMessage);
            }
        }
        for (Player spy : playersInSpyMode) {
            spy.sendMessage(ChatColor.GRAY + "[Spy] " + formattedMessage);
        }
    }

    // Add a method to toggle spy mode
    public void toggleSpyMode(Player player) {
        if (playersInSpyMode.contains(player)) {
            playersInSpyMode.remove(player);
            plugin.sendPrefixedMessage(player,ChatColor.RED + "Clan chat spy mode disabled.");
        } else {
            playersInSpyMode.add(player);
            plugin.sendPrefixedMessage(player,ChatColor.GREEN + "Clan chat spy mode enabled.");
        }
    }

}
