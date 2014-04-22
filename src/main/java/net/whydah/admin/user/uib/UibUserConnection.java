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
    private static final int STATUS_FORBIDDEN = 403;


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

    public UserAggregate addUserAgregate(String userAdminServiceTokenId, String userTokenId, String userAggregateJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user");
        UserAggregate userAggregate = null;
        UserAggregateRepresentation userAggregateRepresentation = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userAggregateJson, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response form Uib {}", response.readEntity(String.class));
                userAggregateRepresentation = UserAggregateRepresentation.fromJson(userAggregateJson);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + userAggregateJson + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        userAggregate = userAggregateRepresentation.getUserAggregate();
        return userAggregate;
    }

    public UserIdentity createUser(String userAdminServiceTokenId, String userTokenId, String userIdentityJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user");
        UserIdentity userIdentity = null;
        UserAggregateRepresentation userAggregateRepresentation = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userIdentityJson, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("createUser-Response form Uib {}", response.readEntity(String.class));
                userIdentity = UserIdentity.fromJson(response.readEntity(String.class));
                break;
            case STATUS_BAD_REQUEST:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
//FIXME bli:                throw new BadRequestException("BadRequest for Json " + userIdentityJson + ",  Status code " + response.getStatus());
                userIdentity = UserIdentity.fromJson(userIdentityJson);
                break;
            default:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return userIdentity;
    }

    public boolean changePassword(String userAdminServiceTokenId, String adminUserTokenId, String userName, String password) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(userName).path("changepassword");
        boolean updatedOk = false;
        Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(password, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("changePassword-Response form Uib {}", response.readEntity(String.class));
                updatedOk = true;
                break;
            case STATUS_FORBIDDEN:
                log.error("changePassword-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), response.readEntity(String.class));
                break;
            default:
                log.error("changePassword-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return updatedOk;
    }
}
