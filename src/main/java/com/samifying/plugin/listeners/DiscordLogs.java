package com.samifying.plugin.listeners;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.samifying.plugin.SamiPlugin;
import com.samifying.plugin.atributes.BackendData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public class DiscordLogs implements Listener {

    private final SamiPlugin plugin;
    private final FileConfiguration config;

    public DiscordLogs(@NotNull SamiPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onWorldLoadEvent(@NotNull WorldLoadEvent event) {
        new Thread(() -> {
            if (plugin.getMainServerWorld().equals(event.getWorld())) {
                plugin.getWebhook().send(new WebhookEmbedBuilder()
                        .setColor(config.getInt("color.system"))
                        .setTitle(new WebhookEmbed.EmbedTitle("**LOADING THE WORLD**", null))
                        .setDescription("Loading the world, please wait")
                        .setFooter(plugin.getEmbedFooter())
                        .setTimestamp(Instant.now())
                        .build());
                plugin.getLogger().info("World load message dispatched");
            }
        }, "WorldLoadDispatcher").start();
    }

    @EventHandler
    public void onServerLoadEvent(@NotNull ServerLoadEvent event) {
        new Thread(() -> {
            if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
                plugin.getWebhook().send(new WebhookEmbedBuilder()
                        .setColor(config.getInt("color.system"))
                        .setTitle(new WebhookEmbed.EmbedTitle("**SERVER LOADED**", null))
                        .setDescription("Server has successfully loaded, players can join now")
                        .setFooter(plugin.getEmbedFooter())
                        .setTimestamp(Instant.now())
                        .build());
                plugin.getLogger().info("Server loaded message dispatched");
            }
        }, "ServerLoadDispatcher").start();
    }

    @EventHandler
    public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {
        new Thread(() -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            BackendData data = plugin.getPlayers().get(uuid);
            if (data != null) {
                plugin.getPlayers().remove(uuid, data);
                Server server = plugin.getServer();
                String online = (server.getOnlinePlayers().size() - 1) + "/" + server.getMaxPlayers();
                plugin.sendCustomisedEmbed(player, new WebhookEmbedBuilder()
                        .setColor(config.getInt("color.leave"))
                        .setTitle(new WebhookEmbed.EmbedTitle("**" + player.getName().toUpperCase() + " LEFT**", null))
                        .setAuthor(new WebhookEmbed.EmbedAuthor(data.getNickname(), data.getAvatar(), null))
                        .setDescription(player.getName() + " just left the game")
                        .addField(new WebhookEmbed.EmbedField(false, "Currently online:", online))
                        .setFooter(new WebhookEmbed.EmbedFooter(data.getId(), null))
                        .setTimestamp(Instant.now())
                        .build());
                plugin.getLogger().info("Player leave message dispatched");
            }
        }, "PlayerQuitDispatcher").start();
    }

    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {
        new Thread(() -> {
            Player player = event.getEntity();
            UUID uuid = player.getUniqueId();
            BackendData data = plugin.getPlayers().get(uuid);
            if (data != null) {
                plugin.sendCustomisedEmbed(player, new WebhookEmbedBuilder()
                        .setColor(config.getInt("color.death"))
                        .setTitle(new WebhookEmbed.EmbedTitle("**" + player.getName().toUpperCase() + " DIED**", null))
                        .setAuthor(new WebhookEmbed.EmbedAuthor(data.getNickname(), data.getAvatar(), null))
                        .setDescription(event.getDeathMessage())
                        .setFooter(new WebhookEmbed.EmbedFooter(data.getId(), null))
                        .setTimestamp(Instant.now())
                        .build());
                plugin.getLogger().info("Player death message dispatched");
            }
        }, "PlayerDeathDispatcher").start();
    }

    @EventHandler
    public void onPlayerAdvancementDoneEvent(@NotNull PlayerAdvancementDoneEvent event) {
        new Thread(() -> {
            String[] advancement = event.getAdvancement().getKey().getKey().split("/");
            // Recipes should not be displayed
            if (advancement[0].equalsIgnoreCase("recipes")) {
                return;
            }
            String category = StringUtils.capitalize(advancement[0]);
            String name = StringUtils.capitalize(advancement[1].replace("_", " "));

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            BackendData data = plugin.getPlayers().get(uuid);
            if (data != null) {
                plugin.sendCustomisedEmbed(player, new WebhookEmbedBuilder()
                        .setColor(config.getInt("color.advancement"))
                        .setTitle(new WebhookEmbed.EmbedTitle("**" + player.getName().toUpperCase() + " MADE AN ADVANCEMENT**", null))
                        .setAuthor(new WebhookEmbed.EmbedAuthor(data.getNickname(), data.getAvatar(), null))
                        .addField(new WebhookEmbed.EmbedField(false, "Category:", category))
                        .addField(new WebhookEmbed.EmbedField(false, "Name:", name))
                        .setFooter(new WebhookEmbed.EmbedFooter(data.getId(), null))
                        .setTimestamp(Instant.now())
                        .build());
                plugin.getLogger().info("Player death message dispatched");
            }
        }, "PlayerAdvancementDispatcher").start();
    }
}
