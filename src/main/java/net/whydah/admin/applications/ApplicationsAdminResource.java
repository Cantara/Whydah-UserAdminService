package net.whydah.admin.applications;

import net.whydah.admin.errorhandling.AppException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Accessable in DEV mode:
 *  - http://localhost:9992/useradminservice/1/1/adminapplications
 *  - http://localhost:9992/useradminservice/1/1/adminapplications/1
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */


@Path("/{applicationtokenid}/adminapplications")
@Component
public class ApplicationsAdminResource {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsAdminResource.class);
    private final ApplicationsService applicationsService;


    @Autowired
    public ApplicationsAdminResource(ApplicationsService applicationsService) {
        this.applicationsService = applicationsService;
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAll(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId) throws AppException {
        log.trace("listAll(Admin) is called ");

        String applications = applicationsService.listAll(applicationTokenId);
        log.trace("Returning applicationlist as json \n",applications);
        return Response.ok(applications).build();
      
    }

    @GET
    @Path("/{applicationName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByName(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("applicationName") String applicationName) throws AppException {
    	log.trace("findByName(Admin) is called ");

    	String application = applicationsService.findApplication(applicationTokenId,userTokenId, applicationName);
    	return Response.ok(application).build();

    }

    @GET
    @Path("{userTokenId}//")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllOld(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId) throws AppException {
        log.trace("listAll(Admin) is called ");
        String applications = applicationsService.listAll(applicationTokenId);
        log.trace("Returning applicationlist as json \n",applications);
        return Response.ok(applications).build();

    }

    @GET
    @Path("{userTokenId}//{applicationName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByNameOld(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               @PathParam("applicationName") String applicationName) throws AppException {
        log.trace("findByName(Admin) is called ");

        String application = applicationsService.findApplication(applicationTokenId,userTokenId, applicationName);
        return Response.ok(application).build();

    }
    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }
}
