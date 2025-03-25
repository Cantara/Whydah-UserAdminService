package net.whydah.admin.applications;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.sso.application.mappers.ApplicationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.whydah.sso.util.LoggerUtil.first50;

/**
 * Accessable in DEV mode:
 * - http://localhost:9992/useradminservice/1/1/applications
 * - http://localhost:9992/useradminservice/1/1/applications/1
 *
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */

@Path("/{applicationtokenid}")
@Component
public class ApplicationsResource {
	private static final Logger log = LoggerFactory.getLogger(ApplicationsResource.class);
	private ApplicationsService applicationsService;

	/**
	 * Default constructor for HK2
	 */
	public ApplicationsResource() {
		log.debug("Default constructor called by HK2");
		// No initialization - will be injected
	}

	/**
	 * Constructor with dependency injection.
	 * Using @Inject which works with both Spring and HK2
	 */
	@Inject
	public ApplicationsResource(ApplicationsService applicationsService) {
		log.debug("Constructor injection called with service: {}", applicationsService);
		this.applicationsService = applicationsService;
	}


	/**
	 * @apiIgnore
	 * @throws AppException 
	 * @throws Exception 
	 * @api {get} :applicationtokenid}/applications listAll
	 * @apiName listAll
	 * @apiGroup User Admin Service (UAS)
	 * @apiDescription List all applications
	 * 
	 *
	 * @apiSuccessExample Success-Response:
	 *	HTTP/1.1 200 OK
	 * 
	 *[{"id":"03847c72-6bc3-46e6-94d2-07a4d9c3f35b","name":"ACS_2744","description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.","company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY","applicationUrl":"http://my.application.com","logoUrl":"http://my.application.com/mylogo.png","fullTokenApplication":false,"roles":[{"id":"roleId-133","name":"superuser"}],"defaultRoleName":"Employee","orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],"defaultOrganizationName":"ACSOrganization","security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":null,"userTokenFilter":"true","secret":"*************"},"acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]},{"id":"100","name":"ACS","description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tillpasset leseren.","company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY","applicationUrl":null,"logoUrl":null,"fullTokenApplication":false,"roles":[{"id":"acs101","name":"Employee"},{"id":"acs102","name":"Manager"},{"id":"acs103","name":"Administrator"}],"defaultRoleName":"Employee","orgs":[{"id":"100","name":"Whydah"},{"id":"101","name":"Cantara"},{"id":"102","name":"Getwhydah"}],"defaultOrganizationName":"ACSOrganization","security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":null,"userTokenFilter":"true","secret":"*************"},"acl":[{"applicationId":"11","applicationACLPath":"/user","accessRights":null}],"organizationNames":[{"id":"100","name":"Whydah"},{"id":"101","name":"Cantara"},{"id":"102","name":"Getwhydah"}]}]
	 *
	 * 
	 * @apiError 500/9996 AuthenticationFailedException
	 * @apiError 400/9997 BadRequestException
	 * @apiError 500/9999 A generic exception or an unexpected error 
	 *
	 * @apiErrorExample Error-Response:
	 * HTTP/1.1 400 Bad Request
	 * {
	 * 	"status": 400,
	 *  "code": 9997,
	 *  "message": "BadRequestException",
	 *  "link": "",
	 *  "developerMessage": ""
	 *	}
	 */
	@GET
	@Path("/applications")
	@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
	public Response listAll(@PathParam("applicationtokenid") String applicationTokenId) throws AppException {
		String applications = applicationsService.listAll(applicationTokenId);
        return Response.ok(ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications))).build();
    }

	/**
	 * @throws AppException 
	 * @api {get} :userTokenId/find/applications/:applicationName findByName
	 * @apiName findByName
	 * @apiGroup User Admin Service (UAS)
	 * @apiDescription List all applications
	 * 
	 *
	 * @apiSuccessExample Success-Response:
	 *	HTTP/1.1 200 OK
	 * 
	 *[{"id":"03847c72-6bc3-46e6-94d2-07a4d9c3f35b","name":"ACS_2744","description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tilpasset leseren.","company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY","applicationUrl":"http://my.application.com","logoUrl":"http://my.application.com/mylogo.png","fullTokenApplication":false,"roles":[{"id":"roleId-133","name":"superuser"}],"defaultRoleName":"Employee","orgs":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}],"defaultOrganizationName":"ACSOrganization","security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":null,"userTokenFilter":"true","secret":"*************"},"acl":[],"organizationNames":[{"id":"orgid-12345","name":"ACSOrganization"},{"id":"my.application.com","name":"application.com"}]},{"id":"100","name":"ACS","description":"Finn den kompetansen du trenger, når du trenger det. Lag eksklusive CV'er tillpasset leseren.","company":"Norway AS","tags":"HIDDEN, JURISDICTION_NORWAY","applicationUrl":null,"logoUrl":null,"fullTokenApplication":false,"roles":[{"id":"acs101","name":"Employee"},{"id":"acs102","name":"Manager"},{"id":"acs103","name":"Administrator"}],"defaultRoleName":"Employee","orgs":[{"id":"100","name":"Whydah"},{"id":"101","name":"Cantara"},{"id":"102","name":"Getwhydah"}],"defaultOrganizationName":"ACSOrganization","security":{"minSecurityLevel":"0","minDEFCON":"DEFCON5","maxSessionTimeoutSeconds":"86400","allowedIpAddresses":null,"userTokenFilter":"true","secret":"*************"},"acl":[{"applicationId":"11","applicationACLPath":"/user","accessRights":null}],"organizationNames":[{"id":"100","name":"Whydah"},{"id":"101","name":"Cantara"},{"id":"102","name":"Getwhydah"}]}]
	 *
	 * @apiError 500/9996 AuthenticationFailedException
	 * @apiError 400/9997 BadRequestException
	 * @apiError 500/9999 A generic exception or an unexpected error 
	 *
	 * @apiErrorExample Error-Response:
	 * HTTP/1.1 400 Bad Request
	 * {
	 * 	"status": 400,
	 *  "code": 9997,
	 *  "message": "BadRequestException",
	 *  "link": "",
	 *  "developerMessage": ""
	 * }
	 */
	@GET
	@Path("{userTokenId}/find/applications/{applicationName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public Response findByName(@PathParam("applicationtokenid") String applicationTokenId,
			@PathParam("userTokenId") String userTokenId,
			@PathParam("applicationName") String applicationName) throws AppException {
        log.info("{userTokenId}/find/applications/{}  userTokenId:{}  - applicationTokenId:{}", applicationName, userTokenId, applicationTokenId);
        String applications = applicationsService.findApplications(applicationTokenId, userTokenId, applicationName);
        log.info("/find/applications/{} - returns:{}", applicationName, first50(applications));
        return Response.ok(applications).build();

	}

    @GET
    @Path("/find/applications/{applicationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response findByNameWithoutUserTokenId(@PathParam("applicationtokenid") String applicationTokenId,
                                                 @PathParam("applicationName") String applicationName) throws AppException {
        log.info("/find/applications/{}  userTokenId:{}  - applicationTokenId:{}", applicationName, "missing", applicationTokenId);
        String applications = applicationsService.findApplications(applicationTokenId, applicationName);
        log.info("/find/applications/{} - returns:{}", applicationName, first50(applications));
        return Response.ok(applications).build();

    }

    @GET
	@Path("{userTokenId}/hasUASAccess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response hasUASAccess(@PathParam("applicationtokenid") String applicationTokenId,
			@PathParam("userTokenId") String userTokenId) throws AppException {
		
		boolean result = applicationsService.getAdminChecker().authorise(applicationTokenId, userTokenId);
        log.trace("check access for applicationTokenId={}/usertokenid={} - result: {}", applicationTokenId, userTokenId, result);
        return Response.ok("{\"result\":" + String.valueOf(result) + "}").build();
		

	}
	
	@GET
	@Path("hasUASAccess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response hasUASAccess(@PathParam("applicationtokenid") String applicationTokenId) throws AppException {
		
		boolean result = applicationsService.getAdminChecker().authorise(applicationTokenId);
        log.trace("check access for applicationTokenId={} result: {}", applicationTokenId, result);
        return Response.ok("{\"result\":" + String.valueOf(result) + "}").build();
		

	}
	

	
	//OLD
	
	@GET
	@Path("{userTokenId}/applications")
	@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
	public Response listAllOld(@PathParam("applicationtokenid") String applicationTokenId) throws AppException {
		log.trace("listAll is called ");

		String applications = applicationsService.listAll(applicationTokenId);
		log.trace("listAll {}", applications);
		return Response.ok(applications).build();
		
	}

	@GET
	@Path("{userTokenId}/applications/find/{applicationName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findByNameOld(@PathParam("applicationtokenid") String applicationTokenId,
			@PathParam("userTokenId") String userTokenId,
			@PathParam("applicationName") String applicationName) throws AppException {
        log.debug("{userTokenId}/applications/find/{}  userTokenId:{}  - applicationTokenId:{}", applicationName, userTokenId, applicationTokenId);
        log.trace("findByName - is called, query {}", applicationName);

		String applications = applicationsService.findApplications(applicationTokenId, userTokenId, applicationName);
		//            String applications = applicationsService.listAll(applicationTokenId, userTokenId);
		return Response.ok(applications).build();

	}

	@GET
    @Path("/applications/find/{applicationName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByNameNuToken(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("applicationName") String applicationName) throws AppException {
        log.debug("/applications/find/{}  userTokenId:{}  - applicationTokenId:{}", applicationName, "missing", applicationTokenId);
        log.trace("findByName - is called, query {}", applicationName);

        String applications = applicationsService.findApplications(applicationTokenId, UUID.randomUUID().toString(), applicationName);
        return Response.ok(applications).build();

    }

    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }
}
