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
        credentialStore.setUserAdminServiceTokenId("ed8b5101b9592e90bcb25c760275f9c2");
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

    private UserIdentity createUser(String applicationTokenId, String adminUserTokenId, String userJson) {
        UserIdentity userIdentity = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            userIdentity = uibUserConnection.createUser(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userJson);
        } else {
            //FIXME handle no access to this method.
        }
        return userIdentity;
    }

    public boolean changePassword(String applicationTokenId, String adminUserTokenId, String userName, String password) {
        boolean isUpdated = false;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            isUpdated = uibUserConnection.changePassword(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userName, password);
        } else {
            //FIXME handle no access to this method.
        }
        return isUpdated;
    }

    public UserAggregate addUserRoleFromXml(String applicationTokenId, String adminUserTokenId, String userId, String propertyOrRoleXml) {

        UserAggregate updatedUser = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            UserPropertyAndRole userPropertyAndRole = UserPropertyAndRole.fromXml(propertyOrRoleXml);
            updatedUser = uibUserConnection.addPropertyOrRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userId, userPropertyAndRole);
        } else {
            //FIXME handle no access to this method.
        }
        return updatedUser;
    }


    public RoleRepresentation addUserRole(String applicationTokenId, String adminUserTokenId, String userId, RoleRepresentationRequest roleRequest) {
        RoleRepresentation role = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            role = uibUserConnection.addRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userId, roleRequest);
        } else {
            //FIXME handle no access to this method.
        }
        return role;
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
