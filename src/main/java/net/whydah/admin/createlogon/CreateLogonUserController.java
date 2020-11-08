package net.whydah.admin.createlogon;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.createlogon.uib.UibCreateLogonConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserIdentity;

import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final MailService mailService;

    @Autowired
    @Configure
    public CreateLogonUserController(UibCreateLogonConnection uibConnection, SignupService signupService, ObjectMapper objectMapper, MailService mailService) {
        this.uibConnection = uibConnection;
        this.signupService = signupService;
        this.objectMapper = objectMapper;
        this.mailService = mailService;
    }


    /*
    @Path("/authenticateee/user/createandlogon")
    public Response tempCreateAndLogonUser(@PathParam("applicationtokenid") String applicationtokenid, String fbUserXml) {
        return createAndLogonUser(applicationtokenid, fbUserXml);
    }
    */


    /**
     * @param applicationtokenid
     * @param userXml          lookinc simmilar to this
     *                           <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     *                           <user>
     *                           <params>
     *                          
     *                           <userId>null</userId>
     *                           <firstName>null</firstName>
     *                           <lastName>null</lastName>
     *                           <username>null</username>     
     *                           <email>null</email>
     *                           <cellPhone>null</cellPhone>
     *                           </params>
     *                           </user>
     * @return
     */
    @POST
    @Path("/createandlogon")
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
    public Response createAndLogonUser(@PathParam("applicationtokenid") String applicationtokenid, String userXml) {
        log.trace("Try to create user from xml {}", userXml);
        Response response = null;
        String userCreatedXml = null;
        try {
            userCreatedXml = uibConnection.createUser(applicationtokenid, userXml);
            response = Response.ok(userCreatedXml).build();
        } catch (AuthenticationFailedException e) {
            log.trace("Failed to create user with applicationtokenid {}, facebookUserXml: {}", applicationtokenid, userXml);
            response = Response.serverError().build();
        }
        return response;

    }

    @POST
    @Path("/signup")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response signup(@PathParam("applicationtokenid") String applicationtokenid, String userJson) throws AppException {
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
    public Response signupUserAction(@PathParam("applicationtokenid") String applicationtokenid, @PathParam("userAction") String userActionInput, String userJson) throws AppException {
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




}
