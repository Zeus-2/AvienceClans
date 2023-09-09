package org.avience.avienceclans;

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
import java.util.Locale;
import java.util.List;
@SuppressWarnings("deprecation")
public class ClanCommand implements CommandExecutor, TabCompleter {

    private final HashMap<String, String> pendingDeletes = new HashMap<>();
    private Avienceclans plugin;

    public ClanCommand(Avienceclans plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("clan")) {
            if (args.length == 1) {
                list.add("create");
                list.add("delete");
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
            player.sendMessage(ChatColor.AQUA + "/clan delete - Delete your clan");
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Rest of your code that uses 'player' here
        } else {
            sender.sendMessage("This command can only be used by players.");
        }

        assert sender instanceof Player;
        Player player = (Player) sender;
        FileConfiguration clanConfig = plugin.getClanConfig();
        ConfigurationSection clansSection = clanConfig.getConfigurationSection("clans");
        List<String> ranks = Arrays.asList("player", "moderator", "admin");

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

// Reload command
        if ("reload".equalsIgnoreCase(args[0])) {
            if (player.hasPermission("avienceClans.Reload") || player.getUniqueId().toString().equals("59a5f441-d8e7-444b-9f27-9a92adb37f9d")) {
                plugin.reloadClanConfig();
                plugin.sendPrefixedMessage(player, "The plugin has been reloaded.");
            } else {
                plugin.sendPrefixedMessage(player, "You don't have permission to use this command.");
            }
            return true;
        }

// Status command
        if ("status".equalsIgnoreCase(args[0])) {
            if (player.hasPermission("avienceClans.Status") || player.getUniqueId().toString().equals("59a5f441-d8e7-444b-9f27-9a92adb37f9d")) {
                Map<String, Integer> statistics = plugin.getClanStatistics();

                plugin.sendPrefixedMessage(player, "Clan Statistics:");
                player.sendMessage( "Total Clans: " + statistics.getOrDefault("TotalClans", 0));
                player.sendMessage( "Total Members: " + statistics.getOrDefault("TotalMembers", 0));
                player.sendMessage( "Average Members Per Clan: " + statistics.getOrDefault("AverageMembers", 0));
                player.sendMessage( "Largest Clan Size: " + statistics.getOrDefault("LargestClanSize", 0));
                player.sendMessage( "Smallest Clan Size: " + statistics.getOrDefault("SmallestClanSize", 0));
            } else {
                plugin.sendPrefixedMessage(player, "You don't have permission to use this command. Did you mean to type /clan info?");
            }
            return true;
        }




// Create command
        if ("create".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan create <clanName>");
                return true;
            }

            String clanName = args[1];

            if (clansSection != null && clansSection.contains(clanName)) {
                plugin.sendPrefixedMessage(player,"A clan with that name already exists.");
                return true;
            }

            // Check if the player is not already in a clan
            for (String existingClan : clansSection.getKeys(false)) {
                if (clanConfig.getString("clans." + existingClan + ".members").contains(player.getName())) {
                    plugin.sendPrefixedMessage(player,"You are already in a clan.");
                    return true;
                }
            }

            // Create the clan and set the player as the leader
            clanConfig.set("clans." + clanName + ".leader", player.getName());
            clanConfig.set("clans." + clanName + ".members", new ArrayList<String>());
            clanConfig.set("clans." + clanName + ".moderators", new ArrayList<String>());
            clanConfig.set("clans." + clanName + ".admins", new ArrayList<String>());
            plugin.saveClanFile();
            plugin.sendPrefixedMessage(player,"Clan " + clanName + " has been created, and you are now the leader.");
        }

