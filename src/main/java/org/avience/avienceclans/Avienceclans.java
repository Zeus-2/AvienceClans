package org.avience.avienceclans;

import org.avience.avienceclans.ClanChat.ClanChat;
import org.avience.avienceclans.ClanChat.ClanChatCommandExecutor;
import org.avience.avienceclans.ClanChat.ClanChatListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public final class Avienceclans extends JavaPlugin {
    private ClanChat clanChat;
    private File clanFile;
    private FileConfiguration clanConfig;

    @Override
    public void onEnable() {
        // Initialize the clan file
        createClanFile();

        // Initialize ClanChat
        this.clanChat = new ClanChat(this);

        // Register the command executor for /clanchat
        Objects.requireNonNull(this.getCommand("clanchat")).setExecutor(new ClanChatCommandExecutor(this));

        // Register the event listener for chat events
        getServer().getPluginManager().registerEvents(new ClanChatListener(this), this);

        // Initialize ClanCommand and set it as the executor and tab completer for /clan
        ClanCommand clanCommand = new ClanCommand(this);
        Objects.requireNonNull(this.getCommand("clan")).setExecutor(clanCommand);
        Objects.requireNonNull(this.getCommand("clan")).setTabCompleter(clanCommand);

        // PlaceholderAPI support
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholder(this).register();
        }

        // Register permissions
        Permission reloadPermission = new Permission("avienceClans.Reload");
        Permission statusPermission = new Permission("avienceClans.Status");
        getServer().getPluginManager().addPermission(reloadPermission);
        getServer().getPluginManager().addPermission(statusPermission);
    }


    public Map<String, Integer> getClanStatistics() {
        ConfigurationSection clansSection = clanConfig.getConfigurationSection("clans");
        Map<String, Integer> statistics = new HashMap<>();

        if (clansSection == null) return statistics;

        int totalMembers = 0;
        int largestClanSize = 0;
        int smallestClanSize = Integer.MAX_VALUE;

        for (String clan : clansSection.getKeys(false)) {
            List<String> members = clanConfig.getStringList("clans." + clan + ".members");
            int clanSize = members.size();

            totalMembers += clanSize;

            if (clanSize > largestClanSize) {
                largestClanSize = clanSize;
            }

            if (clanSize < smallestClanSize) {
                smallestClanSize = clanSize;
            }
        }

        int numberOfClans = clansSection.getKeys(false).size();
        int averageMembers = numberOfClans > 0 ? totalMembers / numberOfClans : 0;

        statistics.put("TotalClans", numberOfClans);
        statistics.put("TotalMembers", totalMembers);
        statistics.put("AverageMembers", averageMembers);
        statistics.put("LargestClanSize", largestClanSize);

        return statistics;
    }


    @Override
    public void onDisable() {
        saveClanFile();
    }

    public FileConfiguration getClanConfig() {
        return this.clanConfig;
    }

    public String getClanName(Player player) {
        ConfigurationSection clansSection = clanConfig.getConfigurationSection("clans");
        if (clansSection != null) {
            for (String existingClan : clansSection.getKeys(false)) {
                String leaderUUID = clanConfig.getString("clans." + existingClan + ".leader");
                List<String> memberUUIDs = clanConfig.getStringList("clans." + existingClan + ".members");
                List<String> moderatorUUIDs = clanConfig.getStringList("clans." + existingClan + ".moderators");
                List<String> adminUUIDs = clanConfig.getStringList("clans." + existingClan + ".admins");

                String playerUUID = player.getUniqueId().toString();

                if (playerUUID.equalsIgnoreCase(leaderUUID) ||
                        memberUUIDs.contains(playerUUID) ||
                        moderatorUUIDs.contains(playerUUID) ||
                        adminUUIDs.contains(playerUUID)) {
                    return existingClan;
                }
            }
        }
        return null; // Return null if the player is not in any clan
    }



    private void createClanFile() {
        clanFile = new File(getDataFolder(), "clans.yml");

        if (!clanFile.exists()) {
            clanFile.getParentFile().mkdirs();
            try {
                // Check if a default file exists in the resources folder
                InputStream defaultClanStream = this.getResource("clans.yml");
                if (defaultClanStream != null) {
                    // Copy the default file from resources to the plugin's data folder
                    Files.copy(defaultClanStream, clanFile.toPath());
                } else {
                    // Create a new empty file if no default exists
                    clanFile.createNewFile();
                }

                // Load the configuration
                clanConfig = YamlConfiguration.loadConfiguration(clanFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            clanConfig = YamlConfiguration.loadConfiguration(clanFile);
        }

        if (clanConfig == null) {
            getLogger().severe("Failed to load clanConfig!");
        }
    }




    public void saveClanFile() {
        try {
            clanConfig.save(clanFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClanChat getClanChat() {
        return this.clanChat;
    }

    public void reloadClanConfig() {
        clanConfig = YamlConfiguration.loadConfiguration(clanFile);
    }

    public void sendPrefixedMessage(Player player, String message) {
        String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "Clans" + ChatColor.GRAY + "] " + ChatColor.RESET + ChatColor.AQUA;
        player.sendMessage(prefix + message);
    }
}
