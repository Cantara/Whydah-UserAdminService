package net.whydah.admin.auth;

import net.whydah.admin.createlogon.UserAction;
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
@Path("/{applicationtokenid}/auth/password")
@Controller
public class PasswordController {
    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    private final UibAuthConnection uibAuthConnection;
    private final AuthenticationService authenticationService;

    @Autowired
    public PasswordController(UibAuthConnection uibAuthConnection, AuthenticationService authenticationService) {
        this.uibAuthConnection = uibAuthConnection;
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/reset/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username) {
        log.trace("reset username={}", username);
        boolean passwordResetOk = false;
//        String userToken = uibAuthConnection.resetPassword(applicationTokenId, username);
        if (username != null && !username.isEmpty()) {
             passwordResetOk = authenticationService.resetPassword(applicationTokenId,username, UserAction.EMAIL);
//            String resetPasswordToken = uibAuthConnection.resetPassword(applicationTokenId, username);
//            3.Send email or sms pin (STS)
//            boolean notificationSent = sendNotification (createdUser, userAction, resetPasswordToken);
//            if (notificationSent) {
//                passwordResetToken = resetPasswordToken;
//            }
        }
        if (passwordResetOk) {
            log.trace("Password reset ok. Username {}", username);
            return Response.ok(username).build();
        } else {
            log.trace("Password reset failed. Username {}", username);
            return Response.accepted().build();
        }
    }


    @POST
    @Path("/reset/username/{username}/newpassword/{passwordChangeToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetNewPW(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username,@PathParam("passwordChangeToken") String passwordChangeToken,@FormParam("password") String password) {

        log.trace("resetNewPW - username={}", username);
        String userToken = uibAuthConnection.setPasswordByToken(applicationTokenId, username, passwordChangeToken,password);
        return Response.ok(username).build();
    }


}
