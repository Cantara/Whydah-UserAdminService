package net.whydah.admin.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/auth/password")
@Controller
public class PasswordController {
    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    private final UibAuthConnection uibAuthConnection;

    @Autowired
    public PasswordController(UibAuthConnection uibAuthConnection) {
        this.uibAuthConnection = uibAuthConnection;
    }

    @GET
    @Path("/reset/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username) {
        log.trace("username is called username={}", username);
        String userToken = uibAuthConnection.resetPassword(applicationTokenId, username);
        return Response.ok(username).build();
        //FIXME real implementation to UIB.
    }
}
