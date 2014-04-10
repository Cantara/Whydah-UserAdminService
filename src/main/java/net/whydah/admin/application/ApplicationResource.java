package net.whydah.admin.application;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/application")
public class ApplicationResource {
    private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);
    ApplicationService applicationService;
    ObjectMapper mapper = new ObjectMapper();


    @Autowired
    public ApplicationResource(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Create a new applcation from json
     * Add default
     *
     * @param applicationXml json representing an Application
     * @return Application
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createApplication(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, String applicationXml) {
        log.trace("createApplication is called with applicationXml={}", applicationXml);
        Application application;
        try {
            application = applicationService.createApplicationFromXml(applicationTokenId, userTokenId, applicationXml);

        } catch (IllegalArgumentException iae) {
            log.error("createApplication: Invalid xml={}", applicationXml, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (application != null) {
            String applicationCreatedXml = application.toXML();
            return Response.ok(applicationCreatedXml).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @GET
    @Path("/{applicationId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getApplication(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                   @PathParam("applicationId") String applicationId) {
        log.trace("getApplication is called with applicationId={}", applicationId);
        try {
            Application application = applicationService.getApplication(applicationTokenId, userTokenId,applicationId);
            String applicationCreatedXml = buildApplicationXml(application);
            return Response.ok(applicationCreatedXml).build();
        } catch (IllegalArgumentException iae) {
            log.error("createApplication: Invalid xml={}", applicationId, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    public Response ping() {
        return Response.ok("pong").build();
    }

    protected String buildApplicationJson(Application application) {
        String applicationCreatedJson = null;
        try {
            applicationCreatedJson = mapper.writeValueAsString(application);
        } catch (IOException e) {
            log.warn("Could not convert application to Json {}", application.toString());
        }
        return applicationCreatedJson;
    }
    protected String buildApplicationXml(Application application) {
        String applicationCreatedXml = null;
        if (application != null) {
            applicationCreatedXml = application.toXML();
        }
        return applicationCreatedXml;
    }
}
