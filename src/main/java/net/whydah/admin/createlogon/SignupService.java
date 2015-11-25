package net.whydah.admin.createlogon;

import net.whydah.admin.auth.UibAuthConnection;
import net.whydah.admin.email.PasswordSender;
import net.whydah.admin.user.uib.RoleRepresentation;
import net.whydah.admin.user.uib.UserAggregateRepresentation;
import net.whydah.admin.user.uib.UserIdentityRepresentation;
import net.whydah.admin.user.uib.UserPropertyAndRole;
import org.constretto.ConstrettoConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 30.09.15.
 */
@Service
public class SignupService {
    private static final Logger log = getLogger(SignupService.class);
    private final UibCreateLogonConnection uibConnection;
    private final ConstrettoConfiguration configuration;
    private final UibAuthConnection uibAuthConnection;
    private final PasswordSender passwordSender;

    private String defaultApplicationId;
    private String defaultApplicationName;
    private String defaultOrganizationName;
    private String defaultRoleName;
    private String defaultRoleValue;
    private String netIQapplicationId;
    private String netIQapplicationName;
    private String netIQorganizationName;
    private String netIQRoleName;
    private String fbapplicationId;
    private String fbapplicationName;
    private String fborganizationName;
    private String fbRoleName;

    @Autowired
    public SignupService(UibCreateLogonConnection uibConnection, ConstrettoConfiguration configuration,
                         UibAuthConnection uibAuthConnection, PasswordSender passwordSender) {
        this.uibConnection = uibConnection;
        this.configuration = configuration;
        this.uibAuthConnection = uibAuthConnection;
        this.passwordSender = passwordSender;
        initAddUserDefaults(this.configuration);
    }
    private void initAddUserDefaults(ConstrettoConfiguration configuration) {
        this.defaultApplicationId = configuration.evaluateToString("adduser.defaultapplication.id");
        this.defaultApplicationName = configuration.evaluateToString("adduser.defaultapplication.name");
        this.defaultOrganizationName = configuration.evaluateToString("adduser.defaultorganization.name");
        this.defaultRoleName = configuration.evaluateToString("adduser.defaultrole.name");
        this.defaultRoleValue = configuration.evaluateToString("adduser.defaultrole.value");

        this.netIQapplicationId = configuration.evaluateToString("adduser.netiq.defaultapplication.id");
        this.netIQapplicationName = configuration.evaluateToString("adduser.netiq.defaultapplication.name");
        this.netIQorganizationName = configuration.evaluateToString("adduser.netiq.defaultorganization.name");
        this.netIQRoleName = configuration.evaluateToString("adduser.netiq.defaultrole.name");

        this.fbapplicationId = configuration.evaluateToString("adduser.facebook.defaultapplication.id");
        this.fbapplicationName = configuration.evaluateToString("adduser.facebook.defaultapplication.name");
        this.fborganizationName = configuration.evaluateToString("adduser.facebook.defaultorganization.name");
        this.fbRoleName = configuration.evaluateToString("adduser.facebook.defaultrole.name");
    }

    public String signupUser(String applicationtokenId, UserIdentityRepresentation signupUser, UserAction userAction) {
        String passwordResetToken = null;
        //Add default roles
        UserAggregateRepresentation createUserRepresentation = buildUserWithDefaultRoles(signupUser);
        //1.Create User (UIB)
        UserAggregateRepresentation  createdUser =  uibConnection.createAggregateUser(applicationtokenId, createUserRepresentation);
        //2.CreateResetPasswordToken
        if (createdUser != null) {
            String username = createdUser.getUsername();
            String uid = createdUser.getUid();
            String resetPasswordToken = uibAuthConnection.resetPassword_new(applicationtokenId, username);
            //3.Send email or sms pin (STS)
            boolean notificationSent = sendNotification (createdUser, userAction, resetPasswordToken);
            if (notificationSent) {
                passwordResetToken = resetPasswordToken;
            }
        }

        return passwordResetToken;
    }

    protected boolean sendNotification(UserAggregateRepresentation createdUser, UserAction userAction, String passwordResetToken) {
        boolean notificationIsSent = false;
        if (createdUser != null) {
            if (userAction != null && userAction.equals(UserAction.PIN)) {
                //TODO send PIN notification see https://github.com/Cantara/Whydah-UserAdminService/issues/31
                throw new NotImplementedException();
            } else {
                String username = createdUser.getUsername();
                String userEmail = createdUser.getEmail();
                if (userEmail != null && !userEmail.isEmpty()) {
                    notificationIsSent = passwordSender.sendResetPasswordEmail(username, passwordResetToken, userEmail);
                }
            }
        }
        return notificationIsSent;
    }

    protected UserAggregateRepresentation buildUserWithDefaultRoles(UserIdentityRepresentation signupUser) {
        UserAggregateRepresentation userAggregate = null;
        if (signupUser != null) {
            userAggregate = UserAggregateRepresentation.fromUserIdentityRepresentation(signupUser);
            RoleRepresentation defaultRole = buildDefaultRole();
            userAggregate.addRole(defaultRole);
        }
        return userAggregate;
    }

    protected RoleRepresentation buildDefaultRole() {
        UserPropertyAndRole userRole = new UserPropertyAndRole(null, null, defaultApplicationId,defaultApplicationName,defaultOrganizationName,defaultRoleName,defaultRoleValue);
        return RoleRepresentation.fromUserPropertyAndRole(userRole);
    }
}