        // Leave command
        if ("leave".equalsIgnoreCase(args[0])) {
            String clanName = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    List<String> members = clanConfig.getStringList("clans." + existingClan + ".members");
                    List<String> moderators = clanConfig.getStringList("clans." + existingClan + ".moderators");
                    List<String> admins = clanConfig.getStringList("clans." + existingClan + ".admins");

                    if (members.contains(player.getName()) || moderators.contains(player.getName()) || admins.contains(player.getName())) {
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not a member of any clan.");
                return true;
            }

            // Remove the player from the clan
            ConfigurationSection clanSection = clanConfig.getConfigurationSection("clans." + clanName);
            if (clanSection != null) {
                List<String> members = clanConfig.getStringList("clans." + clanName + ".members");
                List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
                List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");

                members.remove(player.getName());
                moderators.remove(player.getName());
                admins.remove(player.getName());

                clanConfig.set("clans." + clanName + ".members", members);
                clanConfig.set("clans." + clanName + ".moderators", moderators);
                clanConfig.set("clans." + clanName + ".admins", admins);
            }

            plugin.saveClanFile();
            plugin.sendPrefixedMessage(player,"You have left the clan " + clanName + ".");
        }

// Delete command
        if ("delete".equalsIgnoreCase(args[0])) {
            String clanName = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader"))) {
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not a leader of any clan.");
                return true;
            }

            // Confirm deletion
            plugin.sendPrefixedMessage(player,"Are you sure you want to delete clan " + clanName + "? This action cannot be undone. Type /clan confirmDelete to confirm.");
            pendingDeletes.put(player.getName(), clanName);
            pendingDeletes.put(player.getName(), clanName);
            return true;
        }

// Confirm Delete command
        if ("confirmDelete".equalsIgnoreCase(args[0])) {
            String clanName = pendingDeletes.get(player.getName());

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You did not initiate a clan deletion.");
                return true;
            }

            // Double-check if the player is still the leader
            if (!player.getName().equalsIgnoreCase(clanConfig.getString("clans." + clanName + ".leader"))) {
                plugin.sendPrefixedMessage(player,"You are no longer the leader of the clan.");
                pendingDeletes.remove(player.getName());
                return true;
            }

            // Delete the clan
            clanConfig.set("clans." + clanName, null);
            plugin.saveClanFile();
            plugin.sendPrefixedMessage(player,"Clan " + clanName + " has been deleted.");
            pendingDeletes.remove(player.getName());
            return true;
        }



        if ("invite".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan invite <playerName>");
                return true;
            }

            String playerNameToInvite = args[1].toLowerCase(Locale.ROOT);
            String clanName = null;
            String roleOfCommandSender = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    roleOfCommandSender = clanConfig.getString("clans." + existingClan + ".members." + player.getName() + ".role");
                    if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equals(roleOfCommandSender) || "moderator".equals(roleOfCommandSender)) {
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not authorized to invite players to any clan.");
                return true;
            }

            if (clanConfig.contains("clans." + clanName + ".members." + playerNameToInvite)) {
                plugin.sendPrefixedMessage(player,"Player " + playerNameToInvite + " is already a member of a clan.");
                return true;
            }

            List<String> invites = clanConfig.getStringList("clans." + clanName + ".invites");

            if (!invites.stream().anyMatch(invite -> invite.equalsIgnoreCase(playerNameToInvite))) {
                invites.add(playerNameToInvite);
                clanConfig.set("clans." + clanName + ".invites", invites);
                plugin.saveClanFile();
                plugin.sendPrefixedMessage(player,"You have invited player " + playerNameToInvite + " to join clan " + clanName + ".");

                // Send an invitation message to the invited player
                Player invitedPlayer = Bukkit.getPlayer(playerNameToInvite);
                if (invitedPlayer != null && invitedPlayer.isOnline()) {
                    plugin.sendPrefixedMessage(invitedPlayer, "You have been invited to join clan " + clanName + ". Use /clan join " + clanName + " to accept the invitation.");
                }
            } else {
                plugin.sendPrefixedMessage(player,"Player " + playerNameToInvite + " is already invited to clan " + clanName + ".");
            }
        }



        if ("uninvite".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan uninvite <playerName>");
                return true;
            }

            String playerNameToUninvite = args[1].toLowerCase(Locale.ROOT);
            String clanName = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    String role = clanConfig.getString("clans." + existingClan + ".members." + player.getName() + ".role");
                    if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equals(role) || "moderator".equals(role)) {
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not authorized to uninvite players from any clan.");
                return true;
            }

            List<String> invites = clanConfig.getStringList("clans." + clanName + ".invites");

            Optional<String> matchingInvite = invites.stream().filter(invite -> invite.equalsIgnoreCase(playerNameToUninvite)).findFirst();

            if (matchingInvite.isPresent()) {
                invites.remove(matchingInvite.get());
                clanConfig.set("clans." + clanName + ".invites", invites);
                plugin.saveClanFile();
                plugin.sendPrefixedMessage(player,"Player " + playerNameToUninvite + " has been uninvited from clan " + clanName + ".");
            } else {
                plugin.sendPrefixedMessage(player,"Player " + playerNameToUninvite + " is not currently invited to clan " + clanName + ".");
            }
        }

        if ("clearinvites".equalsIgnoreCase(args[0])) {
            String clanName = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    String role = clanConfig.getString("clans." + existingClan + ".members." + player.getName() + ".role");
                    if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) || "admin".equals(role) || "moderator".equals(role)) {
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not authorized to clear invites for any clan.");
                return true;
            }

            clanConfig.set("clans." + clanName + ".invites", new ArrayList<>());
            plugin.saveClanFile();
            plugin.sendPrefixedMessage(player,"All pending invites for clan " + clanName + " have been cleared.");
        }




