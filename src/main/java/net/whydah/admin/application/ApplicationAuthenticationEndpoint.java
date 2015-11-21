package net.whydah.admin.application;

import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-11-21.
 */
@Component
@Path("/{stsApplicationtokenId}/application/auth")
public class ApplicationAuthenticationEndpoint {
    private static final String UAS_APP_CREDENTIAL_XML = "uasAppCredentialXml";
    private static final String APP_CREDENTIAL_XML = "appCredentialXml";
    private static final String APPLICATION_AUTH_PATH = "application/auth";
    private static final Logger log = LoggerFactory.getLogger(ApplicationAuthenticationEndpoint.class);
    private final WebTarget uib;
    private final String stsUri;


    @Autowired
    @Configure
    public ApplicationAuthenticationEndpoint(@Configuration("useridentitybackend") String uibUrl, @Configuration("securitytokenservice") String stsUri) {
        log.debug("Connection to UserIdentityBackend on {}", uibUrl);
        Client client = ClientBuilder.newClient();
        this.uib = client.target(uibUrl);
        this.stsUri = stsUri;
    }


    /**
     * Proxy for UIB application auth endpoint
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateApplication(@PathParam("stsApplicationtokenId") String stsApplicationtokenId,
                                            @FormParam(UAS_APP_CREDENTIAL_XML) String uasAppCredentialXml,
                                            @FormParam(APP_CREDENTIAL_XML) String appCredentialXml) {

        // verify stsApplicationtokenId
        Boolean stsAuthenticationOK = new CommandValidateApplicationTokenId(stsUri, stsApplicationtokenId).execute();
        if (!stsAuthenticationOK) {
            log.warn("Invalid securitytokenservice session. stsApplicationtokenId={}. Returning Forbidden.", stsApplicationtokenId);
            return Response.status(Response.Status.FORBIDDEN).build();
        }


        //call UIB
        WebTarget webResource = uib.path(stsApplicationtokenId).path(APPLICATION_AUTH_PATH);
        MultivaluedMap<String,String> formData = new MultivaluedHashMap<>(2);
        formData.add(UAS_APP_CREDENTIAL_XML, uasAppCredentialXml);
        formData.add(APP_CREDENTIAL_XML, appCredentialXml);
        Response responseFromUib = webResource.request(MediaType.APPLICATION_FORM_URLENCODED)
                                              .post(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED));
        return copyResponse(responseFromUib);
    }


    private Response copyResponse(Response responseFromUib) {
        Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
        if (responseFromUib.hasEntity()) {
            rb.entity(responseFromUib.getEntity());
        }
        return rb.build();
    }
}
