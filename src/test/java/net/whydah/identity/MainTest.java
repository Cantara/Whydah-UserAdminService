package net.whydah.identity;


import net.whydah.admin.Main;
import net.whydah.admin.config.ApplicationMode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class MainTest {
    private static Main main;
    private static URI baseUri;
    Client restClient;

    @BeforeClass
    public static void init() throws Exception {
        System.setProperty(ApplicationMode.IAM_MODE_KEY, ApplicationMode.DEV);
        main = new Main();
        main.startServer();
        baseUri = UriBuilder.fromUri("http://localhost" + Main.CONTEXT_PATH).port(main.getPort()).build();
    }

    @Before
    public void initRun() throws Exception {
        restClient = ClientBuilder.newClient();
    }

    @AfterClass
    public static void teardown() throws Exception {
        main.stop();
    }

    @Test
    public void getLegalRemark() {
        WebTarget logonResource = restClient.target(baseUri).path("logon");
        Response response = logonResource.request(MediaType.TEXT_HTML).get();
        String responseMsg =  response.readEntity(String.class);
        assertTrue(responseMsg.contains("Any misuse will be prosecuted."));
    }

    @Test
    public void getApplicationTokenTemplate() {
        WebTarget logonResource = restClient.target(baseUri).path("/applicationtokentemplate");
        String responseMsg = logonResource.request(MediaType.TEXT_HTML).get(String.class);
        assertTrue(responseMsg.contains("<applicationtoken>"));
    }


    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    @Test
    public void testApplicationWadl() {
        WebTarget logonResource = restClient.target(baseUri).path("application.wadl");
        String responseMsg = logonResource.request(MediaType.APPLICATION_XML).get(String.class);
        assertTrue(responseMsg.contains("<application"));
        assertTrue(responseMsg.contains("logonApplication"));
    }
}