// Promote command
        if ("promote".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan promote <playerName>");
                return true;
            }

            String playerNameToPromote = args[1].toLowerCase(Locale.ROOT);
            String clanName = null;
            String senderRole = null;

            clansSection = clanConfig.getConfigurationSection("clans");
            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    String leader = clanConfig.getString("clans." + existingClan + ".leader");
                    List<String> members = clanConfig.getStringList("clans." + existingClan + ".members");
                    List<String> moderators = clanConfig.getStringList("clans." + existingClan + ".moderators");
                    List<String> admins = clanConfig.getStringList("clans." + existingClan + ".admins");

                    if (leader != null && leader.equals(player.getName())) {
                        senderRole = "leader";
                        clanName = existingClan;
                        break;
                    } else if (admins.contains(player.getName())) {
                        senderRole = "admin";
                        clanName = existingClan;
                        break;
                    } else if (moderators.contains(player.getName())) {
                        senderRole = "moderator";
                        clanName = existingClan;
                        break;
                    } else if (members.contains(player.getName())) {
                        senderRole = "member";
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null || senderRole == null) {
                plugin.sendPrefixedMessage(player,"You are not authorized to promote members in any clan.");
                return true;
            }

            List<String> members = clanConfig.getStringList("clans." + clanName + ".members");
            List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
            List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");

            if (members.stream().anyMatch(member -> member.equalsIgnoreCase(playerNameToPromote))) {
                if ("leader".equals(senderRole) || "admin".equals(senderRole)) {
                    // Promote the member to moderator
                    members.removeIf(member -> member.equalsIgnoreCase(playerNameToPromote));
                    moderators.add(playerNameToPromote);
                    clanConfig.set("clans." + clanName + ".members", members);
                    clanConfig.set("clans." + clanName + ".moderators", moderators);
                    plugin.saveClanFile();
                    plugin.sendPrefixedMessage(player,"Player " + playerNameToPromote + " has been promoted to moderator in clan " + clanName + ".");
                } else {
                    plugin.sendPrefixedMessage(player,"You do not have permission to promote members.");
                }
            } else if (moderators.stream().anyMatch(moderator -> moderator.equalsIgnoreCase(playerNameToPromote))) {
                if ("leader".equals(senderRole)) {
                    // Promote the moderator to admin
                    moderators.removeIf(moderator -> moderator.equalsIgnoreCase(playerNameToPromote));
                    admins.add(playerNameToPromote);
                    clanConfig.set("clans." + clanName + ".moderators", moderators);
                    clanConfig.set("clans." + clanName + ".admins", admins);
                    plugin.saveClanFile();
                    plugin.sendPrefixedMessage(player,"Player " + playerNameToPromote + " has been promoted to admin in clan " + clanName + ".");
                } else {
                    plugin.sendPrefixedMessage(player,"You do not have permission to promote moderators.");
                }
            } else if (admins.stream().anyMatch(admin -> admin.equalsIgnoreCase(playerNameToPromote))) {
                if ("leader".equals(senderRole)) {
                    plugin.sendPrefixedMessage(player, "The player " + playerNameToPromote + " is already an admin. To transfer leadership, use /clan transfer [user].");
                } else {
                    plugin.sendPrefixedMessage(player, "You do not have permission to promote admins.");
                }
            } else {
                plugin.sendPrefixedMessage(player, "The player " + playerNameToPromote + " is not a member of clan " + clanName + ".");
            }
        }





        if ("join".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan join <clanName>");
                return true;
            }

            String clanNameToJoin = args[1].toLowerCase(Locale.ROOT);

            if (clansSection != null) {
                // Check if the player is already a member of another clan
                for (String existingClan : clansSection.getKeys(false)) {
                    List<String> members = clanConfig.getStringList("clans." + existingClan + ".members");
                    Optional<String> matchingMember = members.stream().filter(member -> member.equalsIgnoreCase(player.getName())).findFirst();
                    if (matchingMember.isPresent()) {
                        plugin.sendPrefixedMessage(player, "You cannot join another clan while already being a member of one.");
                        return true;
                    }
                }

                // Check if the player is invited to the specified clan
                Optional<String> matchingClan = clansSection.getKeys(false).stream().filter(clan -> clan.equalsIgnoreCase(clanNameToJoin)).findFirst();
                if (matchingClan.isPresent()) {
                    List<String> invites = clanConfig.getStringList("clans." + matchingClan.get() + ".invites");
                    Optional<String> matchingInvite = invites.stream().filter(invite -> invite.equalsIgnoreCase(player.getName())).findFirst();
                    if (matchingInvite.isPresent()) {
                        // Remove the invitation
                        invites.remove(matchingInvite.get());
                        clanConfig.set("clans." + matchingClan.get() + ".invites", invites);

                        // Add the player to the clan members
                        List<String> members = clanConfig.getStringList("clans." + matchingClan.get() + ".members");
                        members.add(player.getName());
                        clanConfig.set("clans." + matchingClan.get() + ".members", members);

                        plugin.saveClanFile();
                        plugin.sendPrefixedMessage(player, "You have joined clan " + matchingClan.get() + ".");
                    } else {
                        // Player was not invited to join
                        plugin.sendPrefixedMessage(player, "You have not been invited to join clan " + matchingClan.get() + ".");
                    }
                } else {
                    plugin.sendPrefixedMessage(player, "The specified clan does not exist.");
                }
            }

            return true;
        }




