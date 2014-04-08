package net.whydah.identity;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import net.whydah.admin.Main;
import net.whydah.admin.config.ApplicationMode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        restClient = ApacheHttpClient.create();
    }

    @AfterClass
    public static void teardown() throws Exception {
        main.stop();
    }

    @Test
    public void getLegalRemark() {
        WebResource webResource = restClient.resource(baseUri);
        String responseMsg = webResource.get(String.class);
        assertTrue(responseMsg.contains("Any misuse will be prosecuted."));
    }

    @Test
    public void getApplicationTokenTemplate() {
        WebResource webResource = restClient.resource(baseUri).path("/applicationtokentemplate");
        String responseMsg = webResource.get(String.class);
        assertTrue(responseMsg.contains("<applicationtoken>"));
    }


    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    @Test
    public void testApplicationWadl() {
        WebResource webResource = restClient.resource(baseUri).path("application.wadl");
        String responseMsg = webResource.get(String.class);
        assertTrue(responseMsg.contains("<application"));
        assertTrue(responseMsg.contains("logonApplication"));
    }
}
