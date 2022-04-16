package com.samifying.test;

import com.samifying.plugin.PluginUtils;
import com.samifying.plugin.SamiPlugin;
import com.samifying.plugin.atributes.BackendData;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

public class PluginTest {

    public static final String GUILD_ID = "264801645370671114";
    public static final String ROLE_ID = "426156903555399680";

    @Test
    public void isBackendReachable() throws IOException {
        HttpURLConnection connection = PluginUtils.fetchBackend("06805a4280d0463dbf7151b1e1317cd4", GUILD_ID, ROLE_ID);
        BackendData data = PluginUtils.getBackendData(connection.getInputStream());
        Assert.assertEquals(SamiPlugin.PEQULA_USER_ID, data.getId());
    }

}
