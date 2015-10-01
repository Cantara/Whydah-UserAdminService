package net.whydah.admin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.createlogon.UserAction;
import net.whydah.admin.email.PasswordSender;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 01.10.15.
 */
@Service
public class AuthenticationService {
    private static final Logger log = getLogger(AuthenticationService.class);

    private final UibAuthConnection uibAuthConnection;
    private final PasswordSender passwordSender;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthenticationService(UibAuthConnection uibAuthConnection, PasswordSender passwordSender, ObjectMapper objectMapper) {
        this.uibAuthConnection = uibAuthConnection;
        this.passwordSender = passwordSender;
        this.objectMapper = objectMapper;
    }

    public boolean resetPassword(String applicationtokenId,String username, UserAction userAction){
        boolean passwordResetOk = false;
        String passwordResetJson = uibAuthConnection.resetPassword(applicationtokenId, username);
        try {
            Map<String,String> passwordResetMap = objectMapper.readValue(passwordResetJson,Map.class);
            String email = passwordResetMap.get("email");
            String cellPhone = passwordResetMap.get("cellPhone");
            String resetPasswordToken = passwordResetMap.get("resetPasswordToken");
            passwordResetOk = sendNotification (email, cellPhone, username,userAction, resetPasswordToken);
            passwordResetOk = false;

        } catch (IOException e) {
            log.warn("Failed to parse response from uibAuthConnection.resetPassword. username {}, responseJson {}", username, passwordResetJson);
        }
        return passwordResetOk;

    }



    protected boolean sendNotification(String userEmail, String cellPhone, String username,UserAction userAction, String passwordResetToken) {
        boolean notificationIsSent = false;
            if (userAction != null && userAction.equals(UserAction.PIN)) {
                //TODO send PIN notification see https://github.com/Cantara/Whydah-UserAdminService/issues/31
                throw new NotImplementedException();
            } else {
                if (userEmail != null && !userEmail.isEmpty()) {
                    notificationIsSent = passwordSender.sendResetPasswordEmail(username, passwordResetToken, userEmail);
                }
            }
        return notificationIsSent;
    }
}
