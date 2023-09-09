package org.avience.avienceclans;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ClanPlaceholder extends PlaceholderExpansion {

    private Avienceclans plugin;

    public ClanPlaceholder(Avienceclans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "YourName";
    }

    @Override
    public String getIdentifier() {
        return "avienceclans";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // %avienceclans_clan%
        if ("clan".equals(identifier)) {
            // Your code to get the clan name
            String clanName = plugin.getClanName(player);
            return clanName != null ? clanName : "";
        }

        return null;
    }
}
