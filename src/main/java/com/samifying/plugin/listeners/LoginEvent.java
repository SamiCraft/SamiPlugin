package com.samifying.plugin.listeners;


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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class LoginEvent implements Listener {

    private final SamiPlugin plugin;
    private final Logger logger;
    private final FileConfiguration config;

    public LoginEvent(@NotNull SamiPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();
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
            HttpURLConnection con = PluginUtils.fetchBackend(player);
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

            // Maintenance mode
            if (config.getBoolean("enable.maintenance") && !data.isModerator()) {
                logger.info("Player " + player.getName() + " was rejected, maintenance in progress");
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server is under maintenance");
                return;
            }

            // Success log message
            logger.info("User " + data.getName() + " access was permitted");

            // Allowing staff and supporters to bypass player slot limit
            if (data.isModerator() || (config.getBoolean("enable.supporter-bypass") && data.isSupporter())) {
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
