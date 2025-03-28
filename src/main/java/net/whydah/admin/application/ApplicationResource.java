package net.whydah.admin.application;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.errorhandling.AppException;
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

    private ApplicationService applicationService;

    @Autowired
    public ApplicationResource(ApplicationService applicationService) {
        log.debug("Injected constructor called with service: {}", applicationService);
        this.applicationService = applicationService;
    }
    /**
     *
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
        return applicationService.createApplication(applicationTokenId, userTokenId, applicationJson);
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
        return applicationService.getApplication(applicationTokenId, userTokenId, applicationId);

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
        return applicationService.updateApplication(applicationTokenId, userTokenId, applicationId, applicationJson);

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
        return applicationService.deleteApplication(applicationTokenId, userTokenId, applicationId);
    }

    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }
}