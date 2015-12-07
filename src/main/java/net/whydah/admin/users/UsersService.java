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

    /**
     * Internal function for administration of users and roles
     *
     * @param applicationTokenId
     * @param userTokenId
     * @param query searchstring to be matched against UserAggregateDeprecated values
     * @return Json formatted string of UserAggregates
     */
    public String findUsers(String applicationTokenId, String userTokenId, String query) {
        String usersJson = null;
        if (hasAccess("findUsers",applicationTokenId, userTokenId)) {
           usersJson = uibUsersConnection.findUsers(applicationTokenId, userTokenId, query);
        } else {
            throw new NotAuthorizedException("Not Authorized to findUsers");
        }
        return usersJson;
    }

    /**
     * Directory function for 3.party applications
     *
     * @param applicationTokenId
     * @param userTokenId
     * @param query searchstring to be matched against UserIdentityDeprecated values
     * @return Json formatted string of Useridentities
     */
    public String searchUsers(String applicationTokenId, String userTokenId, String query) {
        String usersJson = null;
        if (hasAccess("searchUsers",applicationTokenId, userTokenId)) {
            usersJson = uibUsersConnection.findUsers(applicationTokenId, userTokenId, query);
            // TODO map to useridentity or implement new function in UIB for this (last is better)
        } else {
            throw new NotAuthorizedException("Not Authorized to searchUsers");
        }
        return usersJson;
    }

    boolean hasAccess(String operation,String applicationTokenId, String userTokenId) {
        //FIXME validate user and appliciation trying search for users
        return true;
    }
}
