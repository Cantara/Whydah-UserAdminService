package net.whydah.admin.user;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.application.Application;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.admin.user.uib.UserAggregate;
import net.whydah.admin.user.uib.UserIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by baardl on 18.04.14.
 */
@Service
public class UserService {

    private final UibUserConnection uibUserConnection;
    private final CredentialStore credentialStore;

    @Autowired
    public UserService(UibUserConnection uibUserConnection, CredentialStore credentialStore) {
        this.uibUserConnection = uibUserConnection;
        this.credentialStore = credentialStore;
    }

    public UserIdentity createUserFromXml(String applicationTokenId, String userTokenId, String userXml) {
        UserIdentity createdUser = null;
        UserAggregate userAggregate = UserAggregate.fromXML(userXml);
        UserIdentity userIdentity = userAggregate.getIdentity();
        if (userIdentity != null) {
            String userJson = userIdentity.toJson();
            createdUser = createUser(applicationTokenId, userTokenId, userJson);
        }
        return createdUser;
    }

    private UserIdentity createUser(String applicationTokenId, String userTokenId, String userJson) {
        UserIdentity userIdentity = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            userIdentity = uibUserConnection.createUser(credentialStore.getUserAdminServiceTokenId(), userTokenId, userJson);
        } else {
            //FIXME handle no access to this method.
        }
        return userIdentity;
    }

    public UserAggregate addUserRole(String applicationId,String applicationName, String organizationId, String applicationRoleName, String applicationRoleValue) {
        return null;
    }

    public UserAggregate updateUserRole(String applicationId,String applicationName, String organizationId, String applicationRoleName, String applicationRoleValue) {
        return null;
    }

    public UserAggregate deleteUserRole(String applicationId,String applicationName, String organizationId, String applicationRoleName) {
        return null;
    }

    public Application getUser(String applicationTokenId, String userTokenId, String userId) {
        return null;
    }

    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new user.
        return true;
    }
}
