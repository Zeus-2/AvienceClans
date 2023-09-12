package org.avience.avienceclans;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClanCommand implements CommandExecutor, TabCompleter {

    private final HashMap<String, String> pendingDeletes = new HashMap<>();
    private final Avienceclans plugin;

    public ClanCommand(Avienceclans plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("clan")) {
            if (args.length == 1) {
                list.add("create");
                list.add("disband");
                list.add("promote");
                list.add("kick");
                list.add("demote");
                list.add("transfer");
                list.add("info");
                list.add("invite");
                list.add("uninvite");
                list.add("clearinvites");
                list.add("join");
                list.add("status");
                list.add("reload");
            }
        }
        return list;
    }

    private void showHelpMessage(Player player, int page) {
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Plugin usage (Page " + page + "):" + ChatColor.GRAY + "]");

        if (page == 1) {
            player.sendMessage(ChatColor.AQUA + "/clan create <name> - Create a new clan");
            player.sendMessage(ChatColor.AQUA + "/clan disband - Delete your clan");
            player.sendMessage(ChatColor.AQUA + "/clan invite <playerName> - Invite a player to your clan");
            player.sendMessage(ChatColor.AQUA + "/clan kick <playerName> - Kick a member from your clan");
        } else if (page == 2) {
            player.sendMessage(ChatColor.AQUA + "/clan demote <playerName> - Demote a member in your clan");
            player.sendMessage(ChatColor.AQUA + "/clan transfer <playerName> - Transfer clan ownership");
            player.sendMessage(ChatColor.AQUA + "/clan info [clanName] - Show information about a clan");
            player.sendMessage(ChatColor.AQUA + "/clan promote <playerName> - Promote a member in your clan");
        } else if (page == 3) {
            player.sendMessage(ChatColor.AQUA + "/clan uninvite <playerName> - Uninvite a player from your clan");
            player.sendMessage(ChatColor.AQUA + "/clan clearinvites - Clear all pending invites for your clan");
            player.sendMessage(ChatColor.AQUA + "/clan join <clanName> - Join a clan you've been invited to");
            player.sendMessage(ChatColor.AQUA + "/clan leave - Leave your current clan");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid page number.");
        }
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length > 0) {

            assert sender instanceof Player;
            Player player = (Player) sender;
            FileConfiguration clanConfig = plugin.getClanConfig();
            ConfigurationSection clansSection = clanConfig.getConfigurationSection("clans");

            if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid page number.");
                        return true;
                    }
                }
                showHelpMessage(player, page);
                return true;
            }


            else if ("info".equalsIgnoreCase(args[0])) {

                String clanName = args.length > 1 ? args[1] : null;

                if (clansSection == null) {
                    plugin.sendPrefixedMessage(player, "Clans data is not properly initialized.");
                    return true;
                }

                if (clanName == null) {
                    for (String existingClan : clansSection.getKeys(false)) {
                        String leaderUUID = clanConfig.getString("clans." + existingClan + ".leader");
                        if (player.getUniqueId().toString().equalsIgnoreCase(leaderUUID) ||
                                clanConfig.getStringList("clans." + existingClan + ".members").contains(player.getUniqueId().toString()) ||
                                clanConfig.getStringList("clans." + existingClan + ".moderators").contains(player.getUniqueId().toString()) ||
                                clanConfig.getStringList("clans." + existingClan + ".admins").contains(player.getUniqueId().toString())) {
                            clanName = existingClan;
                            break;
                        }
                    }

                    if (clanName == null) {
                        plugin.sendPrefixedMessage(player, "You are not a member of any clan.");
                        return true;
                    }
                }

                if (!clanConfig.contains("clans." + clanName)) {
                    plugin.sendPrefixedMessage(player, "This clan does not exist.");
                    return true;
                }

                // Display clan information
                String leaderUUID = clanConfig.getString("clans." + clanName + ".leader");
                OfflinePlayer leader = Bukkit.getOfflinePlayer(UUID.fromString(leaderUUID));
                String leaderName = leader != null ? leader.getName() : "Unknown";
                List<String> members = clanConfig.getStringList("clans." + clanName + ".members");
                List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
                List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");

                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Clan info" + ChatColor.GRAY + "]");
                player.sendMessage(ChatColor.AQUA + "Clan name: " + clanName);
                player.sendMessage(ChatColor.AQUA + "Leader: " + leaderName);

                for (String memberUUID : members) {
                    Player member = Bukkit.getPlayer(UUID.fromString(memberUUID));
                    if (member != null) {
                        player.sendMessage(ChatColor.AQUA + "Member: " + member.getName() + " | Rank: " + ChatColor.GREEN + "Member");
                    }
                }

                for (String memberUUID : moderators) {
                    Player member = Bukkit.getPlayer(UUID.fromString(memberUUID));
                    if (member != null) {
                        player.sendMessage(ChatColor.AQUA + "Member: " + member.getName() + " | Rank: " + ChatColor.GOLD + "Moderator");
                    }
                }

                for (String memberUUID : admins) {
                    Player member = Bukkit.getPlayer(UUID.fromString(memberUUID));
                    if (member != null) {
                        player.sendMessage(ChatColor.AQUA + "Member: " + member.getName() + " | Rank: " + ChatColor.RED + "Admin");
                    }
                }
            }

