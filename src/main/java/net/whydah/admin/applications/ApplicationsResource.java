package net.whydah.admin.applications;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
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
 *  - http://localhost:9992/useradminservice/1/1/applications
 *  - http://localhost:9992/useradminservice/1/1/applications/1
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */

@Path("/{applicationtokenid}/applications")
@Component
public class ApplicationsResource {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsResource.class);
    private final ApplicationsService applicationsService;

    @Autowired
    public ApplicationsResource(ApplicationsService applicationsService) {
        this.applicationsService = applicationsService;
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=utf-8")
    public Response listAll(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId) {
        log.trace("listAll is called ");
        try {
            String applications = applicationsService.listAll(applicationTokenId, userTokenId);
            log.trace("listAll {}", applications);
            return Response.ok(applications).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("Failed to list all.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/find/{applicationName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByName(@PathParam("applicationtokenid") String applicationTokenId,
                            @PathParam("applicationName") String applicationName) {
        log.trace("findByName - listAll is called, query {}",applicationName);
        try {
            String applications = applicationsService.findApplication(applicationTokenId,applicationName);
//            String applications = applicationsService.listAll(applicationTokenId, userTokenId);
            return Response.ok(applications).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("Failed to list all.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("{userTokenId}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=utf-8")
    public Response listAllOld(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId) {
        log.trace("listAll is called ");
        try {
            String applications = applicationsService.listAll(applicationTokenId, userTokenId);
            log.trace("listAll {}", applications);
            return Response.ok(applications).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("Failed to list all.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("{userTokenId}/find/{applicationName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByNameOld(@PathParam("applicationtokenid") String applicationTokenId,
                               @PathParam("applicationName") String applicationName) {
        log.trace("findByName - listAll is called, query {}",applicationName);
        try {
            String applications = applicationsService.findApplication(applicationTokenId,applicationName);
//            String applications = applicationsService.listAll(applicationTokenId, userTokenId);
            return Response.ok(applications).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("Failed to list all.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }
}
