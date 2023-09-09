package org.avience.avienceclans;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;


import java.util.*;

import java.util.List;

public final class Avienceclans extends JavaPlugin {

    private File clanFile;
    private FileConfiguration clanConfig;

    public void sendPrefixedMessage(Player player, String message) {
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Clans" + ChatColor.GRAY + "] " + ChatColor.RESET + ChatColor.AQUA + message);
    }

    @Override
    public void onEnable() {
        // Initialize the clan file
        createClanFile();

        // Register the command executor
        Objects.requireNonNull(this.getCommand("clan")).setExecutor(new ClanCommand(this));


        ClanCommand clanCommand = new ClanCommand(this);
        Objects.requireNonNull(this.getCommand("clan")).setExecutor(clanCommand);
        Objects.requireNonNull(this.getCommand("clan")).setTabCompleter(clanCommand);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholder(this).register();
        }

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
        String largestClan = "";
        String smallestClan = "";

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
        statistics.put("SmallestClanSize", smallestClanSize);

        return statistics;
    }


    @Override
    public void onDisable() {
        // Save the clan file
        saveClanFile();
    }

    public FileConfiguration getClanConfig() {
        return this.clanConfig;
    }

    public String getClanName(Player player) {
        ConfigurationSection clansSection = clanConfig.getConfigurationSection("clans");
        if (clansSection != null) {
            for (String existingClan : clansSection.getKeys(false)) {
                String roleOfPlayer = clanConfig.getString("clans." + existingClan + ".members." + player.getName() + ".role");
                if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader"))
                        || "admin".equals(roleOfPlayer)
                        || "moderator".equals(roleOfPlayer)
                        || "member".equals(roleOfPlayer)) {
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
                clanFile.createNewFile();

                // Create initial clan structure
                clanConfig = YamlConfiguration.loadConfiguration(clanFile);
                clanConfig.createSection("clans");

                // Create an example clan with sections and data
                ConfigurationSection exampleClan = clanConfig.createSection("clans.clan1");
                exampleClan.set("leader", "PlayerName");
                exampleClan.set("members", Arrays.asList("Member1", "Member2"));
                exampleClan.set("moderators", Arrays.asList("Moderator1", "Moderator2"));
                exampleClan.set("admins", Arrays.asList("Admin1", "Admin2"));

                saveClanFile(); // Save the changes
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

    public void reloadClanConfig() {
        clanConfig = YamlConfiguration.loadConfiguration(clanFile);
    }
}
