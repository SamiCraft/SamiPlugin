package com.samifying.plugin;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.samifying.plugin.atributes.BackendData;
import com.samifying.plugin.commands.DiscordCommand;
import com.samifying.plugin.commands.LookupCommand;
import com.samifying.plugin.commands.LoreCommand;
import com.samifying.plugin.listeners.CustomMobs;
import com.samifying.plugin.listeners.DiscordLogs;
import com.samifying.plugin.listeners.LoginEvent;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.time.Instant;
import java.util.*;

public final class SamiPlugin extends JavaPlugin {

    private final WebhookClient webhook;
    private final Map<UUID, BackendData> players;
    private Economy economy;
    private LuckPerms perms;

    public SamiPlugin() {
        saveDefaultConfig();
        this.webhook = WebhookClient.withUrl(Objects.requireNonNull(getConfig().getString("webhook")));
        this.players = new HashMap<>();
    }

    @Override
    public void onEnable() {
        // Getting the economy ready
        getLogger().info("Checking if Vault and LuckPerms are present");
        PluginManager manager = getServer().getPluginManager();
        if (!dependencySetup()) {
            getLogger().warning("This plugin requires Vault and LuckPerms");
            getLogger().warning("Plugin will exit");
            manager.disablePlugin(this);
            return;
        }

        // Registering events
        getLogger().info("Registering events");
        manager.registerEvents(new CustomMobs(), this);
        manager.registerEvents(new LoginEvent(this), this);
        manager.registerEvents(new DiscordLogs(this), this);

        // Registering commands
        Objects.requireNonNull(getCommand("discord")).setExecutor(new DiscordCommand(this));
        Objects.requireNonNull(getCommand("lookup")).setExecutor(new LookupCommand(this));
        Objects.requireNonNull(getCommand("lore")).setExecutor(new LoreCommand(this));

        // Getting the balance endpoint ready
        getLogger().info("Initializing balance endpoint");
        Spark.port(getConfig().getInt("port"));
        Spark.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET");
        });
        Spark.get("v1/balance", "application/json", (request, response) -> {
            // Retrieving the uuid
            UUID uuid = getUniqueIdFromDiscordId(request.queryParams("id"));
            if (uuid != null) {
                // Retrieving the player
                for (OfflinePlayer player : getServer().getOfflinePlayers()) {
                    if (Objects.equals(uuid, player.getUniqueId())) {
                        // its a match
                        double balance = economy.getBalance(player);
                        return String.format("$%.0f", balance);
                    }
                }
            }
            return "Unknown";
        });

        // Discord notification
        new Thread(() -> {
            webhook.send(new WebhookEmbedBuilder()
                    .setColor(getConfig().getInt("color.system"))
                    .setTitle(new WebhookEmbed.EmbedTitle("**SERVER STARTING**", null))
                    .setDescription("Plugin loaded, please wait")
                    .setFooter(getEmbedFooter())
                    .setTimestamp(Instant.now())
                    .build());
            getLogger().info("Plugin loaded, webhook message dispatched");
        },"PluginEnabledDispatcher").start();
    }

    @Override
    public void onDisable() {
        // Discord notification
        webhook.send(new WebhookEmbedBuilder()
                .setColor(getConfig().getInt("color.system"))
                .setTitle(new WebhookEmbed.EmbedTitle("**SERVER STOPPED**", null))
                .setDescription("Server stopped successfully")
                .setFooter(getEmbedFooter())
                .setTimestamp(Instant.now())
                .build());
        getLogger().info("Server stopping message dispatched");

        // Closing Discord Webhook connection
        webhook.close();
        getLogger().info("Plugin unloaded, webhook connection closed");
    }

    public World getMainServerWorld() {
        List<World> worlds = getServer().getWorlds();
        return worlds.get(0);
    }

    public WebhookClient getWebhook() {
        return webhook;
    }

    public synchronized Map<UUID, BackendData> getPlayers() {
        return players;
    }

    private boolean dependencySetup() {
        // Looking for Vault in registered plugins
        PluginManager pm = getServer().getPluginManager();
        if (pm.getPlugin("Vault") == null || pm.getPlugin("LuckPerms") == null) {
            return false;
        }

        ServicesManager sm = getServer().getServicesManager();

        // Registering the Vault and LuckPerms providers
        RegisteredServiceProvider<Economy> rspv = sm.getRegistration(Economy.class);
        RegisteredServiceProvider<LuckPerms> rspl = sm.getRegistration(LuckPerms.class);
        if (rspv == null || rspl == null) {
            return false;
        }
        // Received an economy and luck perms instance
        economy = rspv.getProvider();
        perms = rspl.getProvider();
        return true;
    }

    // Gets the UUID from the players map based on the Discord id
    @Nullable
    private UUID getUniqueIdFromDiscordId(String id) {
        for (Map.Entry<UUID, BackendData> entry : players.entrySet()) {
            if (Objects.equals(id, entry.getValue().getId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void sendCustomisedEmbed(@NotNull Player player, WebhookEmbed embed) {
        webhook.send(new WebhookMessageBuilder()
                .setUsername(player.getName())
                .setAvatarUrl(PluginConstants.AVATAR_API.replace("%uuid%", PluginUtils.trimUniqueId(player)))
                .addEmbeds(embed)
                .build());
    }

    public LuckPerms getPerms() {
        return perms;
    }

    public Economy getEconomy() {
        return economy;
    }

    @NotNull
    public WebhookEmbed.EmbedFooter getEmbedFooter() {
        return new WebhookEmbed.EmbedFooter(getName() + " | Developed by Pequla#3038", null);
    }
}
