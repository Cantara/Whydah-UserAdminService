package net.whydah.admin.application;

import net.whydah.sso.commands.adminapi.application.CommandAuthenticateApplication;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-11-21.
 */
@Component
@Path("/{stsApplicationtokenId}/application/auth")
public class ApplicationAuthenticationEndpoint {
    private static final Logger log = LoggerFactory.getLogger(ApplicationAuthenticationEndpoint.class);
    private final String uibUri;
    private final String stsUri;


    @Autowired
    @Configure
    public ApplicationAuthenticationEndpoint(@Configuration("useridentitybackend") String uibUri, @Configuration("securitytokenservice") String stsUri) {
        this.uibUri = uibUri;
        this.stsUri = stsUri;
    }


    /**
     * Proxy for UIB application auth endpoint
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateApplication(@PathParam("stsApplicationtokenId") String stsApplicationtokenId,
                                            @FormParam(CommandAuthenticateApplication.UAS_APP_CREDENTIAL_XML) String uasAppCredentialXml,
                                            @FormParam(CommandAuthenticateApplication.APP_CREDENTIAL_XML) String appCredentialXml) {

        // verify stsApplicationtokenId
        Boolean stsAuthenticationOK = new CommandValidateApplicationTokenId(stsUri, stsApplicationtokenId).execute();
        if (!stsAuthenticationOK) {
            log.warn("Invalid securitytokenservice session. stsApplicationtokenId={}. Returning Forbidden.", stsApplicationtokenId);
            return Response.status(Response.Status.FORBIDDEN).build();
        }


        Response responseFromUib =
                new CommandAuthenticateApplication(uibUri, stsApplicationtokenId, uasAppCredentialXml, appCredentialXml).execute();
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
