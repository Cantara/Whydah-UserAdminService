package net.whydah.admin.applications.uib;

import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.security.UASCredentials;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;

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


    private  WebTarget uib;
    private final String userIdentityBackendUri;
    private final UASCredentials uasCredentials;
    private static String cachedApplicationsString = "";
    private static Instant cachedApplicationsStringInstant;

    @Autowired
    @Configure
    public UibApplicationsConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        this.userIdentityBackendUri=uibUrl;
    }


    public String listAll(String applicationTokenId) throws AppException {
        if (cachedApplicationsStringInstant != null) {
            if (Instant.now().isAfter(cachedApplicationsStringInstant.plusSeconds(30))) {
                // 30 second cache to avoid too much UIB noise
                return cachedApplicationsString;
            }
        }
        Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , userIdentityBackendUri);
        uib = client.target(userIdentityBackendUri);
        WebTarget webResource = uib.path(applicationTokenId).path("applications");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
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
                //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            case NOT_AUTHERIZED:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), output);
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), output);
                //throw new AuthenticationFailedException("listAll failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("listAll-Response from UIB: {}: {}", response.getStatus(), output);
        }
        cachedApplicationsStringInstant = Instant.now();
        cachedApplicationsString = output;
        return output;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String query) throws AppException {
        Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , userIdentityBackendUri);
        uib = client.target(userIdentityBackendUri);
        //WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/applications/find/"+query);
        WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("applications").path("find").path(query);
        //WebTarget webResource = uib.path("/applications/find/"+query);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
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
                //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
            case NOT_AUTHERIZED:
                log.error("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
            default:
                log.error("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                //throw new AuthenticationFailedException("listAll failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
        }
        return output;
    }


}
