package net.whydah.admin.application;

import net.whydah.admin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * Proxy in front of UIB application CRUD endpoint.
 *
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/application")
@Component
public class ApplicationResource {
    private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

    private static final String APPLICATION_PATH = "application";
    private final WebTarget uib;


    @Autowired
    public ApplicationResource(AppConfig appConfig) {
        Client client = ClientBuilder.newClient();
        String uibUrl = appConfig.getProperty("useridentitybackend");
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("userTokenId") String userTokenId,
                                      String applicationJson)  {
        log.trace("create is called with applicationJson={}", applicationJson);

        WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH);
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
        return copyResponse(responseFromUib);
    }

    private Response copyResponse(Response responseFromUib) {
        Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
        if (responseFromUib.hasEntity()) {
            rb.entity(responseFromUib.getEntity());
        }
        return rb.build();
    }

    @GET
    @Path("/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                   @PathParam("userTokenId") String userTokenId,
                                   @PathParam("applicationId") String applicationId){
        log.trace("getApplication is called with applicationId={}", applicationId);
        WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH).path(applicationId);
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).get();
        return copyResponse(responseFromUib);
    }

    @PUT
    @Path("/{applicationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("userTokenId") String userTokenId,
                                      @PathParam("applicationId") String applicationId,
                                      String applicationJson)  {
        log.trace("updateApplication applicationId={}, applicationJson={}", applicationId, applicationJson);
        WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH).path(applicationId);
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).put(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
        return copyResponse(responseFromUib);
    }

    @DELETE
    @Path("/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApplication(@PathParam("applicationtokenid") String applicationTokenId,
                                      @PathParam("userTokenId") String userTokenId,
                                      @PathParam("applicationId") String applicationId){
        log.trace("deleteApplication is called with applicationId={}", applicationId);

        WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path(APPLICATION_PATH).path(applicationId);
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).delete();
        return copyResponse(responseFromUib);
    }



    /*
    //TODO: Enhance due to https://github.com/Cantara/Whydah-UserAdminService/issues/20
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

   //TODO enchance due to https://github.com/Cantara/Whydah-UserAdminService/issues/20
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

    protected String buildApplicationXml(Application application) {
        String applicationCreatedXml = null;
        if (application != null) {
            applicationCreatedXml = application.toXML();
        }
        return applicationCreatedXml;
    }
    */

    /**
     * Enhance due to https://github.com/Cantara/Whydah-UserAdminService/issues/20
     * Create a new applcation from json
     * Add default
     *
     * @param applicationXml json representing an Application
     * @return Application
     */
    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response authenticateApplication(@PathParam("applicationtokenid") String applicationTokenId,  String applicationXml) {
        log.trace("authenticateApplication is called with applicationXml={} from applicationtokenid={}", applicationXml,applicationTokenId);

        // FIXME verify that the request come from STS, which is the only application who has access to auth
        // FIXME ask UIB for to verify applicationSecret
        // FIXME Build and return application.toXML()

        boolean authOK=true;
        if (authOK) {
            String applicationCreatedXml ="";// application.toXML();
            return Response.ok(applicationCreatedXml).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }


    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    public Response ping() {
        return Response.ok("pong").build();
    }
}
