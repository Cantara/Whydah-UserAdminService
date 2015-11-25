package net.whydah.admin.application;

import net.whydah.admin.security.UASCredentials;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
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
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/application")
@Component
public class ApplicationResource {
    private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

    private static final String APPLICATION_PATH = "application";
    private final WebTarget uib;
    private final UASCredentials uasCredentials;


    @Autowired
    @Configure
    public ApplicationResource(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        Client client = ClientBuilder.newClient();
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
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
        Response response = copyResponse(responseFromUib);
        return response;
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
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        String jsonResult = responseFromUib.readEntity(String.class);
        log.trace("Received jsonResult {}", jsonResult);
//        Response response = copyResponse(responseFromUib);
//        return response;
        return Response.ok(jsonResult).build();
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
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
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
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        return copyResponse(responseFromUib);
    }


    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }
}
