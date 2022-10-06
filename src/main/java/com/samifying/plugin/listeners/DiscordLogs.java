package com.samifying.plugin.listeners;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.samifying.plugin.PluginUtils;
import com.samifying.plugin.SamiPlugin;
import com.samifying.plugin.models.BackendData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
            if (plugin.getServer().getWorlds().get(0).equals(event.getWorld())) {
                plugin.sendSystemEmbed("Loading the world");
                plugin.getLogger().info("World load message dispatched");
            }
        }, "WorldLoadDispatcher").start();
    }

    @EventHandler
    public void onServerLoadEvent(@NotNull ServerLoadEvent event) {
        new Thread(() -> {
            if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
                plugin.sendSystemEmbed("Server loaded");
                plugin.getLogger().info("Server loaded message dispatched");
            }
        }, "ServerLoadDispatcher").start();
    }

    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {
        new Thread(() -> {
            Player player = event.getPlayer();
            BackendData data = plugin.getPlayers().get(player.getUniqueId());
            if (data != null) {
                int color = config.getInt("color.join");
                if (data.getId().equals(SamiPlugin.SAMI_USER_ID)) {
                    color = config.getInt("color.sami");
                }
                plugin.sendWebhookEmbed(player, data, color,
                        PluginUtils.sanitize(player.getName() + " joined the game"),
                        new WebhookEmbedBuilder().addField(currentlyOnline(event))
                );
                plugin.getLogger().info("Player join message dispatched");
            }
        }, "PlayerJoinDispatcher").start();
    }

    @EventHandler
    public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {
        new Thread(() -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            BackendData data = plugin.getPlayers().get(uuid);
            if (data != null) {
                plugin.getPlayers().remove(uuid, data);
                plugin.sendWebhookEmbed(player, data, config.getInt("color.leave"),
                        PluginUtils.sanitize(player.getName() + " left the game"),
                        new WebhookEmbedBuilder().addField(currentlyOnline(event))
                );
                plugin.getLogger().info("Player leave message dispatched");
            }
        }, "PlayerQuitDispatcher").start();
    }

    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {
        new Thread(() -> {
            Player player = event.getEntity();
            BackendData data = plugin.getPlayers().get(player.getUniqueId());
            if (data != null) {
                plugin.sendWebhookEmbed(player, data, config.getInt("color.death"),
                        PluginUtils.sanitize(Objects.requireNonNull(event.getDeathMessage())),
                        new WebhookEmbedBuilder()
                );
                plugin.getLogger().info("Player death message dispatched");
            }
        }, "PlayerDeathDispatcher").start();
    }

    @EventHandler
    public void onPlayerAdvancementDoneEvent(@NotNull PlayerAdvancementDoneEvent event) {
        if (!config.getBoolean("enable.advancements")) return;
        new Thread(() -> {
            String[] advancement = event.getAdvancement().getKey().getKey().split("/");
            // Recipes should not be displayed
            if (advancement[0].equalsIgnoreCase("recipes")) {
                return;
            }
            String category = StringUtils.capitalize(advancement[0]);
            String name = StringUtils.capitalize(advancement[1].replace("_", " "));
            String format = String.format("%s: %s", category, name);

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            BackendData data = plugin.getPlayers().get(uuid);
            if (data != null) {
                plugin.sendWebhookEmbed(player, data, config.getInt("color.advancement"),
                        PluginUtils.sanitize(player.getName() + " made an advancement"),
                        new WebhookEmbedBuilder()
                                .addField(new WebhookEmbed.EmbedField(false, "Advancement:", format))
                );
                plugin.getLogger().info("Player advancement made message dispatched");
            }
        }, "PlayerAdvancementDispatcher").start();
    }

    @NotNull
    private WebhookEmbed.EmbedField currentlyOnline(PlayerEvent event) {
        Server server = plugin.getServer();
        int i = (event instanceof PlayerQuitEvent) ? 1 : 0;
        String online = String.format("%s/%s", server.getOnlinePlayers().size() - i, server.getMaxPlayers());
        return new WebhookEmbed.EmbedField(false, "Online:", online);
    }
}
