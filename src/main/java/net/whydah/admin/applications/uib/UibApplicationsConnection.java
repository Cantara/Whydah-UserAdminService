package net.whydah.admin.applications.uib;

import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.security.UASCredentials;
import net.whydah.sso.ddd.model.ApplicationTokenID;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.whydah.sso.util.LoggerUtil.first50;

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


    //    private  WebTarget uib;
    private final String userIdentityBackendUri;
    private final UASCredentials uasCredentials;
    private static String cachedApplicationsString = "";
    private static Instant cachedApplicationsStringInstant = Instant.now();
    private static Map<String, String> cachedApplicationMap = new LinkedHashMap<String, String>();
    private static Instant cachedApplicationMapInstant = Instant.now();

    @Autowired
    @Configure
    public UibApplicationsConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        this.userIdentityBackendUri=uibUrl;
    }


    public String listAll(String applicationTokenId) throws AppException {
        if (applicationTokenId == null || applicationTokenId.length() < 4) {
            log.warn("listAll Null or bogus applicationTokenId found {}, returning null", applicationTokenId);
            return null;  // DO NOT BLOCK THREAD on requests that are doomed to fail
        }

        if (cachedApplicationsStringInstant != null && cachedApplicationsString != null && cachedApplicationsString.length() > 50) {
            if (Instant.now().isBefore(cachedApplicationsStringInstant.plusSeconds(30))) {
                log.debug("listAll Returning applications from cache");
                // 30 second cache to avoid too much UIB noise
                return cachedApplicationsString;
            } else {
                log.debug("listAll Returning applications from UIB request");

            }
        }
//        Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}", userIdentityBackendUri);
//        uib = client.target(userIdentityBackendUri);
//        WebTarget webResource = uib.path(applicationTokenId).path("applications");
//        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();

        Response response =
                new CommandGetApplicationsFromUIB(userIdentityBackendUri, applicationTokenId, uasCredentials.getApplicationCredentialsXmlEncoded()).execute();

        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        log.info("listAll Applications: {}", first50(output));
        switch (statusCode) {
            case STATUS_OK:
                cachedApplicationsStringInstant = Instant.now();
                cachedApplicationsString = output;
                break;
            case NO_CONTENT:
                break;
            case STATUS_BAD_REQUEST:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), first50(output));
                //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            case NOT_AUTHERIZED:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), first50(output));
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("listAll-Response from UIB: {}: {}", response.getStatus(), first50(output));
                //throw new AuthenticationFailedException("listAll failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("listAll-Response from UIB: {}: {}", response.getStatus(), output);
        }
        return output;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String query) throws AppException {
        try {
            if (!ApplicationTokenID.isValid(applicationTokenId) || query == null) {
                log.warn("findApplications - Null or bogus applicationTokenId found {} query:{}, returning null", applicationTokenId, query);
                return null;  // DO NOT BLOCK THREAD on requests that are doomed to fail
            }
            if (query.length() < 1) {
                log.warn("findApplications - Empty query found {} query:{}, returning null", applicationTokenId, query);
                return null;  // DO NOT BLOCK THREAD on requests that are doomed to fail
            }
            if (cachedApplicationMap.get(query) != null && cachedApplicationMapInstant != null) {
                if (Instant.now().isBefore(cachedApplicationMapInstant.plusSeconds(20))) {
                    log.info("findApplications - Returning application(s) from cache");
                    // 30 second cache to avoid too much UIB noise
                    return cachedApplicationMap.get(query);
                }
            }

//        Client client = ClientBuilder.newClient();
            log.info("findApplications - Connection to UserIdentityBackend on {}", userIdentityBackendUri);
//        uib = client.target(userIdentityBackendUri);
            //WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/applications/find/"+query);
//        WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("applications").path("find").path(query);
            //WebTarget webResource = uib.path("/applications/find/"+query);
//        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
            // String output = response.readEntity(String.class);

            Response response =
                    new CommandFindApplicationsFromUIB(userIdentityBackendUri, applicationTokenId, userTokenId, uasCredentials.getApplicationCredentialsXmlEncoded(), query).execute();

            int statusCode = response.getStatus();
            String output = response.readEntity(String.class);
            log.info("findApplications {}", first50(output));
            switch (statusCode) {
                case STATUS_OK:
                    log.debug("findApplications-Response from UIB: {}: {}", statusCode, output);
                    cachedApplicationMapInstant = Instant.now();
                    cachedApplicationMap = new LinkedHashMap<String, String>();  // only old data, start caching again
                    cachedApplicationMap.put(query, output);
                    break;
                case NO_CONTENT:
                    break;
                case STATUS_BAD_REQUEST:
                    log.error("findApplications-Response from UIB: {}: {}", statusCode, output);
                    //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                    throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                case NOT_AUTHERIZED:
                    log.error("findApplications-Response from UIB: {}: {}", statusCode, output);
                    //throw new BadRequestException("listAll failed. Bad request " + response.toString() + ",  Status code " + response.getStatus());
                    throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
                default:
                    log.error("findApplications-Response from UIB: {}: {}", statusCode, output);
                    //throw new AuthenticationFailedException("listAll failed. Status code " + response.getStatus());
                    throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("findApplications-Response from UIB: {}: {}", response.getStatus(), output);
            }
            return output;
        } catch (Exception e) {
            log.error("findApplications-Exception in UIB request, returning null", e);

        }
        return null;
    }

    public static void clearCache() {
        cachedApplicationsStringInstant = null;
        cachedApplicationMapInstant = null;

    }


}
