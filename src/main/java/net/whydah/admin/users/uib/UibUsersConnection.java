package net.whydah.admin.users.uib;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.security.UASCredentials;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
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


    private  WebTarget uib;
    private final UASCredentials uasCredentials;
    private final String myUibUrl;

    @Autowired
    @Configure
    public UibUsersConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        this.myUibUrl=uibUrl;
//        URI useridbackendUri = URI.create(appConfig.getProperty("userIdentityBackendUri"));
        // uib = client.target(userIdentityBackendUri);
    }

    public Response findUsers(String userAdminServiceTokenId, String userTokenId, String query) {
        Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , myUibUrl);
        uib = client.target(myUibUrl);
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/users/find").path(query);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }
    
    private Response copyResponse(Response responseFromUib) {
//		Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
//		if (responseFromUib.hasEntity()) {
//			rb.entity(responseFromUib.getEntity());
//		}
//		return rb.build();
    	return responseFromUib;
	}


}
