package net.whydah.admin.users.uib;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by baardl on 17.04.14.
 */
@Component
public class UibUsersConnection {

    private static final Logger log = LoggerFactory.getLogger(UibUsersConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
    private static final int STATUS_FORBIDDEN = 403;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_CONFLICT = 409;
    private static final int STATUS_NO_CONTENT = 204;


    private final WebTarget uib;

    @Autowired
    public UibUsersConnection(AppConfig appConfig) {
        Client client = ClientBuilder.newClient();
//        URI useridbackendUri = URI.create(appConfig.getProperty("userIdentityBackendUri"));
        // uib = client.target(userIdentityBackendUri);
        String uibUrl = appConfig.getProperty("useridentitybackend");
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    public String findUsers(String userAdminServiceTokenId, String userTokenId, String query) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/users/find").path(query);
        String resultJson = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response form Uib {}", output);
                resultJson = output;
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("BadRequest for query " + query + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("Request failed. Status code " + response.getStatus());
        }
        return resultJson;
    }


}