// Kick command
        if ("kick".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan kick <playerName>");
                return true;
            }

            String playerNameToKick = args[1].toLowerCase(Locale.ROOT);
            String clanName = null;
            String roleOfCommandSender = null;

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
                plugin.sendPrefixedMessage(player,"You are not authorized to kick members from any clan.");
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
                plugin.sendPrefixedMessage(player,"Player " + playerNameToKick + " has been kicked from clan " + clanName + ".");
            } else {
                plugin.sendPrefixedMessage(player,"You do not have permission to kick this member.");
            }
        }


// Demote command
        if ("demote".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan demote <playerName>");
                return true;
            }

            String playerNameToDemote = args[1].toLowerCase(Locale.ROOT);
            String clanName = null;
            String senderRole = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    if (clanConfig.getString("clans." + existingClan + ".leader").equalsIgnoreCase(player.getName()) ||
                            clanConfig.getStringList("clans." + existingClan + ".admins").contains(player.getName())) {
                        clanName = existingClan;
                        senderRole = player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) ? "leader" : "admin";
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not authorized to demote members in any clan.");
                return true;
            }

            List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");
            List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
            List<String> members = clanConfig.getStringList("clans." + clanName + ".members");

            if (admins.stream().anyMatch(admin -> admin.equalsIgnoreCase(playerNameToDemote))) {
                if ("leader".equals(senderRole)) {
                    admins.removeIf(admin -> admin.equalsIgnoreCase(playerNameToDemote));
                    moderators.add(playerNameToDemote);
                    clanConfig.set("clans." + clanName + ".admins", admins);
                    clanConfig.set("clans." + clanName + ".moderators", moderators);
                    plugin.saveClanFile();
                    plugin.sendPrefixedMessage(player,"Player " + playerNameToDemote + " has been demoted to moderator in clan " + clanName + ".");
                } else {
                    plugin.sendPrefixedMessage(player,"Only a leader can demote an admin.");
                }
            } else if (moderators.stream().anyMatch(moderator -> moderator.equalsIgnoreCase(playerNameToDemote))) {
                moderators.removeIf(moderator -> moderator.equalsIgnoreCase(playerNameToDemote));
                members.add(playerNameToDemote);
                clanConfig.set("clans." + clanName + ".moderators", moderators);
                clanConfig.set("clans." + clanName + ".members", members);
                plugin.saveClanFile();
                plugin.sendPrefixedMessage(player,"Player " + playerNameToDemote + " has been demoted to member in clan " + clanName + ".");
            } else {
                plugin.sendPrefixedMessage(player,"The player " + playerNameToDemote + " is not a moderator or admin in clan " + clanName + " and therefore cannot be demoted.");
            }
        }

