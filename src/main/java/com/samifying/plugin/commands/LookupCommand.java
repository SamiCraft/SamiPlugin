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

import java.io.IOException;

public class LookupCommand implements CommandExecutor {

    private final SamiPlugin plugin;

    public LookupCommand(SamiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        new Thread(() -> {
            if (args.length == 1) {
                try {
                    Player player = plugin.getServer().getPlayerExact(args[0]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Player is not online");
                        return;
                    }
                    BackendData data = PluginUtils.getBackendData(PluginUtils.fetchBackend(player).getInputStream());
                    double balance = plugin.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
                    sender.sendMessage("Following data was found:\n"
                            + "Player Nickname: " + player.getDisplayName() + ChatColor.RESET + "\n"
                            + "Discord Nickname: " + ChatColor.AQUA + data.getName() + ChatColor.RESET + "\n"
                            + "Balance: " + ChatColor.AQUA + balance + ChatColor.RESET + "\n"
                            + "Discord ID: " + ChatColor.AQUA + data.getId() + ChatColor.RESET + "\n"
                            + "Supporter: " + ChatColor.AQUA + data.isSupporter() + ChatColor.RESET + "\n"
                            + "Staff: " + ChatColor.AQUA + data.isModerator()
                    );
                    return;
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.YELLOW + "Something went wrong");
                }
            }
            sender.sendMessage(ChatColor.YELLOW + "Usage: /lookup <username>");
        }, "PlayerLookup").start();
        return true;
    }
}
