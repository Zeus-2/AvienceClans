package org.avience.avienceclans.ClanChat;

import org.avience.avienceclans.Avienceclans;
import org.avience.avienceclans.ClanChat.ClanChat;
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


        if (clanChat.isPlayerInClanChat(player)) {
            event.setCancelled(true);
            clanChat.sendMessageToClan(player, event.getMessage());
        }
    }



}