package com.samifying.plugin.commands;

import com.samifying.plugin.SamiPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordCommand implements CommandExecutor, TabCompleter {

    private final List<String> availableCommands;
    private final SamiPlugin plugin;

    public DiscordCommand(SamiPlugin plugin) {
        this.availableCommands = Collections.singletonList("reload");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                dispatchMessage(sender, "Plugin configuration reloaded");
                return true;
            }
            return false;
        }
        dispatchMessage(sender, "Handles Discord and balance integration, developed by Pequla");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return availableCommands.stream()
                    .filter(arg -> StringUtils.startsWithIgnoreCase(arg, args[0]))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private void dispatchMessage(@NotNull CommandSender sender, String message) {
        sender.sendMessage(ChatColor.AQUA + "[SamiPlugin] " + ChatColor.GOLD + message);
    }
}
