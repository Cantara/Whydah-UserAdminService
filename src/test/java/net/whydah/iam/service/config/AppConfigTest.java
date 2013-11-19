package net.whydah.iam.service.config;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class AppConfigTest {
    @BeforeClass
    public static void init() {
        System.setProperty(ApplicationMode.IAM_MODE_KEY, ApplicationMode.TEST);
        System.setProperty(AppConfig.IAM_CONFIG_KEY, "src/test/testconfig.properties");
    }

    @Test
    public void readProperties() throws IOException {
        AppConfig appConfig = new AppConfig();
        assertEquals("puh", appConfig.getProperty("nalle")); //fra testconfig
        assertEquals("9998", appConfig.getProperty("service.port")); //fra useradminservice.TEST.properties
    }
}
