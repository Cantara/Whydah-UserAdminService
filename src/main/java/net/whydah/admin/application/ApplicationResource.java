package net.whydah.admin.application;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.whydah.admin.applications.ApplicationsService;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.security.UASCredentials;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Proxy in front of UIB application CRUD endpoint.
 *
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/application")
@Component
public class ApplicationResource {
    private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

    private static final String APPLICATION_PATH = "application";
    private WebTarget uib;
    private final UASCredentials uasCredentials;
    private final String myUibUrl;
    ApplicationsService applicationsService;

    @Autowired
    @Configure
    public ApplicationResource(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials, ApplicationsService applicationsService) {
        this.uasCredentials = uasCredentials;
        this.myUibUrl = uibUrl;
        this.applicationsService = applicationsService; 
    }

    /**
     * @throws Exception
     * @api {post} :applicationtokenid}/:userTokenId/application createApplication
     * @apiName createApplication
     * @apiGroup User Admin Service (UAS)
     * @apiDescription Create an application
     * @apiParamExample {json} Request-Example:
     * {
     * "id":"47afaee0-d6a7-4522-89b3-04a894d3432b",
     * "name":"ACS_6476",
     * "description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.",
     * "company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY",
     * "applicationUrl":"http://my.application.com",
     * "logoUrl":"http://my.application.com/mylogo.png",
     * "fullTokenApplication":false,
     * "roles":[{"id":"roleId-133","name":"superuser"}],
     * "defaultRoleName":"Employee",
     * "orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],
     * "defaultOrganizationName":"ACSOrganization",
     * "security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":["0.0.0.0/0"],"userTokenFilter":"true","secret":"45fhRM6nbKZ2wfC6RMmMuzXpk"},
     * "acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]
     * }
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id":"47afaee0-d6a7-4522-89b3-04a894d3432b",
     * "name":"ACS_6476",
     * "description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.",
     * "company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY",
     * "applicationUrl":"http://my.application.com",
     * "logoUrl":"http://my.application.com/mylogo.png",
     * "fullTokenApplication":false,
     * "roles":[{"id":"roleId-133","name":"superuser"}],
     * "defaultRoleName":"Employee",
     * "orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],
     * "defaultOrganizationName":"ACSOrganization",
     * "security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":["0.0.0.0/0"],"userTokenFilter":"true","secret":"45fhRM6nbKZ2wfC6RMmMuzXpk"},
     * "acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]
     * }
     * @apiError 400/8001 Invalid json format.
     * @apiError 500/9999 A generic exception or an unexpected error
     * @apiErrorExample Error-Response:
     * HTTP/1.1 400 Bad request
     * {
     * "status": 400,
     * "code": 8001,
     * "message": "Invalid json format.",
     * "link": "",
     * "developerMessage": "Invalid json format."
     * }
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("userTokenId") String userTokenId,
                                      String applicationJson) throws Exception {
        log.trace("create is called with applicationJson={}", applicationJson);

        if (hasUserAndApplicationAccess(userTokenId, applicationTokenId)) {
            Client client = ClientBuilder.newClient();
            log.info("Connection to UserIdentityBackend on {}", myUibUrl);
            uib = client.target(myUibUrl);
            WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH);
            Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
            Response response = copyResponse(responseFromUib);
            if (responseFromUib.getStatus() == 400) {
                throw AppExceptionCode.APP_INVALID_JSON_FORMAT_8001;
            } else if (responseFromUib.getStatus() == 500) {
                throw new WebApplicationException("Unexpected error from UIB", 500);
            }
            return response;
        } else {
            throw new WebApplicationException("No access", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private Response copyResponse(Response responseFromUib) {
        Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
        if (responseFromUib.hasEntity()) {
            rb.entity(responseFromUib.getEntity());
        }


        return rb.build();
    }

    /**
     * @throws AppException
     * @throws Exception
     * @api {get} :applicationtokenid}/:userTokenId/application/:applicationId getApplication
     * @apiName getApplication
     * @apiGroup User Admin Service (UAS)
     * @apiDescription Get an application by its applciationId
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id":"47afaee0-d6a7-4522-89b3-04a894d3432b",
     * "name":"ACS_6476",
     * "description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.",
     * "company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY",
     * "applicationUrl":"http://my.application.com",
     * "logoUrl":"http://my.application.com/mylogo.png",
     * "fullTokenApplication":false,
     * "roles":[{"id":"roleId-133","name":"superuser"}],
     * "defaultRoleName":"Employee",
     * "orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],
     * "defaultOrganizationName":"ACSOrganization",
     * "security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":["0.0.0.0/0"],"userTokenFilter":"true","secret":"45fhRM6nbKZ2wfC6RMmMuzXpk"},
     * "acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]
     * }
     * @apiError 404/8002 Application cannot be found
     * @apiError 500/9999 A generic exception or an unexpected error
     * @apiErrorExample Error-Response:
     * HTTP/1.1 404 Not found
     * {
     * "status": 404,
     * "code": 8002,
     * "message": "Application cannot be found.",
     * "link": "",
     * "developerMessage": "Application cannot be found."
     * }
     */
    @GET
    @Path("/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                   @PathParam("userTokenId") String userTokenId,
                                   @PathParam("applicationId") String applicationId) throws AppException {
        log.trace("getApplication is called with applicationId={}", applicationId);
        if (hasUserAndApplicationAccess(userTokenId, applicationTokenId)) {
            Client client = ClientBuilder.newClient();
            log.info("Connection to UserIdentityBackend on {}", myUibUrl);
            uib = client.target(myUibUrl);
            WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH).path(applicationId);
            Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();

            if (responseFromUib.getStatus() == 200) {

                String jsonResult = responseFromUib.readEntity(String.class);
                log.trace("Received jsonResult {}", jsonResult);
                return Response.ok(jsonResult).build();

            } else {
                if (responseFromUib.getStatus() == 404) {
                    //application not found
                    throw AppExceptionCode.APP_NOTFOUND_8002;
                } else {
                    //server error
                    throw new WebApplicationException("Unexpected error from UIB", 500);
                }
            }
        } else {
            throw new WebApplicationException("No access", HttpServletResponse.SC_UNAUTHORIZED);
        }

    }

    /**
     * @throws AppException
     * @throws Exception
     * @api {put} :applicationtokenid}/:userTokenId/application/:applicationId getApplication
     * @apiName getApplication
     * @apiGroup User Admin Service (UAS)
     * @apiDescription Get an application by its applciationId
     * @apiParamExample {json} Request-Example:
     * {
     * "id":"47afaee0-d6a7-4522-89b3-04a894d3432b",
     * "name":"ACS_6476",
     * "description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.",
     * "company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY",
     * "applicationUrl":"http://my.application.com",
     * "logoUrl":"http://my.application.com/mylogo.png",
     * "fullTokenApplication":false,
     * "roles":[{"id":"roleId-133","name":"superuser"}],
     * "defaultRoleName":"Employee",
     * "orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],
     * "defaultOrganizationName":"ACSOrganization",
     * "security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":["0.0.0.0/0"],"userTokenFilter":"true","secret":"45fhRM6nbKZ2wfC6RMmMuzXpk"},
     * "acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]
     * }
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 204 No Content
     * {
     * "id":"47afaee0-d6a7-4522-89b3-04a894d3432b",
     * "name":"ACS_6476",
     * "description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.",
     * "company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY",
     * "applicationUrl":"http://my.application.com",
     * "logoUrl":"http://my.application.com/mylogo.png",
     * "fullTokenApplication":false,
     * "roles":[{"id":"roleId-133","name":"superuser"}],
     * "defaultRoleName":"Employee",
     * "orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],
     * "defaultOrganizationName":"ACSOrganization",
     * "security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":["0.0.0.0/0"],"userTokenFilter":"true","secret":"45fhRM6nbKZ2wfC6RMmMuzXpk"},
     * "acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]
     * }
     * @apiError 400/8001 Invalid json format
     * @apiError 404/8002 Application cannot be found
     * @apiError 500/9999 A generic exception or an unexpected error
     * @apiErrorExample Error-Response:
     * HTTP/1.1 400 Bad request
     * {
     * "status": 400,
     * "code": 8001,
     * "message": "Invalid json format",
     * "link": "",
     * "developerMessage": "Invalid json format"
     * }
     */
    @PUT
    @Path("/{applicationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("userTokenId") String userTokenId,
                                      @PathParam("applicationId") String applicationId,
                                      String applicationJson) throws AppException {
        log.trace("updateApplication applicationId={}, applicationJson={}", applicationId, applicationJson);
        if (hasUserAndApplicationAccess(userTokenId, applicationTokenId)) {
            Client client = ClientBuilder.newClient();
            log.info("Connection to UserIdentityBackend on {}", myUibUrl);
            uib = client.target(myUibUrl);
            WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH).path(applicationId);
            Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));

            if (responseFromUib.getStatus() == 204) {
                return copyResponse(responseFromUib);
            } else {
                if (responseFromUib.getStatus() == 404) {
                    //application not found
                    throw AppExceptionCode.APP_NOTFOUND_8002;
                } else if (responseFromUib.getStatus() == 400) {
                    //application not found
                    throw AppExceptionCode.APP_INVALID_JSON_FORMAT_8001;
                } else {
                    //server error
                    throw new WebApplicationException("Unexpected error from UIB", 500);

                }
            }

        } else {
            throw new WebApplicationException("No access", HttpServletResponse.SC_UNAUTHORIZED);
        }

    }

    /**
     * @throws AppException
     * @throws Exception
     * @api {delete} :applicationtokenid}/:userTokenId/application/:applicationId deleteApplication
     * @apiName deleteApplication
     * @apiGroup User Admin Service (UAS)
     * @apiDescription Delete an application by its applicationId
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 204 No content
     * @apiError 404/8002 Application cannot be found
     * @apiError 500/9999 A generic exception or an unexpected error
     * @apiErrorExample Error-Response:
     * HTTP/1.1 404 Not found
     * {
     * "status": 404,
     * "code": 8002,
     * "message": "Application cannot be found.",
     * "link": "",
     * "developerMessage": "Application cannot be found."
     * }
     */
    @DELETE
    @Path("/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("userTokenId") String userTokenId,
                                      @PathParam("applicationId") String applicationId) throws AppException {
        log.trace("deleteApplication is called with applicationId={}", applicationId);
        if (hasUserAndApplicationAccess(userTokenId, applicationTokenId)) {
            Client client = ClientBuilder.newClient();
            log.info("Connection to UserIdentityBackend on {}", myUibUrl);
            uib = client.target(myUibUrl);
            WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH).path(applicationId);
            Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
            if (responseFromUib.getStatus() == 204) {

                return copyResponse(responseFromUib);

            } else {
                if (responseFromUib.getStatus() == 404) {
                    //application not found
                    throw AppExceptionCode.APP_NOTFOUND_8002;
                } else {
                    //server error
                    throw new WebApplicationException("Unexpected error from UIB", 500);
                }
            }
        } else {
            throw new WebApplicationException("No access", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean hasUserAndApplicationAccess(String userTokenId, String applicationTokenId) throws AppException {
    	return applicationsService.getAdminChecker().hasAccess(applicationTokenId, userTokenId);
        
    }

    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }
}