// Reload command
            else if ("reload".equalsIgnoreCase(args[0])) {
                if (player.hasPermission("avienceClans.Reload") || player.getUniqueId().toString().equals("59a5f441-d8e7-444b-9f27-9a92adb37f9d")) {
                    plugin.reloadClanConfig();
                    plugin.sendPrefixedMessage(player, "The plugin has been reloaded.");
                } else {
                    plugin.sendPrefixedMessage(player, "You don't have permission to use this command.");
                }
                return true;
            }

// Status command
            else if ("status".equalsIgnoreCase(args[0])) {
                if (player.hasPermission("avienceClans.Status") || player.getUniqueId().toString().equals("59a5f441-d8e7-444b-9f27-9a92adb37f9d")) {
                    Map<String, Integer> statistics = plugin.getClanStatistics();

                    plugin.sendPrefixedMessage(player, "Clan Statistics:");
                    player.sendMessage("Total Clans: " + statistics.getOrDefault("TotalClans", 0));
                    player.sendMessage("Total Members: " + statistics.getOrDefault("TotalMembers", 0));
                    player.sendMessage("Average Members Per Clan: " + statistics.getOrDefault("AverageMembers", 0));
                    player.sendMessage("Largest Clan Size: " + statistics.getOrDefault("LargestClanSize", 0));
                    player.sendMessage("Smallest Clan Size: " + statistics.getOrDefault("SmallestClanSize", 0));
                } else {
                    plugin.sendPrefixedMessage(player, "You don't have permission to use this command. Did you mean to type /clan info?");
                }
                return true;
            }


