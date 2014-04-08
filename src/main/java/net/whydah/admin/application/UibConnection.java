package net.whydah.admin.application;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import net.whydah.identity.exception.AuthenticationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class UibConnection {
    private static final Logger log = LoggerFactory.getLogger(UibConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();


    private final WebResource uib;
    private final String userIdentityBackendUri = "http://localhost:9995/uib";

    public UibConnection() {
        Client client = Client.create();
        uib = client.resource(userIdentityBackendUri);
    }

    public Application addApplication(String userAdminServiceTokenId, String userTokenId, String applicationJson) {
        WebResource webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/application");
        Application application = null;
        com.sun.jersey.api.client.ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(com.sun.jersey.api.client.ClientResponse.class, applicationJson);
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response form Uib {}", response.getEntity(String.class));
                application = buildApplication(applicationJson);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.getEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + applicationJson + ",  Status code " + response.getStatus());
            default:
              log.error("Response from UIB: {}: {}", response.getStatus(), response.getEntity(String.class));
              throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
         return application;
    }

    private Application buildApplication(String applicationJson) {
        log.debug("build json from {}", applicationJson);
        return Application.fromJson(applicationJson);
    }

    public Application getApplication(String userAdminServiceTokenId, String userTokenId, String applicationId) {
        WebResource webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/application/" + applicationId);
        Application application = null;
        com.sun.jersey.api.client.ClientResponse response = webResource.get(com.sun.jersey.api.client.ClientResponse.class);
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                application =  buildApplication(response.getEntity(String.class));
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.getEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.getEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return application;
    }
}
