package net.whydah.admin.application;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.application.uib.CommandAuthenticateApplicationUIB;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.sso.application.mappers.ApplicationCredentialMapper;
import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.internal.commands.uib.adminapi.application.CommandAuthenticateApplicationUAS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    public ApplicationAuthenticationEndpoint(@Value("${useridentitybackend}") String uibUri,
                                             @Value("${securitytokenservice}") String stsUri,
                                             @Value("${applicationid}") String applicationid,
                                             @Value("${applicationname}") String applicationname,
                                             @Value("${applicationsecret}") String applicationsecret) {
        this.uibUri = uibUri;
        this.stsUri = stsUri;
        this.uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
    }


    /**
     * @throws AppException
     * @api {post} :stsApplicationtokenId/application/auth authenticateApplication
     * @apiName authenticateApplication
     * @apiGroup User Admin Service (UAS)
     * @apiDescription Validate an application from a request of STS
     *
     * @apiParam {String} appCredentialXml A label for this address.
     * @apiParamExample {xml} Request-Example:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * &lt;applicationcredential&gt;
     * &lt;params&gt;
     * &lt;applicationID&gt;101&lt;/applicationID&gt;
     *  &lt;applicationName&gt;Whydah-SystemTests&lt;/applicationName&gt;
     *   &lt;applicationSecret&gt;55fhRM6nbKZ2wfC6RMmMuzXpk&lt;/applicationSecret&gt;
     * &lt;/params&gt;
     * &lt;/applicationcredential&gt;
     *
     * @apiSuccessExample Success-Response:
     *	HTTP/1.1 204 No Content
     *
     *
     * @apiError 403/8000 Illegal Token Service
     * @apiError 500/9999 A generic exception or an unexpected error
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 403 Forbidden
     *     {
     *  		"status": 403,
     *  		"code": 8000,
     *  		"message": "Illegal Token Service.",
     *  		"link": "",
     *  		"developerMessage": "Illegal Token Service."
     *        }
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateApplication(@PathParam("stsApplicationtokenId") String callingApplicationtokenId,
                                            @FormParam(CommandAuthenticateApplicationUAS.APP_CREDENTIAL_XML) String appCredentialXml) throws AppException {

        log.info("authenticateApplication - trying to authenticate applicationcredential: {}  callingApplicationtokenId: {}  stsUri: {} ",appCredentialXml, callingApplicationtokenId,stsUri);

        // verify stsApplicationtokenId
//        Boolean stsAuthenticationOK =   new CommandValidateApplicationTokenId(stsUri, callingApplicationtokenId).execute();
//        if (!stsAuthenticationOK) {
//            log.warn("Invalid securitytokenservice session. callingApplicationtokenId={}. Returning Forbidden.", callingApplicationtokenId);
//            //return Response.status(Response.Status.FORBIDDEN).build();
//            throw AppExceptionCode.STSAPP_ILLEGAL_8000;
//        }

        String uasAppCredentialXml = ApplicationCredentialMapper.toXML(uasApplicationCredential);
        Response responseFromUib =
                new CommandAuthenticateApplicationUIB(uibUri, callingApplicationtokenId, uasAppCredentialXml, appCredentialXml).execute();
        return copyResponse(responseFromUib);
    }


    private Response copyResponse(Response responseFromUib) {
        Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
        log.info("Status from UIB:" + responseFromUib.getStatusInfo());
        if (responseFromUib.hasEntity()) {
            rb.entity(responseFromUib.getEntity());
            log.info("Entity from UIB:" + responseFromUib.getEntity());
        }
        return rb.build();
    }
}