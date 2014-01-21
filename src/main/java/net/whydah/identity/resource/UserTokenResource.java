package net.whydah.identity.resource;

import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;
import net.whydah.identity.data.UserToken;
import net.whydah.identity.data.helper.ActiveUserTokenRepository;
import net.whydah.identity.data.helper.AuthenticatedApplicationRepository;
import net.whydah.identity.data.helper.UserAuthenticator;
import net.whydah.identity.exception.AuthenticationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("/uas")
public class UserTokenResource {
    private final static Logger logger = LoggerFactory.getLogger(UserTokenResource.class);

    private static Map ticketmap = new HashMap();

    @Inject
    private UserAuthenticator userAuthenticator;

    @Context
    UriInfo uriInfo;

    @Path("/usertokentemplate")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserTokenTemplate() {
        return Response.ok(new Viewable("/usertoken.ftl", new UserToken())).build();
    }


    @Path("/{applicationtokenid}/{ticketid}/createuser")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response createAndLogOnUser(@PathParam("applicationtokenid") String applicationtokenid,
                                 @PathParam("ticketid") String ticketid,
                                 @FormParam("apptoken") String appTokenXml,
                                 @FormParam("usercredential") String userCredentialXml,
                                 @FormParam("fbuser") String fbUserXml) {

        if (!verifyApptoken(applicationtokenid, appTokenXml)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Application authentication not valid.").build();
        }

        try {
            UserToken token = userAuthenticator.createAndLogonUser(appTokenXml, userCredentialXml, fbUserXml);
            ticketmap.put(ticketid, token.getTokenid());
            return Response.ok(new Viewable("/usertoken.ftl", token)).build();
        } catch (AuthenticationFailedException ae) {
            return Response.status(Response.Status.FORBIDDEN).entity("Error creating or authenticating user.").build();
        }
    }





    @Path("/{applicationtokenid}/validateusertokenid/{usertokenid}")
    @GET
    public Response validateUserTokenID(@PathParam("applicationtokenid") String applicationtokenid, @PathParam("usertokenid") String usertokenid) {
        if(!AuthenticatedApplicationRepository.verifyApplicationTokenId(applicationtokenid)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Application authentication not valid.").build();
        }
        if (ActiveUserTokenRepository.getUserToken(usertokenid) != null) {
            logger.debug("Verified {}", usertokenid);
            return Response.ok().build();
        }
        logger.debug("Usertoken not ok: {}", usertokenid);
        return Response.status(Response.Status.CONFLICT).build();
    }



    private boolean verifyApptoken(String apptokenid, String appTokenXml) {
        return appTokenXml.contains(apptokenid) && AuthenticatedApplicationRepository.verifyApplicationToken(appTokenXml);
    }
}
