package net.whydah.admin.application;

import net.whydah.sso.application.mappers.ApplicationCredentialMapper;
import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.commands.adminapi.application.CommandAuthenticateApplicationUAS;
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
    private final ApplicationCredential uasApplicationCredential;


    @Autowired
    @Configure
    public ApplicationAuthenticationEndpoint(@Configuration("useridentitybackend") String uibUri,
                                             @Configuration("securitytokenservice") String stsUri,
                                             @Configuration("applicationid") String applicationid,
                                             @Configuration("applicationname") String applicationname,
                                             @Configuration("applicationsecret") String applicationsecret) {
        this.uibUri = uibUri;
        this.stsUri = stsUri;
        this.uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
    }


    /**
     * Proxy for UIB application auth endpoint
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateApplication(@PathParam("stsApplicationtokenId") String callingApplicationtokenId,
                                            @FormParam(CommandAuthenticateApplicationUAS.APP_CREDENTIAL_XML) String appCredentialXml) {

        log.info("authenticateApplication - trying to authenticate applicationcredential: {}  callingApplicationtokenId: {}  stsUri: {} ",appCredentialXml, callingApplicationtokenId,stsUri);

        // verify stsApplicationtokenId
        Boolean stsAuthenticationOK =   new CommandValidateApplicationTokenId(stsUri, callingApplicationtokenId).execute();
        if (!stsAuthenticationOK) {
            log.warn("Invalid securitytokenservice session. callingApplicationtokenId={}. Returning Forbidden.", callingApplicationtokenId);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String uasAppCredentialXml = ApplicationCredentialMapper.toXML(uasApplicationCredential);
        Response responseFromUib =
                new CommandAuthenticateApplicationUIB(uibUri, callingApplicationtokenId, uasAppCredentialXml, appCredentialXml).execute();
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
