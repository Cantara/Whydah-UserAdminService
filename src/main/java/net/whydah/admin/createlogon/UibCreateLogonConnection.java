package net.whydah.admin.createlogon;

import net.whydah.admin.user.uib.UserAggregateRepresentation;
import net.whydah.admin.user.uib.UserIdentity;
import net.whydah.admin.user.uib.UserIdentityRepresentation;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class UibCreateLogonConnection {
    private static final Logger log = LoggerFactory.getLogger(UibCreateLogonConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
    private static final String SIGNUP_USER_PATH = "signup/user";
    private static final String CREATE_AND_LOGON_OPERATION = "createandlogon";


    private final WebTarget uibService;

    @Autowired
    @Configure
    public UibCreateLogonConnection(@Configuration("useridentitybackend") String uibUrl) {
        Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uibService = client.target(uibUrl);
    }

    public String createUser(String applicationTokenId, String fbUserXml) {

        WebTarget webResource = uibService.path("/" + applicationTokenId).path(SIGNUP_USER_PATH).path(CREATE_AND_LOGON_OPERATION);
        log.debug("URI to use {}",webResource.getUri());
        Response response = webResource.request(MediaType.APPLICATION_XML).post(Entity.entity(fbUserXml, MediaType.APPLICATION_XML));
        int statusCode = response.getStatus();
        if (statusCode != 200) {
            log.info("Request to UIB failed status {}, response {}", statusCode, response.getEntity());
            throw new ConnectionFailedException("Error creating user based on facebookUserXml {" + fbUserXml + "}, Response: {"+response.getEntity() +"}, Status {"+ statusCode +"}");
        }
        return response.getEntity().toString();

    }

    public UserIdentity createUser(String applicationTokenId, UserIdentityRepresentation minimalUser) {

        UserIdentity userIdentity = null;
//        userIdentity = new UserIdentity("temp-uid", minimalUser.getUsername(),minimalUser.getFirstName(),minimalUser.getLastName(),minimalUser.getPersonRef(),
//                minimalUser.getEmail(),minimalUser.getCellPhone(),null);
        if ( minimalUser != null) {

            WebTarget webResource = uibService.path("/" + applicationTokenId).path(SIGNUP_USER_PATH);
            log.debug("URI to use {}", webResource.getUri());
            Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(minimalUser.toJson(), MediaType.APPLICATION_JSON));
            int statusCode = response.getStatus();
            if (statusCode != 200) {
                log.info("Request to UIB failed status {}, response {}", statusCode, response.getEntity());
                throw new ConnectionFailedException("Error creating user based on minimalUser {" + minimalUser + "}, Response: {" + response.getEntity() + "}, Status {" + statusCode + "}");
            }
            String responseJson = response.getEntity().toString();
            log.debug("Response from UIB {} ", responseJson);
        }
        return userIdentity;
    }

    public UserAggregateRepresentation createAggregateUser(String applicationTokenId, UserAggregateRepresentation userAggregate) {
        UserAggregateRepresentation createdUser = null;
        if (userAggregate != null) {
            WebTarget webResource = uibService.path("/" + applicationTokenId).path(SIGNUP_USER_PATH);
            log.debug("URI to use {}", webResource.getUri());
            Response response = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userAggregate.toJson(), MediaType.APPLICATION_JSON));
            int statusCode = response.getStatus();
            String responseJson = null;
            switch (statusCode) {
                case 200:
                    responseJson = response.readEntity(String.class);
                    break;
                case 201:
                    responseJson = response.readEntity(String.class);
                    break;
                case 409:
                    log.trace("User already exist for userAggregate {}", userAggregate);
                    break;
                default:
                    log.info("Request to UIB failed status {}, response {}", statusCode, response.getEntity());
            }
            if (responseJson != null) {
                log.debug("Try to build createdUser from json {}", responseJson);
                createdUser = UserAggregateRepresentation.fromJson(responseJson);
            }
            log.debug("Response from UIB {} ", responseJson);
        }
        return createdUser;
    }
}