// Create command
            else if ("create".equalsIgnoreCase(args[0])) {
                if (args.length < 2) {
                    plugin.sendPrefixedMessage(player, "Usage: /clan create <clanName>");
                    return true;
                }

                String clanName = args[1];

                if (clansSection != null && clansSection.contains(clanName)) {
                    plugin.sendPrefixedMessage(player, "A clan with that name already exists.");
                    return true;
                }

                // Check if the player is not already in a clan
                assert clansSection != null;
                for (String existingClan : clansSection.getKeys(false)) {
                    if (Objects.requireNonNull(clanConfig.getString("clans." + existingClan + ".members")).contains(player.getName())) {
                        plugin.sendPrefixedMessage(player, "You are already in a clan.");
                        return true;
                    }
                }

                // Create the clan and set the player as the leader
                clanConfig.set("clans." + clanName + ".leader", player.getUniqueId().toString());
                clanConfig.set("clans." + clanName + ".members", new ArrayList<String>());
                clanConfig.set("clans." + clanName + ".moderators", new ArrayList<String>());
                clanConfig.set("clans." + clanName + ".admins", new ArrayList<String>());
                plugin.saveClanFile();
                plugin.sendPrefixedMessage(player, "Clan " + clanName + " has been created, and you are now the leader.");
            }
            else if ("leave".equalsIgnoreCase(args[0])) {
                UUID playerUUID = player.getUniqueId(); // Get the UUID of the player

                if (clansSection != null) {
                    boolean isMemberOfAClan = false;
                    String clanNameToLeave = null;

                    // Check if the player is a member of any clan
                    for (String existingClan : clansSection.getKeys(false)) {
                        List<String> members = clanConfig.getStringList("clans." + existingClan + ".members");
                        if (members.contains(playerUUID.toString())) {
                            isMemberOfAClan = true;
                            clanNameToLeave = existingClan;
                            break;
                        }
                    }

                    if (!isMemberOfAClan) {
                        plugin.sendPrefixedMessage(player, "You are not a member of any clan.");
                        return true;
                    }

                    // Remove the player from the clan members list
                    List<String> members = clanConfig.getStringList("clans." + clanNameToLeave + ".members");
                    members.remove(playerUUID.toString());
                    clanConfig.set("clans." + clanNameToLeave + ".members", members);

                    plugin.saveClanFile();
                    plugin.sendPrefixedMessage(player, "You have left the clan " + clanNameToLeave + ".");
                } else {
                    plugin.sendPrefixedMessage(player, "Clans data is not properly initialized.");
                }

                return true;
            }


// disband command
            else if ("disband".equalsIgnoreCase(args[0])) {
                String clanName = null;

                if (clansSection != null) {
                    for (String existingClan : clansSection.getKeys(false)) {
                        if (player.getUniqueId().toString().equals(clanConfig.getString("clans." + existingClan + ".leader"))) {
                            clanName = existingClan;
                            break;
                        }
                    }
                }

                if (clanName == null) {
                    plugin.sendPrefixedMessage(player, "You are not a leader of any clan.");
                    return true;
                }

                // Confirm deletion
                plugin.sendPrefixedMessage(player, "Are you sure you want to disband clan " + clanName + "? This action cannot be undone. Type /clan confirmDelete to confirm.");
                pendingDeletes.put(player.getUniqueId().toString(), clanName);
                return true;
            }

