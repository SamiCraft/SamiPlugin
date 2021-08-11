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
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SamiPlugin extends JavaPlugin {

    public static final String SAMI_USER_ID = "179261209529417729";
    public static final String PEQULA_USER_ID = "358236836113547265";

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
            return "Player offline";
        });

        // Discord notification
        new Thread(() -> {
            sendSystemEmbed("Server starting");
            getLogger().info("Plugin loaded, webhook message dispatched");
        }, "PluginEnabledDispatcher").start();
    }

    @Override
    public void onDisable() {
        // Discord notification
        sendSystemEmbed("Server stopped");
        getLogger().info("Server stopping message dispatched");

        // Closing Discord Webhook connection
        webhook.close();
        getLogger().info("Plugin unloaded, webhook connection closed");
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

    public void sendWebhookEmbed(@NotNull Player player, @NotNull BackendData data, int color,
                                 String title, @NotNull WebhookEmbedBuilder builder) {
        webhook.send(new WebhookMessageBuilder()
                .setUsername(player.getName())
                .setAvatarUrl("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve")
                .addEmbeds(builder
                        .setColor(color)
                        .setAuthor(new WebhookEmbed.EmbedAuthor(data.getNickname(), data.getAvatar(), null))
                        .setDescription("**" + title + "**")
                        .setFooter(new WebhookEmbed.EmbedFooter(data.getId(), null))
                        .setTimestamp(Instant.now())
                        .build())
                .build());
    }

    public void sendSystemEmbed(String message) {
        webhook.send(new WebhookEmbedBuilder()
                .setColor(getConfig().getInt("color.system"))
                .setDescription("**" + message + "**")
                .setTimestamp(Instant.now())
                .build());
    }

    public LuckPerms getPerms() {
        return perms;
    }

    public Economy getEconomy() {
        return economy;
    }
}
