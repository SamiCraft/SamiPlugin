package com.samifying.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samifying.plugin.atributes.BackendData;
import com.samifying.plugin.atributes.BackendError;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PluginUtils {

    @NotNull
    public static String trimUniqueId(@NotNull Player player) {
        return player.getUniqueId().toString().replace("-", "");
    }

    @NotNull
    public static HttpURLConnection fetchBackend(String uuid, String guild, String role) throws IOException {
        URL url = new URL("https://link.samifying.com/api/user/" + uuid + "?guild=" + guild + "&role=" + role);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    @NotNull
    public static HttpURLConnection fetchBackend(Player player, String guild, String role) throws IOException {
        return fetchBackend(trimUniqueId(player), guild, role);
    }

    public static String getJson(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        return in.readLine();
    }

    public static BackendData getBackendData(InputStream stream) throws IOException {
        String json = getJson(stream);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, BackendData.class);
    }

    public static BackendError getBackendError(InputStream stream) throws IOException {
        String json = getJson(stream);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, BackendError.class);
    }

    @NotNull
    public static String sanitize(@NotNull String string) {
        return string
                .replace("*", "")
                .replace("_", "")
                .replace("#", "")
                .replace(">", "")
                .replace("-", "")
                .replace("`", "")
                .replace("|", "");
    }
}
