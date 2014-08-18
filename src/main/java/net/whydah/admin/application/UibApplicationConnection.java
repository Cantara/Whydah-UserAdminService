package net.whydah.admin.application;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Component
public class UibApplicationConnection {
    private static final Logger log = LoggerFactory.getLogger(UibApplicationConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();


    private final WebTarget uib;
    private final String userIdentityBackendUri = "http://localhost:9995/uib";

    @Autowired
    public UibApplicationConnection(AppConfig appConfig) {
        Client client = ClientBuilder.newClient();
//        URI useridbackendUri = URI.create(appConfig.getProperty("useridentitybackend"));
       // uib = client.target(userIdentityBackendUri);
        String uibUrl = appConfig.getProperty("useridentitybackend");
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    public Application addApplication(String userAdminServiceTokenId, String userTokenId, String applicationJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/application");
        Application application = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(applicationJson,MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response form Uib {}", response.readEntity(String.class));
                application = buildApplication(applicationJson);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + applicationJson + ",  Status code " + response.getStatus());
            default:
              log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
              throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
         return application;
    }

    private Application buildApplication(String applicationJson) {
        log.debug("build json from {}", applicationJson);
        return Application.fromJson(applicationJson);
    }

    public Application getApplication(String userAdminServiceTokenId, String userTokenId, String applicationId) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/application/" + applicationId);
        Application application = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                application =  buildApplication(response.readEntity(String.class));
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return application;
    }
}
