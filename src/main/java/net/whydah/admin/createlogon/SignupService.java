package net.whydah.admin.createlogon;

import net.whydah.admin.user.uib.UserIdentity;
import net.whydah.admin.user.uib.UserIdentityRepresentation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 30.09.15.
 */
@Service
public class SignupService {
    private static final Logger log = getLogger(SignupService.class);
    private final UibCreateLogonConnection uibConnection;

    @Autowired
    public SignupService(UibCreateLogonConnection uibConnection) {
        this.uibConnection = uibConnection;
    }

    public UserIdentity signupUser(String applicationtokenid, UserIdentityRepresentation signupUser, UserAction userAction) {
        //1.Create User (UIB)
       UserIdentity userIdentity =  uibConnection.createUser(applicationtokenid, signupUser);
        //2.Send email or sms pin (STS)
        //3.Receive UserToken (STS)
        //4.Return UserToken.

        return userIdentity;
    }
}
