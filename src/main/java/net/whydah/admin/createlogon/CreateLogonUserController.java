package net.whydah.admin.createlogon;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.createlogon.uib.UibCreateLogonConnection;
import net.whydah.admin.extras.ScheduledSendEMailTask;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}")
@Component
public class CreateLogonUserController {
    private static final Logger log = LoggerFactory.getLogger(CreateLogonUserController.class);

    private final UibCreateLogonConnection uibConnection;
    private final SignupService signupService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CreateLogonUserController(UibCreateLogonConnection uibConnection, SignupService signupService, ObjectMapper objectMapper) {
        this.uibConnection = uibConnection;
        this.signupService = signupService;
        this.objectMapper = objectMapper;
    }


    /*
    @Path("/authenticateee/user/createandlogon")
    public Response tempCreateAndLogonUser(@PathParam("applicationtokenid") String applicationtokenid, String fbUserXml) {
        return createAndLogonUser(applicationtokenid, fbUserXml);
    }
    */


    /**
     * @param applicationtokenid
     * @param fbUserXml          lookinc simmilar to this
     *                           <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     *                           <user>
     *                           <params>
     *                           <fbAccessToken>accessMe1234567</fbAccessToken>
     *                           <userId>null</userId>
     *                           <firstName>null</firstName>
     *                           <lastName>null</lastName>
     *                           <username>null</username>
     *                           <gender>null</gender>
     *                           <email>null</email>
     *                           <birthday>null</birthday>
     *                           <hometown>null</hometown>
     *                           </params>
     *                           </user>
     * @return
     */
    @POST
    @Path("/create_logon_facebook_user")
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
    public Response createAndLogonUser(@PathParam("applicationtokenid") String applicationtokenid, @PathParam("usertokenid") String userTokenId, String fbUserXml) {
        log.trace("Try to create user from facebookUserXml {}", fbUserXml);
        Response response = null;
        String userCreatedXml = null;
        try {
            userCreatedXml = uibConnection.createUser(applicationtokenid, fbUserXml);
            response = Response.ok(userCreatedXml).build();
        } catch (AuthenticationFailedException e) {
            log.trace("Failed to create user with applicationtokenid {}, facebookUserXml: {}", applicationtokenid, fbUserXml);
            response = Response.serverError().build();
        }
        return response;

    }

    @POST
    @Path("/signup")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response signup(@PathParam("applicationtokenid") String applicationtokenid, String userJson) {
        log.trace("Try to create user from json {}", userJson);
        UserAction userAction = UserAction.EMAIL;


        Response response = null;
        try {
            UserIdentity signupUser = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
            String passwordResetToken = signupService.signupUser(applicationtokenid, signupUser, userAction);
            String responseJson = "{\"resetPasswordToken\": \"" + passwordResetToken + "\"}";
            if (passwordResetToken != null) {
                response = Response.ok(responseJson).build();
            } else {
                log.debug("UserIdentityDeprecated was not created. Input Json {}", userJson);
                response = Response.status(Status.PRECONDITION_FAILED).build();
            }
        } catch (AuthenticationFailedException e) {
            log.trace("Failed to create user with applicationtokenid {}, userJson: {}", applicationtokenid, userJson);
            response = Response.serverError().build();
        }
        return response;

    }

    @POST
    @Path("/signup/{userAction}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response signupUserAction(@PathParam("applicationtokenid") String applicationtokenid, @PathParam("userAction") String userActionInput, String userJson) {
        log.trace("Try to create user from json {}", userJson);
        UserAction userAction = UserAction.EMAIL;
        if (userActionInput != null && userActionInput.trim().toUpperCase().equals(UserAction.PIN.name())) {
            userAction = UserAction.PIN;
        }

        Response response = null;
        try {
            UserIdentity signupUser = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
            String passwordResetToken = signupService.signupUser(applicationtokenid, signupUser, userAction);
            if (passwordResetToken != null) {
                response = Response.ok(passwordResetToken).build();
            } else {
                log.debug("UserIdentityDeprecated was not created. Input Json {}, userAction {}", userJson, userAction);
                response = Response.status(Status.PRECONDITION_FAILED).build();
            }
        } catch (AuthenticationFailedException e) {
            log.trace("Failed to create user with applicationtokenid {}, userJson: {}", applicationtokenid, userJson);
            response = Response.serverError().build();
        }
        return response;

    }


    @Path("/send_scheduled_email")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON})
    public Response send_scheduled_email(@PathParam("applicationtokenid") String applicationtokenid,
                                         @FormParam("timestamp") String timestamp,
                                         @FormParam("emailaddress") String emailaddress,
                                         @FormParam("subject") String subject,
                                         @FormParam("emailMessage") String emailMessage) {
        log.info("send_scheduled_email - Try to schedule mail user with emailaddress {}", emailaddress);
        new ScheduledSendEMailTask(Long.parseLong(timestamp),emailaddress,subject,emailMessage);
        return Response.ok("email scheduled").build();

    }

}
