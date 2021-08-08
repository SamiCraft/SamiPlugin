package com.samifying.plugin.listeners;


import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.samifying.plugin.PluginConstants;
import com.samifying.plugin.PluginUtils;
import com.samifying.plugin.SamiPlugin;
import com.samifying.plugin.atributes.BackendData;
import com.samifying.plugin.atributes.BackendError;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class LoginEvent implements Listener {

    private final SamiPlugin plugin;
    private final Logger logger;

    public LoginEvent(@NotNull SamiPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoginEvent(@NotNull PlayerLoginEvent event) {
        Server server = plugin.getServer();
        Player player = event.getPlayer();

        // Whitelist is on and the player is not whitelisted
        if (server.hasWhitelist() && !player.isWhitelisted()) {
            // Player is not whitelisted
            return;
        }

        // Player is banned
        if (server.getBannedPlayers().stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
            return;
        }

        // Checking if player has been verified
        try {
            HttpURLConnection con = PluginUtils.fetchBackend(plugin.getConfig().getString("backend"), player);
            // Rejecting the player if needed
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                BackendError error = PluginUtils.getBackendError(con.getErrorStream());
                logger.info("Player " + player.getName() + " was rejected");
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, error.getMessage());
                return;
            }

            // Mapping and registering the object
            BackendData data = PluginUtils.getBackendData(con.getInputStream());

            // Managing the permissions
            new Thread(() -> {
                managePermission(player, data.isSupporter(), "group.supporter");
                managePermission(player, data.isModerator(), "group.mod");
            }, "PermissionManagerThread").start();

            // Success log message
            logger.info("User " + data.getName() + " access was permitted");


            // Allowing the supporters to bypass player slot limit
            if (data.isModerator() || data.isSupporter()) {
                if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
                    event.allow();
                    logger.info("User " + data.getName() + " bypassed player limit");
                }
            }

            // Saving the data
            if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
                //Saving the data
                plugin.getPlayers().put(player.getUniqueId(), data);
                logger.info("Player data successfully saved");
            }

            // Dispatching a message to Discord
            new Thread(() -> {
                FileConfiguration config = plugin.getConfig();
                int color = config.getInt("color.join");
                if (data.getId().equals(PluginConstants.SAMI_USER_ID)) {
                    color = config.getInt("color.sami");
                }
                String online = (server.getOnlinePlayers().size() + 1) + "/" + server.getMaxPlayers();
                plugin.sendCustomisedEmbed(player, new WebhookEmbedBuilder()
                        .setColor(color)
                        .setTitle(new WebhookEmbed.EmbedTitle("**" + player.getName().toUpperCase() + " JOINED**", null))
                        .setAuthor(new WebhookEmbed.EmbedAuthor(data.getName(), data.getAvatar(), null))
                        .setDescription(player.getName() + " just joined the game")
                        .addField(new WebhookEmbed.EmbedField(false, "Currently online:", online))
                        .setFooter(new WebhookEmbed.EmbedFooter(data.getId(), null))
                        .setTimestamp(Instant.now())
                        .build());
                logger.info("Player join message dispatched");
            }, "JoinMessageDispatcher").start();
        } catch (Exception e) {
            // On any error player will get kicked
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Unexpected error");
            logger.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    private void managePermission(@NotNull Player player, boolean isAllowed, String group) {
        LuckPerms perms = plugin.getPerms();
        UserManager manager = perms.getUserManager();

        // Player has the group
        if (player.hasPermission(group)) {
            if (!isAllowed) {
                // Player is not allowed to have that permission
                CompletableFuture<User> future = manager.loadUser(player.getUniqueId());
                future.thenAcceptAsync(user -> {
                    user.data().remove(Node.builder(group).build());
                    manager.saveUser(user);
                });
                logger.info("Group permission " + group + " was removed from " + player.getName());
            }

            // Player is allowed and has the permission
            return;
        }

        // Player is allowed but does NOT have the group
        if (isAllowed) {
            CompletableFuture<User> future = manager.loadUser(player.getUniqueId());
            future.thenAcceptAsync(user -> {
                user.data().add(Node.builder(group).build());
                manager.saveUser(user);
            });
            logger.info("Group permission " + group + " was added to " + player.getName());
        }
    }

}
