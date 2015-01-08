package net.whydah.admin.users;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.users.uib.UibUsersConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotAuthorizedException;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Service
public class UsersService {
    private static final Logger log = LoggerFactory.getLogger(UsersService.class);

    private final UibUsersConnection uibUsersConnection;
    private final CredentialStore credentialStore;

    @Autowired
    public UsersService(UibUsersConnection uibUsersConnection, CredentialStore credentialStore) {
        this.uibUsersConnection = uibUsersConnection;
        this.credentialStore = credentialStore;
        credentialStore.setUserAdminServiceTokenId("2ff16f110b320dcbacf050b3b9062465");
    }

    public String findUsers(String applicationTokenId, String adminUserTokenId, String query) {
        String usersJson = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
           usersJson = uibUsersConnection.findUsers(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, query);
        } else {
            throw new NotAuthorizedException("Not Authorized to change password");
        }
        return usersJson;
    }

    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new user.
        return true;
    }
}
