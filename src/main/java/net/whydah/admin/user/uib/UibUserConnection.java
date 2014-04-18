package net.whydah.admin.user.uib;

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
 * Created by baardl on 17.04.14.
 */
@Component
public class UibUserConnection {

    private static final Logger log = LoggerFactory.getLogger(UibUserConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();


    private final WebTarget uib;
    private final String userIdentityBackendUri = "http://localhost:9995/uib";

    @Autowired
    public UibUserConnection(AppConfig appConfig) {
        Client client = ClientBuilder.newClient();
//        URI useridbackendUri = URI.create(appConfig.getProperty("userIdentityBackendUri"));
        // uib = client.target(userIdentityBackendUri);
        String uibUrl = appConfig.getProperty("userIdentityBackendUri");
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    public UserAggregateRepresentation addUserAgregate(String userAdminServiceTokenId, String userTokenId, String userAggregateJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user");
        UserAggregateRepresentation userAggregate = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userAggregateJson, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response form Uib {}", response.readEntity(String.class));
                userAggregate = UserAggregateRepresentation.fromJson(userAggregateJson);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + userAggregateJson + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return userAggregate;
    }
}
