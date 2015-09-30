package net.whydah.admin.createlogon;

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
    private static final String USER_AUTHENTICATION_PATH = "authenticate/user";
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

        WebTarget webResource = uibService.path("/" + applicationTokenId).path(USER_AUTHENTICATION_PATH).path(CREATE_AND_LOGON_OPERATION);
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
//        if ( minimalUser != null) {
//
//            WebTarget webResource = uibService.path("/" + applicationTokenId).path(USER_AUTHENTICATION_PATH).path(CREATE_AND_LOGON_OPERATION);
//            log.debug("URI to use {}", webResource.getUri());
//            Response response = webResource.request(MediaType.APPLICATION_XML).post(Entity.entity(fbUserXml, MediaType.APPLICATION_XML));
//            int statusCode = response.getStatus();
//            if (statusCode != 200) {
//                log.info("Request to UIB failed status {}, response {}", statusCode, response.getEntity());
//                throw new ConnectionFailedException("Error creating user based on facebookUserXml {" + fbUserXml + "}, Response: {" + response.getEntity() + "}, Status {" + statusCode + "}");
//            }
//            //TODO build response response.getEntity().toString();

//        }
        return userIdentity;
    }
}
