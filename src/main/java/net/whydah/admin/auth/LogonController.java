package net.whydah.admin.auth;

import net.whydah.admin.auth.uib.UibAuthConnection;
import net.whydah.sso.user.mappers.UserCredentialMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/auth")
@Controller
public class LogonController {
    private static final Logger log = LoggerFactory.getLogger(LogonController.class);

    private final UibAuthConnection uibAuthConnection;

    @Autowired
    public LogonController(UibAuthConnection uibAuthConnection) {
        this.uibAuthConnection = uibAuthConnection;
    }


    @POST
    @Path("/logon/user")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response logonUser(@PathParam("applicationtokenid") String applicationTokenId, String userCredentialsXml) {
        log.trace("logon is called with usercredentialsXml={}", userCredentialsXml);

        // Block and return on empty username
        if (UserCredentialMapper.fromXml(userCredentialsXml).getUserName().length()<1){
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String userXml = uibAuthConnection.logonUser(applicationTokenId, userCredentialsXml);
        return Response.ok(userXml).build();
    }



}
