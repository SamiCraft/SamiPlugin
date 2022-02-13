package com.samifying.plugin.commands;

import com.samifying.plugin.PluginUtils;
import com.samifying.plugin.SamiPlugin;
import com.samifying.plugin.atributes.BackendData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LookupCommand implements CommandExecutor {

    private static final String PERMISSION = "sami.plugin.lookup";
    private final SamiPlugin plugin;

    public LookupCommand(SamiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permissions to use this command");
            return false;
        }
        new Thread(() -> {
            if (args.length == 1) {
                // Getting the player
                Player player = plugin.getServer().getPlayerExact(args[0]);
                if (player == null) {
                    // Player is not online
                    sender.sendMessage(ChatColor.YELLOW + "Player is not online");
                    return;
                }

                // Message formatting
                BackendData data = plugin.getPlayers().get(player.getUniqueId());
                double balance = plugin.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
                sender.sendMessage("Following data was found:\n"
                        + "Player Nickname: " + player.getDisplayName() + ChatColor.RESET + "\n"
                        + "Discord Name: " + ChatColor.AQUA + data.getName() + ChatColor.RESET + "\n"
                        + "Discord Nickname: " + ChatColor.AQUA + data.getNickname() + ChatColor.RESET + "\n"
                        + "Balance: " + ChatColor.AQUA + String.format("$%.0f", balance) + ChatColor.RESET + "\n"
                        + "Discord ID: " + ChatColor.AQUA + data.getId() + ChatColor.RESET + "\n"
                        + "Supporter: " + ChatColor.AQUA + data.isSupporter() + ChatColor.RESET + "\n"
                        + "Staff: " + ChatColor.AQUA + data.isModerator()
                );
                return;
            }
            sender.sendMessage(ChatColor.YELLOW + "Usage: /lookup <username>");
        }, "PlayerLookup").start();
        return true;
    }
}
