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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LoreCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "sami.plugin.lore";
    private final List<String> availableCommands;
    private final SamiPlugin plugin;

    public LoreCommand(SamiPlugin plugin) {
        this.availableCommands = Arrays.asList("set", "reset");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permissions to use this command");
            return false;
        }

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
            double price = stack.getAmount() * 1500.0;
            if (balance > price) {
                plugin.getEconomy().withdrawPlayer(offline, 1500.0);
                List<String> lore = new ArrayList<>();
                lore.add(generate(args));
                meta.setLore(lore);
                stack.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Successfully changed lore, " + String.format("$%.0f", price) + " was removed from your balance");
                return true;
            }
            player.sendMessage(ChatColor.RED + "You are broke, it costs" + String.format("$%.0f", price) + " to change the lore");
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

    @NotNull
    private String generate(String[] args) {
        return String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
