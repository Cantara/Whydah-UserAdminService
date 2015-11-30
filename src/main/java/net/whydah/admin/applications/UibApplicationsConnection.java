package net.whydah.admin.applications;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.security.UASCredentials;
import org.apache.commons.lang.NotImplementedException;
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
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Component
public class UibApplicationsConnection {
    private static final Logger log = LoggerFactory.getLogger(UibApplicationsConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
    private static final int NO_CONTENT = 204; //Response.Status.OK.getStatusCode();
    private static final int NOT_AUTHERIZED = 403;


    private final WebTarget uib;
    private final String userIdentityBackendUri = "http://localhost:9995/uib";
    private final UASCredentials uasCredentials;

    @Autowired
    @Configure
    public UibApplicationsConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }


    public String listAll(String userAdminServiceTokenId, String userTokenId) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/applications");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
       // String output = response.readEntity(String.class);
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        log.trace("listAll {}", output);
        switch (statusCode) {
            case STATUS_OK:
                break;
            case NO_CONTENT:
                break;
            case STATUS_BAD_REQUEST:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            case NOT_AUTHERIZED:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("listAll failed. Status code " + response.getStatus());
        }
        return output;
    }

    public String findApplications(String userAdminServiceTokenId, String userTokenId, String query) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/applications/find/"+query);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        // String output = response.readEntity(String.class);
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        log.trace("findApplications {}", output);
        switch (statusCode) {
            case STATUS_OK:
                break;
            case NO_CONTENT:
                break;
            case STATUS_BAD_REQUEST:
                log.error("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            case NOT_AUTHERIZED:
                log.error("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("listAll failed. Status code " + response.getStatus());
        }
        return output;
    }


}
