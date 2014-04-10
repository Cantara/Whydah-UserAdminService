package net.whydah.admin.config;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;


public class AppConfigTest {
    @BeforeMethod
    public static void init() {
        System.setProperty(ApplicationMode.IAM_MODE_KEY, ApplicationMode.TEST);
        System.setProperty(AppConfig.IAM_CONFIG_KEY, "src/test/testconfig.properties");
    }

    @Test
    public void readProperties() throws IOException {
        AppConfig appConfig = new AppConfig();
        assertEquals("puh", appConfig.getProperty("nalle")); //fra testconfig
        assertEquals("9992", appConfig.getProperty("service.port")); //fra useradminservice.TEST.properties
    }
}
