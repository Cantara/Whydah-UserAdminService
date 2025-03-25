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
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public SignupService(UibCreateLogonConnection uibConnection,
                         @Value("${adduser.defaultapplication.id}") String defaultApplicationId,
                         @Value("${adduser.defaultapplication.name}") String defaultApplicationName,
                         @Value("${adduser.defaultorganization.name}") String defaultOrganizationName,
                         @Value("${adduser.defaultrole.name}") String defaultRoleName,
                         @Value("${adduser.defaultrole.value}") String defaultRoleValue,
                         @Value("${adduser.netiq.defaultapplication.id}") String netIQapplicationId,
                         @Value("${adduser.netiq.defaultapplication.name}") String netIQapplicationName,
                         @Value("${adduser.netiq.defaultorganization.name}") String netIQorganizationName,
                         @Value("${adduser.netiq.defaultrole.name}") String netIQRoleName,
                         @Value("${adduser.facebook.defaultapplication.id}") String fbapplicationId,
                         @Value("${adduser.facebook.defaultapplication.name}") String fbapplicationName,
                         @Value("${adduser.facebook.defaultorganization.name}") String fborganizationName,
                         @Value("${adduser.facebook.defaultrole.name}") String fbRoleName,
                         UibAuthConnection uibAuthConnection,
                         PasswordSender passwordSender,
                         ObjectMapper objectMapper) {
        this.uibConnection = uibConnection;
        this.defaultApplicationId = defaultApplicationId;
        this.defaultApplicationName = defaultApplicationName;
        this.defaultOrganizationName = defaultOrganizationName;
        this.defaultRoleName = defaultRoleName;
        this.defaultRoleValue = defaultRoleValue;
        this.netIQapplicationId = netIQapplicationId;
        this.netIQapplicationName = netIQapplicationName;
        this.netIQorganizationName = netIQorganizationName;
        this.netIQRoleName = netIQRoleName;
        this.fbapplicationId = fbapplicationId;
        this.fbapplicationName = fbapplicationName;
        this.fborganizationName = fborganizationName;
        this.fbRoleName = fbRoleName;
        this.uibAuthConnection = uibAuthConnection;
        this.passwordSender = passwordSender;
        this.objectMapper = objectMapper;
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