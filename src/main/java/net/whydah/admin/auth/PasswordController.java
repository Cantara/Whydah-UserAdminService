package net.whydah.admin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.auth.uib.UibAuthConnection;
import net.whydah.admin.createlogon.UserAction;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.sso.internal.commands.uib.userauth.CommandResetUserPasswordUAS;
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

    /**
     * @Ignore Can do via SSO log on service
  	 * @throws AppException 
     * @throws Exception 
     * @api {post} :applicationtokenid}/auth/password/reset/username/:username Reset password
  	 * @apiName reset
  	 * @apiGroup User Admin Service (UAS)
  	 * @apiDescription A link sent to the registered email provides user a way to change/reset their password 
  	 * 
  	 *
  	 * @apiSuccessExample Success-Response:
  	 *	HTTP/1.1 200 OK plain/text
  	 *  :username (it is the same username as a part of the request)
  	 *
  	 * @apiError 500/9999 A generic exception or an unexpected error 
  	 * @apiError 202/8004 Unable to send reset password notification to user
  	 * 
  	 * @apiErrorExample Error-Response:
   	 * HTTP/1.1 202 Accepted
   	 * {
   	 *  	"status": 202,
   	 *  	"code": 8004,
   	 *  	"message": "Unable to send reset password notification to user",
   	 *  	"link": "",
   	 *  	"developerMessage": ""
   	 * }
  	 * 
  	 */
    @POST
    @Path("/{applicationtokenid}/auth/password/{applicationtokenid}/auth/password/reset/username/{username}")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username) throws AppException {
        log.trace("reset username={}", username);
        try {
            boolean passwordResetOk = false;

            if (false) {  // TODO BLI FIXME  :)
                // Lookup username to find uid
                String uid = null;//new CommandListUsers().execute();
                String response = new CommandResetUserPasswordUAS(uibAuthConnection.getUIBUri(), applicationTokenId, uid).execute();
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
                //return Response.accepted("Unable to send reset password notification to user").build();
                throw AppExceptionCode.APP_UNABLE_TO_RESET_PASSWORD_8004;
            }
        } catch (Exception e) {
            //return Response.accepted("Unable to send reset password notification to user").build();
        	throw AppExceptionCode.APP_UNABLE_TO_RESET_PASSWORD_8004.setDeveloperMessage(e.getMessage());

        }
    }

    /**
     * @apiIgnore Internal
  	 * @throws AppException 
     * @throws Exception 
     * @api {post} :applicationtokenid}/auth/password/reset/username/:username/template/:resetPasswordTemplateName Reset password with a template
  	 * @apiName resetWithTemplate
  	 * @apiGroup User Admin Service (UAS)
  	 * @apiDescription Reset password with a template
  	 * 
  	 *
  	 * @apiSuccessExample Success-Response:
  	 *	HTTP/1.1 200 OK plain/text
  	 *  :username (it is the same username as a part of the request)
  	 *
  	 * @apiError 500/9999 A generic exception or an unexpected error 
  	 * @apiError 202/8004 Unable to send reset password notification to user
  	 * 
  	 * @apiErrorExample Error-Response:
   	 * HTTP/1.1 202 Accepted
   	 * {
   	 *  	"status": 202,
   	 *  	"code": 8004,
   	 *  	"message": "Unable to send reset password notification to user",
   	 *  	"link": "",
   	 *  	"developerMessage": ""
   	 * }
  	 * 
  	 */
    @POST
    @Path("/{applicationtokenid}/auth/password/reset/username/{username}/template/{resetPasswordTemplateName}")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username, @PathParam("resetPasswordTemplateName") String resetPasswordTemplateName) {
        log.trace("reset username={}, resetPasswordTemplateName:{}", username, resetPasswordTemplateName);
        try {
            boolean passwordResetOk = false;

            if (false) {  // TODO BLI FIXME  :)
                // Lookup username to find uid
                String uid = null;//new CommandListUsers().execute();
                String response = new CommandResetUserPasswordUAS(uibAuthConnection.getUIBUri(), applicationTokenId, uid).execute();
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
        } catch (Exception e) {
            return Response.accepted("Unable to send reset password notification to user").build();

        }
    }

    /**
     * @throws AppException 
     * @apiIgnore Internal
  	 */
    @POST
    @Path("/{applicationtokenid}/auth/password/reset/username/{username}/newpassword/{passwordChangeToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetNewPW(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("username") String username, @PathParam("passwordChangeToken") String passwordChangeToken, String newPasswordJson) throws AppException {

        log.trace("resetNewPW - username={}", username);
        String password = null;
        try {
            Map<String, String> newPasswordMap = objectMapper.readValue(newPasswordJson, Map.class);
            password = newPasswordMap.get("newpassword");
            String userIdentity = uibAuthConnection.setPasswordByToken(applicationTokenId, username, passwordChangeToken, password);
            return Response.ok(userIdentity).build();
        } catch (IOException e) {
            log.trace("Failed to parse inncomming newPasswordJson {}", newPasswordJson);
        }
        return Response.accepted(newPasswordJson).build();

    }

    private Response copyResponse(String responseFromUib) {
        Response.ResponseBuilder rb;
        if (responseFromUib != null && responseFromUib.length() > 100) {
            rb = Response.status(200);
            rb.entity(responseFromUib);

        } else {
            rb = Response.status(500);
        }
        return rb.build();
    }
}
