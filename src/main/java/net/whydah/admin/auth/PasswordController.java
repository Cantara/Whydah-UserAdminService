package net.whydah.admin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.auth.uib.UibAuthConnection;
import net.whydah.admin.createlogon.UserAction;
import net.whydah.sso.internal.commands.uib.userauth.CommandResetUserPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/auth/password")
@Controller
public class PasswordController {
    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    private final UibAuthConnection uibAuthConnection;
    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PasswordController(UibAuthConnection uibAuthConnection, AuthenticationService authenticationService, ObjectMapper objectMapper) {
        this.uibAuthConnection = uibAuthConnection;
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
    }

    @POST
    @Path("/reset/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username) {
        log.trace("reset username={}", username);
        boolean passwordResetOk = false;

        if (false) {  // TODO BLI FIXME  :)
        // Lookup username to find uid
        String uid = null;//new CommandListUsers().execute();
        String response = new CommandResetUserPassword(uibAuthConnection.getUIBUri(), applicationTokenId, uid).execute();
        return copyResponse(response);
        }

//        String userToken = uibAuthConnection.resetPassword(applicationTokenId, username);
        if (username != null && !username.isEmpty()) {
            passwordResetOk = authenticationService.resetPassword(applicationTokenId, username, UserAction.EMAIL, "");
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
            return Response.accepted("Unable to send reset password notification to user").build();
        }
    }

    @POST
    @Path("/reset/username/{username}/{resetPasswordTemplateName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username, @PathParam("resetPasswordTemplateName") String resetPasswordTemplateName) {
        log.trace("reset username={}, resetPasswordTemplateName:{}", username, resetPasswordTemplateName);
        boolean passwordResetOk = false;

        if (false) {  // TODO BLI FIXME  :)
            // Lookup username to find uid
            String uid = null;//new CommandListUsers().execute();
            String response = new CommandResetUserPassword(uibAuthConnection.getUIBUri(), applicationTokenId, uid).execute();
            return copyResponse(response);
        }

//        String userToken = uibAuthConnection.resetPassword(applicationTokenId, username);
        if (username != null && !username.isEmpty()) {
            passwordResetOk = authenticationService.resetPassword(applicationTokenId, username, UserAction.EMAIL, resetPasswordTemplateName);
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
            return Response.accepted("Unable to send reset password notification to user").build();
        }
    }

    @POST
    @Path("/reset/username/{username}/newpassword/{passwordChangeToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetNewPW(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username,@PathParam("passwordChangeToken") String passwordChangeToken,String newPasswordJson) {

        log.trace("resetNewPW - username={}", username);
        String password = null;
        try {
            Map<String,String> newPasswordMap = objectMapper.readValue(newPasswordJson, Map.class);
            password = newPasswordMap.get("newpassword");
            String userToken = uibAuthConnection.setPasswordByToken(applicationTokenId, username, passwordChangeToken,password);
            return Response.ok(username).build();
        } catch (IOException e) {
            log.trace("Failed to parse inncomming newPasswordJson {}", newPasswordJson);
        }
        return Response.accepted(newPasswordJson).build();

    }

    private Response copyResponse(String responseFromUib) {
        Response.ResponseBuilder rb;
        if (responseFromUib!=null && responseFromUib.length()>100){
            rb = Response.status(200);
            rb.entity(responseFromUib);

        } else {
            rb = Response.status(500);
        }
        return rb.build();
    }
}
