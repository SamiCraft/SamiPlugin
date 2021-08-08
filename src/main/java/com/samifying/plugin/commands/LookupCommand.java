package com.samifying.plugin.commands;

import com.samifying.plugin.PluginUtils;
import com.samifying.plugin.SamiPlugin;
import com.samifying.plugin.atributes.BackendData;
import com.samifying.plugin.atributes.BackendError;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;

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
                    HttpURLConnection con = PluginUtils.fetchBackend(plugin.getConfig().getString("backend"), player);
                    if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        BackendError error = PluginUtils.getBackendError(con.getErrorStream());
                        sender.sendMessage(ChatColor.YELLOW + "Got the following error: " + error.getMessage());
                        return;
                    }
                    BackendData data = PluginUtils.getBackendData(con.getInputStream());
                    sender.sendMessage("Following data was found:\n"
                            + "Discord Nickname: " + ChatColor.AQUA + data.getName() + ChatColor.RESET + "\n"
                            + "Supporter: " + ChatColor.AQUA + data.isSupporter() + ChatColor.RESET + "\n"
                            + "Staff: " + ChatColor.AQUA + data.isModerator()
                    );
                    return;
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.YELLOW + "Something went wrong");
                }
            }
            sender.sendMessage("Usage: /lookup <username>");
        }, "PlayerLookup").start();
        return true;
    }
}
