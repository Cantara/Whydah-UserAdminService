package net.whydah.identity;


import com.sun.jersey.core.util.MultivaluedMapImpl;
import net.whydah.admin.Main;
import net.whydah.admin.config.ApplicationMode;
import net.whydah.identity.data.ApplicationCredential;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class PostTest {
    private static URI baseUri;
    Client restClient;

    private static Main main;

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

    public void testLogonApplication() {
        String appCredential = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><applicationcredential><appid>app123</appid><appsecret>123123</appsecret></applicationcredential>";
        String responseXML = logonApplication(appCredential);
        assertTrue(responseXML.contains("applicationtoken"));
        assertTrue(responseXML.contains("applicationid"));
        assertTrue(responseXML.contains("expires"));
        assertTrue(responseXML.contains("Url"));
    }


    private String getAppToken() {
        ApplicationCredential acred = new ApplicationCredential();
        acred.setApplicationID("Styrerommet");
        acred.setApplicationPassord("dummy");
        return logonApplication(acred.toXML());
    }

    private String logonApplication(String appCredential) {

        WebTarget logonResource = restClient.target(baseUri).path("logon");
        MultivaluedMap<String,String> formData = new MultivaluedMapImpl();
        formData.add("applicationcredential", appCredential);
        Response response = logonResource.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(Entity.entity(formData, MediaType.TEXT_PLAIN));
        return response.readEntity(String.class);
    }

    private String getTokenIdFromAppToken(String appTokenXML) {
        return appTokenXML.substring(appTokenXML.indexOf("<applicationtoken>") + "<applicationtoken>".length(), appTokenXML.indexOf("</applicationtoken>"));
    }
}
