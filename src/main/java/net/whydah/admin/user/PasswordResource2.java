package net.whydah.admin.user;

import net.whydah.sso.commands.userauth.CommandChangeUserPasswordUsingToken;
import net.whydah.sso.commands.userauth.CommandResetUserPassword;
import org.constretto.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Jax-RS resource responsible for user password management.
 * See also https://wiki.cantara.no/display/whydah/Password+management.
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-11-15.
 */
@Component
@Path("/{applicationtokenid}")
public class PasswordResource2 {
    private static final Logger log = LoggerFactory.getLogger(PasswordResource2.class);
    private final String uibUri;


    @Autowired
    public PasswordResource2(@Configuration("useridentitybackend") String uibUri) {
        this.uibUri = uibUri;
    }


    /**
     * Proxy for resetPassword
     */
    @POST
    @Path("/user/{uid}/reset_password")
    public Response resetPassword(@PathParam("applicationtokenid") String applicationtokenid, @PathParam("uid") String uid) {
        log.info("Reset password for uid={} using applicationtokenid={}", uid, applicationtokenid);
        Response response = new CommandResetUserPassword(uibUri, applicationtokenid, uid).execute();
        return copyResponse(response);
    }


    /**
     * Proxy for authenticateAndChangePasswordUsingToken
     */
    @POST
    @Path("/user/{uid}/change_password")
    public Response authenticateAndChangePasswordUsingToken(@PathParam("applicationtokenid") String applicationtokenid,
                                                            @PathParam("uid") String uid,
                                                            @QueryParam("changePasswordToken") String changePasswordToken,
                                                            String json) {
        log.info("authenticateAndChangePasswordUsingToken for uid={} using applicationtokenid={}", uid, applicationtokenid);
        Response response =
                new CommandChangeUserPasswordUsingToken(uibUri, applicationtokenid, uid, changePasswordToken, json).execute();
        return copyResponse(response);

    }

    private Response copyResponse(Response responseFromUib) {
        Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
        if (responseFromUib.hasEntity()) {
            rb.entity(responseFromUib.getEntity());
        }
        return rb.build();
    }
}
