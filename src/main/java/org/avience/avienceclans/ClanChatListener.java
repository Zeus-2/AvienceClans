package org.avience.avienceclans;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ClanChatListener implements Listener {
    private final Avienceclans plugin;

    public ClanChatListener(Avienceclans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ClanChat clanChat = plugin.getClanChat();

        if (clanChat.isPlayerInClanChat(player)) {  // Assuming you have a method to check this
            event.setCancelled(true);  // Cancel the normal chat event
            clanChat.sendMessageToClan(player, event.getMessage());  // Send the message to the clan
        }
    }
}
