package net.whydah.admin.createlogon;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.auth.uib.UibAuthConnection;
import net.whydah.admin.createlogon.uib.UibCreateLogonConnection;
import net.whydah.admin.email.PasswordSender;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
import org.apache.commons.lang.NotImplementedException;
import org.constretto.ConstrettoConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private final ObjectMapper objectMapper;
    static final String CHANGE_PASSWORD_TOKEN = "changePasswordToken";

    @Autowired
    public SignupService(UibCreateLogonConnection uibConnection, ConstrettoConfiguration configuration,
                         UibAuthConnection uibAuthConnection, PasswordSender passwordSender, ObjectMapper objectMapper) {
        this.uibConnection = uibConnection;
        this.configuration = configuration;
        this.uibAuthConnection = uibAuthConnection;
        this.passwordSender = passwordSender;
        this.objectMapper = objectMapper;
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

    public String signupUser(String applicationtokenId, UserIdentity signupUser, UserAction userAction) throws AppException {
        String passwordResetToken = null;
        //Add default roles
        UserAggregate createUserRepresentation = buildUserWithDefaultRoles(signupUser);
        //1.Create User (UIB)
        UserAggregate  createdUser =  uibConnection.createAggregateUser(applicationtokenId, createUserRepresentation);
        //2.CreateResetPasswordToken
        if (createdUser != null) {
            String username = createdUser.getUsername();
            String uid = createdUser.getUid();
            //String resetPasswordToken = uibAuthConnection.resetPassword_new(applicationtokenId, uid);
            //3.Send email or sms pin (STS)
            String passwordResetJson = uibAuthConnection.resetPassword(applicationtokenId, username);
            log.trace("resetPassword from UIB returned: {}",passwordResetJson);
            boolean passwordResetOk = false;
            try {
                Map<String, String> passwordResetMap = objectMapper.readValue(passwordResetJson, Map.class);
                String email = passwordResetMap.get("email");
                String cellPhone = passwordResetMap.get("cellPhone");
                String resetPasswordToken = passwordResetMap.get(CHANGE_PASSWORD_TOKEN);
                if (resetPasswordToken == null || resetPasswordToken.length() < 7) {
                    log.warn("UIB returned empty reset_password_token");
                } else {
                    passwordResetOk = sendNotification(createdUser, userAction, resetPasswordToken, "");
                    passwordResetOk = true;
                }

                boolean notificationSent = sendNotification(createdUser, userAction, resetPasswordToken, "");

                if (notificationSent) {
                    passwordResetToken = resetPasswordToken;
                }
            } catch (IOException ioe){
                log.warn(ioe.getMessage());
            }
        }

        return passwordResetToken;
    }

    protected boolean sendNotification(UserAggregate createdUser, UserAction userAction, String passwordResetToken, String passwordResetEmailTemplateName) {
        boolean notificationIsSent = false;
        if (createdUser != null) {
            if (userAction != null && userAction.equals(UserAction.PIN)) {
                //TODO send PIN notification see https://github.com/Cantara/Whydah-UserAdminService/issues/31
                throw new NotImplementedException();
            } else {
                String username = createdUser.getUsername();
                String userEmail = createdUser.getEmail();
                if (userEmail != null && !userEmail.isEmpty()) {
                    notificationIsSent = passwordSender.sendResetPasswordEmail(username, passwordResetToken, userEmail, passwordResetEmailTemplateName);
                }
            }
        }
        return notificationIsSent;
    }

    protected UserAggregate buildUserWithDefaultRoles(UserIdentity signupUser) {
        UserAggregate userAggregate = null;
        if (signupUser != null) {
            userAggregate = UserAggregateMapper.fromUserAggregateNoIdentityJson(UserIdentityMapper.toJson(signupUser));
            UserApplicationRoleEntry defaultRole = buildDefaultRole();
            List<UserApplicationRoleEntry> roleList = new LinkedList<UserApplicationRoleEntry>() ;
            roleList.add(defaultRole);
            userAggregate.setRoleList(roleList);
        }
        return userAggregate;
    }

    protected UserApplicationRoleEntry buildDefaultRole() {
        UserApplicationRoleEntry role = new UserApplicationRoleEntry();
        role.setApplicationId(defaultApplicationId);
        role.setApplicationName(defaultApplicationName);
        role.setOrgName(defaultOrganizationName);
        role.setRoleName(defaultRoleName);
        role.setRoleValue(defaultRoleValue);

        return role;
    }
}
