package net.whydah;

import net.whydah.admin.MainWithJetty;
import net.whydah.sso.config.ApplicationMode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.Scanner;

import static org.testng.AssertJUnit.assertTrue;

public class ServiceStartTest {

    private static MainWithJetty serverRunner;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(ApplicationMode.IAM_MODE_KEY, ApplicationMode.DEV);
        Random r = new Random(System.currentTimeMillis());
        serverRunner = new MainWithJetty(10000 + r.nextInt(20000));

        serverRunner.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        serverRunner.stop();
    }

    @Test
    public void testGetHealth() {
        URL url;
        URLConnection conn;
        try {
            url = new URL(MainWithJetty.getHEALTHURL());
            conn = url.openConnection();
            conn.connect();
            conn = url.openConnection();
            conn.connect();
            InputStream respose = conn.getInputStream();
            try (Scanner scanner = new Scanner(respose)) {
                String responseBody = scanner.useDelimiter("\\A").next();
                System.out.println(responseBody);
                assertTrue(responseBody.length() > 10);
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            assertTrue("Unable to connect to server", true);
        }
    }
}