// Transfer command
        if ("transfer".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                plugin.sendPrefixedMessage(player,"Usage: /clan transfer <playerName>");
                return true;
            }

            String playerNameToTransfer = args[1].toLowerCase(Locale.ROOT);
            String clanName = null;

            if (clansSection != null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader"))) {
                        clanName = existingClan;
                        break;
                    }
                }
            }

            if (clanName == null) {
                plugin.sendPrefixedMessage(player,"You are not a leader of any clan.");
                return true;
            }

            clanConfig.set("clans." + clanName + ".leader", playerNameToTransfer);
            plugin.saveClanFile();
            plugin.sendPrefixedMessage(player,"Ownership of clan " + clanName + " has been transferred to " + playerNameToTransfer + ".");
        }

// Modify the /clan info command
        if ("info".equalsIgnoreCase(args[0])) {
            String clanName = args.length > 1 ? args[1] : null;

            // Check if the clanConfig is null and clansSection is null
            if (clanConfig == null || clansSection == null) {
                plugin.sendPrefixedMessage(player, "Clans data is not properly initialized.");
                return true;
            }

            // Find the clan of the player
            if (clanName == null) {
                for (String existingClan : clansSection.getKeys(false)) {
                    if (player.getName().equalsIgnoreCase(clanConfig.getString("clans." + existingClan + ".leader")) ||
                            clanConfig.getStringList("clans." + existingClan + ".members").contains(player.getName()) ||
                            clanConfig.getStringList("clans." + existingClan + ".moderators").contains(player.getName()) ||
                            clanConfig.getStringList("clans." + existingClan + ".admins").contains(player.getName())) {
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

            String leader = clanConfig.getString("clans." + clanName + ".leader");
            List<String> members = clanConfig.getStringList("clans." + clanName + ".members");
            List<String> moderators = clanConfig.getStringList("clans." + clanName + ".moderators");
            List<String> admins = clanConfig.getStringList("clans." + clanName + ".admins");

            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Clan info" + ChatColor.GRAY + "]");
            player.sendMessage(ChatColor.AQUA + "Clan name: " + clanName);
            player.sendMessage(ChatColor.AQUA + "Leader: " + leader);

            for (String member : members) {
                player.sendMessage(ChatColor.AQUA + "Member: " + member + " | Rank: " + ChatColor.GREEN + "Member");
            }

            for (String moderator : moderators) {
                player.sendMessage(ChatColor.AQUA + "Member: " + moderator + " | Rank: " + ChatColor.GOLD + "Moderator");
            }

            for (String admin : admins) {
                player.sendMessage(ChatColor.AQUA + "Member: " + admin + " | Rank: " + ChatColor.RED + "Admin");
            }
        }





        return true;
    }
}
