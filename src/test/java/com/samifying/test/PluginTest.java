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

    @Test
    public void isBackendReachable() throws IOException {
        HttpURLConnection connection = PluginUtils.fetchBackend("06805a4280d0463dbf7151b1e1317cd4");
        BackendData data = PluginUtils.getBackendData(connection.getInputStream(), Logger.getLogger(PluginTest.class.getName()));
        Assert.assertEquals(SamiPlugin.PEQULA_USER_ID, data.getId());
    }

}
