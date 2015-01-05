package net.whydah.admin.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @POST
    @Path("/logon")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response logon(@PathParam("applicationtokenid") String applicationTokenId, String credentialsXml) {
        log.trace("logon is called with credentialsXml={}", credentialsXml);
        String userToken = bulidStubUserToken();
            return Response.ok(userToken).build();
        //FIXME real implementation to UIB.
        }

    private String bulidStubUserToken() {
        return "<xml><usertoken><params><name>admin</name></params></usertoken></xml>";
    }


}
