package com.samifying.plugin.commands;

import com.samifying.plugin.SamiPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LoreCommand implements CommandExecutor, TabCompleter {

    private final List<String> availableCommands;
    private final SamiPlugin plugin;

    public LoreCommand(SamiPlugin plugin) {
        this.availableCommands = Arrays.asList("set", "reset");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You have to be a player");
            return false;
        }

        ItemStack stack = player.getInventory().getItemInMainHand();
        ItemMeta meta = stack.getItemMeta();

        if (args.length == 1 && args[0].equalsIgnoreCase(availableCommands.get(1))) {
            if (meta == null) {
                player.sendMessage(ChatColor.RED + "You need to hold an item");
                return false;
            }
            meta.setLore(Collections.emptyList());
            stack.setItemMeta(meta);
            player.sendMessage(ChatColor.GREEN + "Successfully reset lore");
            return true;
        }

        if (args.length > 1 && args[0].equalsIgnoreCase(availableCommands.get(0))) {
            if (meta == null) {
                player.sendMessage(ChatColor.RED + "You need to hold an item");
                return false;
            }
            OfflinePlayer offline = Bukkit.getOfflinePlayer(player.getUniqueId());
            double balance = plugin.getEconomy().getBalance(offline);
            if (balance > 1500.0) {
                plugin.getEconomy().withdrawPlayer(offline, 1500.0);
                meta.setLore(Arrays.asList("This is the first line", "Hello from 2nd one"));
                stack.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Successfully changed lore, $1500 was removed from your balance");
                return true;
            }
            player.sendMessage(ChatColor.RED + "You are broke, it costs $1500 to change the lore");
            return false;
        }
        player.sendMessage(ChatColor.GOLD + "Usage: /lore set <text> or /lore reset");
        return false;
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
}
