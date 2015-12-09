package net.whydah.admin.auth;

import net.whydah.sso.user.mappers.UserCredentialMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserCredential;
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
    @Path("logon")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response logon(@PathParam("applicationtokenid") String applicationTokenId, String usercredentialsXml) {
        log.trace("logon is called with usercredentialsXml={}", usercredentialsXml);
        String userToken = bulidStubUserToken();
            return Response.ok(userToken).build();
        //FIXME real implementation to UIB.
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
        // TODO This method should only be available for STS to use...
        if (!isSTS()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }


        String userXml = uibAuthConnection.logonUser(applicationTokenId, userCredentialsXml);
        return Response.ok(userXml).build();
    }


    private String bulidStubUserToken() {
        return "<xml><usertoken><params><name>admin</name></params></usertoken></xml>";
    }

    private boolean isSTS(){
        // TODO This method should only be available for STS to use...
        return true;
    }

}