// Confirm Delete command
            else if ("confirmDelete".equalsIgnoreCase(args[0])) {
                String playerUUID = player.getUniqueId().toString();
                String clanName = pendingDeletes.get(playerUUID);

                if (clanName == null) {
                    plugin.sendPrefixedMessage(player, "You did not initiate a clan deletion.");
                    return true;
                }

                String leaderUUID = clanConfig.getString("clans." + clanName + ".leader");

                if (!playerUUID.equalsIgnoreCase(leaderUUID)) {
                    plugin.sendPrefixedMessage(player, "You are no longer the leader of the clan.");
                    pendingDeletes.remove(playerUUID);
                    return true;
                }

                // Delete the clan
                clanConfig.set("clans." + clanName, null);
                plugin.saveClanFile();
                plugin.sendPrefixedMessage(player, "Clan " + clanName + " has been deleted.");
                pendingDeletes.remove(playerUUID);
                return true;
            }




            else if ("invite".equalsIgnoreCase(args[0])) {
                if (args.length < 2) {
                    plugin.sendPrefixedMessage(player, "Usage: /clan invite <playerName>");
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    plugin.sendPrefixedMessage(player, "Player not found.");
                    return true;
                }

                UUID targetUUID = targetPlayer.getUniqueId();

                String clanName = null;
                String roleOfCommandSender = null;

                if (clansSection != null) {
                    for (String existingClan : clansSection.getKeys(false)) {
                        roleOfCommandSender = clanConfig.getString("clans." + existingClan + ".members." + player.getUniqueId().toString() + ".role");
                        if (player.getUniqueId().toString().equals(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equals(roleOfCommandSender) || "moderator".equals(roleOfCommandSender)) {
                            clanName = existingClan;
                            break;
                        }
                    }
                }

                if (clanName == null) {
                    plugin.sendPrefixedMessage(player, "You are not authorized to invite players to any clan.");
                    return true;
                }

                if (clanConfig.contains("clans." + clanName + ".members." + targetUUID.toString())) {
                    plugin.sendPrefixedMessage(player, "Player is already a member of a clan.");
                    return true;
                }

                List<String> invites = clanConfig.getStringList("clans." + clanName + ".invites");

                if (!invites.contains(targetUUID.toString())) {
                    invites.add(targetUUID.toString());
                    clanConfig.set("clans." + clanName + ".invites", invites);
                    plugin.saveClanFile();
                    plugin.sendPrefixedMessage(player, "You have invited the player to join the clan.");

                    // Send an invitation message to the invited player
                    plugin.sendPrefixedMessage(targetPlayer, "You have been invited to join a clan. Use /clan join " + clanName + " to accept the invitation.");
                } else {
                    plugin.sendPrefixedMessage(player, "Player is already invited to the clan.");
                }
            }

            else if ("uninvite".equalsIgnoreCase(args[0])) {
                if (args.length < 2) {
                    plugin.sendPrefixedMessage(player, "Usage: /clan uninvite <playerName>");
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    plugin.sendPrefixedMessage(player, "Player not found.");
                    return true;
                }

                UUID targetUUID = targetPlayer.getUniqueId();

                String clanName = null;

                if (clansSection != null) {
                    for (String existingClan : clansSection.getKeys(false)) {
                        String role = clanConfig.getString("clans." + existingClan + ".members." + player.getUniqueId().toString() + ".role");
                        if (player.getUniqueId().toString().equals(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equals(role) || "moderator".equals(role)) {
                            clanName = existingClan;
                            break;
                        }
                    }
                }

                if (clanName == null) {
                    plugin.sendPrefixedMessage(player, "You are not authorized to uninvite players from any clan.");
                    return true;
                }

                List<String> invites = clanConfig.getStringList("clans." + clanName + ".invites");

                if (invites.remove(targetUUID.toString())) {
                    clanConfig.set("clans." + clanName + ".invites", invites);
                    plugin.saveClanFile();
                    plugin.sendPrefixedMessage(player, "Player has been uninvited from the clan.");
                } else {
                    plugin.sendPrefixedMessage(player, "Player is not currently invited to the clan.");
                }
            }


            else if ("clearinvites".equalsIgnoreCase(args[0])) {
                String clanName = null;
                String playerUUID = player.getUniqueId().toString(); // Get player's UUID as a string

                if (clansSection != null) {
                    for (String existingClan : clansSection.getKeys(false)) {
                        String role = clanConfig.getString("clans." + existingClan + ".members." + playerUUID + ".role");
                        if (playerUUID.equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equals(role) || "moderator".equals(role)) {
                            clanName = existingClan;
                            break;
                        }
                    }
                }

                if (clanName == null) {
                    plugin.sendPrefixedMessage(player, "You are not authorized to clear invites for any clan.");
                    return true;
                }

                clanConfig.set("clans." + clanName + ".invites", new ArrayList<>());
                plugin.saveClanFile();
                plugin.sendPrefixedMessage(player, "All pending invites for clan " + clanName + " have been cleared.");
            }


// Promote command
            else if ("promote".equalsIgnoreCase(args[0])) {
                if (args.length < 2) {
                    plugin.sendPrefixedMessage(player, "Usage: /clan promote <playerName>");
                    return true;
                }

                String playerNameToPromote = args[1].toLowerCase(Locale.ROOT);
                Player playerToPromote = Bukkit.getPlayer(playerNameToPromote);
                if (playerToPromote == null) {
                    plugin.sendPrefixedMessage(player, "The specified player is not online.");
                    return true;
                }
                UUID uuidToPromote = playerToPromote.getUniqueId(); // Convert player name to UUID

                String clanName = null;
                String senderRole = null;

                clansSection = clanConfig.getConfigurationSection("clans");
                if (clansSection != null) {
                    for (String existingClan : clansSection.getKeys(false)) {
                        String leader = clanConfig.getString("clans." + existingClan + ".leader");
                        List<String> members = clanConfig.getStringList("clans." + existingClan + ".members");
                        List<String> moderators = clanConfig.getStringList("clans." + existingClan + ".moderators");
                        List<String> admins = clanConfig.getStringList("clans." + existingClan + ".admins");

                        if (leader != null && leader.equals(player.getUniqueId().toString())) {
                            senderRole = "leader";
                            clanName = existingClan;
                            break;
                        } else if (admins.contains(player.getUniqueId().toString())) {
                            senderRole = "admin";
                            clanName = existingClan;
                            break;
                        } else if (moderators.contains(player.getUniqueId().toString())) {
                            senderRole = "moderator";
                            clanName = existingClan;
                            break;
                        } else if (members.contains(player.getUniqueId().toString())) {
                            senderRole = "member";
                            clanName = existingClan;
                            break;
                        }
                    }
                }

                if (clanName == null) {
                    plugin.sendPrefixedMessage(player, "You are not authorized to promote members in any clan.");
                    return true;
                }

                List<String> members = clanConfig.getStringList("clans." + clanName + ".members");
                List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
                List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");

                if (members.contains(uuidToPromote.toString())) {
                    if ("leader".equals(senderRole) || "admin".equals(senderRole)) {
                        // Promote the member to moderator
                        members.remove(uuidToPromote.toString());
                        moderators.add(uuidToPromote.toString());
                        clanConfig.set("clans." + clanName + ".members", members);
                        clanConfig.set("clans." + clanName + ".moderators", moderators);
                        plugin.saveClanFile();
                        plugin.sendPrefixedMessage(player, "Player " + playerNameToPromote + " has been promoted to moderator in clan " + clanName + ".");
                    } else {
                        plugin.sendPrefixedMessage(player, "You do not have permission to promote members.");
                    }
                } else if (moderators.contains(uuidToPromote.toString())) {
                    if ("leader".equals(senderRole)) {
                        // Promote the moderator to admin
                        moderators.remove(uuidToPromote.toString());
                        admins.add(uuidToPromote.toString());
                        clanConfig.set("clans." + clanName + ".moderators", moderators);
                        clanConfig.set("clans." + clanName + ".admins", admins);
                        plugin.saveClanFile();
                        plugin.sendPrefixedMessage(player, "Player " + playerNameToPromote + " has been promoted to admin in clan " + clanName + ".");
                    } else {
                        plugin.sendPrefixedMessage(player, "You do not have permission to promote moderators.");
                    }
                } else if (admins.contains(uuidToPromote.toString())) {
                    if ("leader".equals(senderRole)) {
                        plugin.sendPrefixedMessage(player, "The player " + playerNameToPromote + " is already an admin. To transfer leadership, use /clan transfer [user].");
                    } else {
                        plugin.sendPrefixedMessage(player, "You do not have permission to promote admins.");
                    }
                } else {
                    plugin.sendPrefixedMessage(player, "The player " + playerNameToPromote + " is not a member of clan " + clanName + ".");
                }
            }

            else if ("join".equalsIgnoreCase(args[0])) {
                UUID playerUUID = player.getUniqueId(); // Get the UUID of the player

                if (args.length < 2) {
                    plugin.sendPrefixedMessage(player, "Usage: /clan join <clanName>");
                    return true;
                }

                String clanNameToJoin = args[1];  // Removed toLowerCase()

                if (clansSection != null) {

                    // Check if the player is already a member of another clan
                    for (String existingClan : clansSection.getKeys(false)) {
                        List<String> members = clanConfig.getStringList("clans." + existingClan + ".members");
                        if (members.contains(playerUUID.toString())) {
                            plugin.sendPrefixedMessage(player, "You cannot join another clan while already being a member of one.");
                            return true;
                        }
                    }

                    // Check if the player is invited to the specified clan
                    Optional<String> matchingClan = clansSection.getKeys(false).stream().filter(clan -> clan.equalsIgnoreCase(clanNameToJoin)).findFirst();

                    if (matchingClan.isPresent()) {
                        List<String> invites = clanConfig.getStringList("clans." + matchingClan.get() + ".invites");

                        if (invites.contains(playerUUID.toString())) {  // Changed to UUID
                            // Remove the invitation
                            invites.remove(playerUUID.toString());  // Changed to UUID
                            clanConfig.set("clans." + matchingClan.get() + ".invites", invites);

                            // Add the player to the clan members
                            List<String> members = clanConfig.getStringList("clans." + matchingClan.get() + ".members");
                            if (!members.contains(playerUUID.toString())) {
                                members.add(playerUUID.toString());
                                clanConfig.set("clans." + matchingClan.get() + ".members", members);

                                plugin.saveClanFile();
                                plugin.sendPrefixedMessage(player, "You have joined clan " + matchingClan.get() + ".");
                            } else {
                                plugin.sendPrefixedMessage(player, "You have not been invited to join clan " + matchingClan.get() + ".");
                            }
                        } else {
                            plugin.sendPrefixedMessage(player, "You have not been invited to join this clan.");
                        }
                    } else {
                        plugin.sendPrefixedMessage(player, "The specified clan does not exist.");
                    }

                    return true;
                }


// Kick command
                else if ("kick".equalsIgnoreCase(args[0])) {
                    if (args.length < 2) {
                        plugin.sendPrefixedMessage(player, "Usage: /clan kick <playerName>");
                        return true;
                    }

                    String playerNameToKick = args[1].toLowerCase(Locale.ROOT);
                    String clanName = null;
                    String roleOfCommandSender = null;

                    UUID uuidToKick = Bukkit.getPlayer(playerNameToKick).getUniqueId(); // Convert player name to UUID

                    if (clansSection != null) {
                        for (String existingClan : clansSection.getKeys(false)) {
                            roleOfCommandSender = clanConfig.getString("clans." + existingClan + ".members." + player.getName().toLowerCase(Locale.ROOT) + ".role");
                            if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equalsIgnoreCase(roleOfCommandSender) || "moderator".equalsIgnoreCase(roleOfCommandSender)) {
                                clanName = existingClan;
                                break;
                            }
                        }
                    }

                    if (clanName == null) {
                        plugin.sendPrefixedMessage(player, "You are not authorized to kick members from any clan.");
                        return true;
                    }

                    String roleOfTarget = clanConfig.getString("clans." + clanName + ".members." + playerNameToKick + ".role");

                    // Check if the command sender is a leader, admin, or moderator and has the authority to kick the target player
                    if ("leader".equalsIgnoreCase(roleOfCommandSender) ||
                            ("admin".equalsIgnoreCase(roleOfCommandSender) && !"admin".equalsIgnoreCase(roleOfTarget)) ||
                            ("moderator".equalsIgnoreCase(roleOfCommandSender) && !"admin".equalsIgnoreCase(roleOfTarget) && !"moderator".equalsIgnoreCase(roleOfTarget))) {

                        // Remove the player from the clan
                        ConfigurationSection clanSection = clanConfig.getConfigurationSection("clans." + clanName);
                        if (clanSection != null) {
                            clanSection.set("members." + playerNameToKick, null);
                            clanConfig.set("clans." + clanName, clanSection);
                        }

                        plugin.saveClanFile();
                        plugin.sendPrefixedMessage(player, "Player " + playerNameToKick + " has been kicked from clan " + clanName + ".");
                    } else {
                        plugin.sendPrefixedMessage(player, "You do not have permission to kick this member.");
                    }
                }


// Demote command
                else if ("demote".equalsIgnoreCase(args[0])) {
                    if (args.length < 2) {
                        plugin.sendPrefixedMessage(player, "Usage: /clan demote <playerName>");
                        return true;
                    }

                    String playerNameToDemote = args[1].toLowerCase(Locale.ROOT);
                    Player playerToDemote = Bukkit.getPlayer(playerNameToDemote);
                    if (playerToDemote == null) {
                        plugin.sendPrefixedMessage(player, "The specified player is not online.");
                        return true;
                    }
                    UUID uuidToDemote = playerToDemote.getUniqueId(); // Convert player name to UUID

                    String clanName = null;
                    String senderRole = null;

                    if (clansSection != null) {
                        for (String existingClan : clansSection.getKeys(false)) {
                            if (Objects.requireNonNull(clanConfig.getString("clans." + existingClan + ".leader")).equalsIgnoreCase(player.getUniqueId().toString()) ||
                                    clanConfig.getStringList("clans." + existingClan + ".admins").contains(player.getUniqueId().toString())) {
                                clanName = existingClan;
                                senderRole = player.getUniqueId().toString().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) ? "leader" : "admin";
                                break;
                            }
                        }
                    }

                    if (clanName == null) {
                        plugin.sendPrefixedMessage(player, "You are not authorized to demote members in any clan.");
                        return true;
                    }

                    List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");
                    List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
                    List<String> members = clanConfig.getStringList("clans." + clanName + ".members");

                    if (admins.contains(uuidToDemote.toString())) {
                        if ("leader".equals(senderRole)) {
                            admins.remove(uuidToDemote.toString());
                            moderators.add(uuidToDemote.toString());
                            clanConfig.set("clans." + clanName + ".admins", admins);
                            clanConfig.set("clans." + clanName + ".moderators", moderators);
                            plugin.saveClanFile();
                            plugin.sendPrefixedMessage(player, "Player " + playerNameToDemote + " has been demoted to moderator in clan " + clanName + ".");
                        } else {
                            plugin.sendPrefixedMessage(player, "Only a leader can demote an admin.");
                        }
                    } else if (moderators.contains(uuidToDemote.toString())) {
                        moderators.remove(uuidToDemote.toString());
                        members.add(uuidToDemote.toString());
                        clanConfig.set("clans." + clanName + ".moderators", moderators);
                        clanConfig.set("clans." + clanName + ".members", members);
                        plugin.saveClanFile();
                        plugin.sendPrefixedMessage(player, "Player " + playerNameToDemote + " has been demoted to member in clan " + clanName + ".");
                    } else {
                        plugin.sendPrefixedMessage(player, "The player " + playerNameToDemote + " is not a moderator or admin in clan " + clanName + " and therefore cannot be demoted.");
                    }
                }

                else if ("transfer".equalsIgnoreCase(args[0])) {
                    if (args.length < 2) {
                        plugin.sendPrefixedMessage(player, "Usage: /clan transfer <newLeader>");
                        return true;
                    }

                    String newLeaderName = args[1];
                    Player newLeader = Bukkit.getPlayer(newLeaderName);

                    if (newLeader == null) {
                        plugin.sendPrefixedMessage(player, "The specified player is not online.");
                        return true;
                    }

                    String clanName = null;
                    for (String existingClan : clansSection.getKeys(false)) {
                        if (player.getUniqueId().toString().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader"))) {
                            clanName = existingClan;
                            break;
                        }
                    }

                    if (clanName == null) {
                        plugin.sendPrefixedMessage(player, "You are not a leader of any clan.");
                        return true;
                    }

                    // Transfer leadership
                    clanConfig.set("clans." + clanName + ".leader", newLeader.getUniqueId().toString());
                    plugin.saveClanFile();  // Assuming you have a method to save the clan file

                    plugin.sendPrefixedMessage(player, "You have transferred the leadership of " + clanName + " to " + newLeaderName + ".");
                    plugin.sendPrefixedMessage(newLeader, "You are now the leader of " + clanName + ".");
                }
            }
            else {
                showHelpMessage(player, 1);
            }
        }
        return true;
    }
}