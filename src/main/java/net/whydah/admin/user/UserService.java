package net.whydah.admin.user;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.user.uib.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ws.rs.NotAuthorizedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baardl on 18.04.14.
 */
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UibUserConnection uibUserConnection;
    private final CredentialStore credentialStore;

    @Autowired
    public UserService(UibUserConnection uibUserConnection, CredentialStore credentialStore) {
        this.uibUserConnection = uibUserConnection;
        this.credentialStore = credentialStore;
        credentialStore.setUserAdminServiceTokenId("2ff16f110b320dcbacf050b3b9062465");
    }

    public UserIdentity createUserFromXml(String applicationTokenId, String userTokenId, String userXml) {
        UserIdentity createdUser = null;
        UserAggregate userAggregate = UserAggregate.fromXML(userXml);
        UserIdentityRepresentation userIdentity = userAggregate.getIdentity();
        if (userIdentity != null) {
            String userJson = userIdentity.toJson();
            createdUser = createUser(applicationTokenId, userTokenId, userJson);
        }
        return createdUser;
    }

    public UserIdentity createUser(String applicationTokenId, String adminUserTokenId, String userJson) {
        UserIdentity userIdentity = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            userIdentity = uibUserConnection.createUser(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userJson);
        } else {
            throw new NotAuthorizedException("Not Authorized to create user");
        }
        return userIdentity;
    }

    public boolean changePassword(String applicationTokenId, String adminUserTokenId, String userName, String password) {
        boolean isUpdated = false;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            isUpdated = uibUserConnection.changePassword(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userName, password);
        } else {
            throw new NotAuthorizedException("Not Authorized to change password");
        }
        return isUpdated;
    }

    public UserAggregate addUserRoleFromXml(String applicationTokenId, String adminUserTokenId, String userId, String propertyOrRoleXml) {

        UserAggregate updatedUser = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            UserPropertyAndRole userPropertyAndRole = UserPropertyAndRole.fromXml(propertyOrRoleXml);
            updatedUser = uibUserConnection.addPropertyOrRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userId, userPropertyAndRole);
        } else {
            throw new NotAuthorizedException("Not Authorized to add user role()");
        }
        return updatedUser;
    }


    public RoleRepresentation addUserRole(String applicationTokenId, String adminUserTokenId, String userId, RoleRepresentationRequest roleRequest) {
        RoleRepresentation role = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            role = uibUserConnection.addRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userId, roleRequest);
        } else {
            throw new NotAuthorizedException("Not Authorized to add user role()");
        }
        return role;
    }

    public void deleteUserRole(String applicationTokenId, String adminUserTokenId, String userId, String userRoleId) {
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
           uibUserConnection.deleteUserRole(credentialStore.getUserAdminServiceTokenId(),adminUserTokenId,userId, userRoleId);
        } else {
            throw new NotAuthorizedException("Not Authorized to delete user role()");
        }
    }

    public UserAggregate updateUserRole(String applicationId,String applicationName, String applicationRoleName, String applicationRoleValue) {
        throw new NotImplementedException();
    }



    public UserAggregate getUser(String applicationTokenId, String userTokenId, String userId) {
        log.trace("getUser by userId {}", userId);
        UserAggregate userAggregate = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            userAggregate = uibUserConnection.getUser(credentialStore.getUserAdminServiceTokenId(),userTokenId, userId);
        } else {
            throw new NotAuthorizedException("Not Authorized to getUser()");
        }
        /*
        UserIdentity userIdentity = new UserIdentity("uid","username","first", "last", "", "first.last@example.com", "12234", "");
        List<UserPropertyAndRole> roles = new ArrayList<>();
        roles.add(buildStubRole());
        userAggregate = new UserAggregate(userIdentity, roles);
        */
        log.trace("found UserAggregate {}", userAggregate);
        return userAggregate;
    }

    private UserPropertyAndRole buildStubRole() {
        return new UserPropertyAndRole("id", "uid", "1", "appname", "orgName", "user", "true");
    }

    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new user.
        return true;
    }


    public String getRolesAsString(String applicationTokenId, String userTokenId, String userId) {
        String roles = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            roles = uibUserConnection.getRolesAsString(credentialStore.getUserAdminServiceTokenId(),userTokenId, userId);
        } else {
            throw new NotAuthorizedException("Not Authorized to getRolesAsString()");
        }
        return roles;
    }

    public List<RoleRepresentation> getRoles(String applicationTokenId, String userTokenId, String userId) {
        List<RoleRepresentation> roles = new ArrayList<>();
        if (hasAccess(applicationTokenId, userTokenId)) {
            String rolesJson = uibUserConnection.getRolesAsString(credentialStore.getUserAdminServiceTokenId(), userTokenId, userId);
            roles = mapRolesFromString(rolesJson);
        } else {
            throw new NotAuthorizedException("Not Authorized to getRolesAsString()");
        }
        return roles;
    }

    private List<RoleRepresentation> mapRolesFromString(String rolesJson) {
        return RoleRepresentationMapper.fromJson(rolesJson);
    }


    public void deleteUser(String applicationTokenId, String userTokenId, String userId) {
        if (hasAccess(applicationTokenId, userTokenId)) {
            uibUserConnection.deleteUser(credentialStore.getUserAdminServiceTokenId(), userTokenId, userId);
        } else {
            throw new NotAuthorizedException("Not Authorized to deleteUser()");
        }
    }


}
