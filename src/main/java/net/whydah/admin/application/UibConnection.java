package net.whydah.admin.application;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class UibConnection {


    private final WebResource uib;
    private final String userIdentityBackendUri = "http://localhost:9995/uib";

    public UibConnection() {
        Client client = Client.create();
        uib = client.resource(userIdentityBackendUri);
    }

    public void addApplication(String applicationTokenId, String userTokenId) {
        String applicationJson = "{}";
        //TODO bli: ClientResponse response = uib.path("/" + applicationTokenId + "/" + userTokenId + "/application").type(MediaType.APPLICATION_JSON).post(applicationJson);

    }
}
