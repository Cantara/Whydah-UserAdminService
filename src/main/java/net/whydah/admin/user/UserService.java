package net.whydah.admin.user;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.user.uib.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.util.List;

/**
 * Created by baardl on 18.04.14.
 */
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UibUserConnection uibUserConnection;
    private final CredentialStore credentialStore;
    private final ObjectMapper mapper;


    @Autowired
    public UserService(UibUserConnection uibUserConnection, CredentialStore credentialStore) {
        this.uibUserConnection = uibUserConnection;
        this.credentialStore = credentialStore;
        credentialStore.setUserAdminServiceTokenId("2ff16f110b320dcbacf050b3b9062465");
        this.mapper = new ObjectMapper();
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
        boolean isUpdated;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            isUpdated = uibUserConnection.changePassword(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userName, password);
        } else {
            throw new NotAuthorizedException("Not Authorized to change password");
        }
        return isUpdated;
    }

    public UserAggregate addUserRoleFromXml(String applicationTokenId, String adminUserTokenId, String uid, String propertyOrRoleXml) {

        UserAggregate updatedUser = null;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            UserPropertyAndRole userPropertyAndRole = UserPropertyAndRole.fromXml(propertyOrRoleXml);
            updatedUser = uibUserConnection.addPropertyOrRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, uid, userPropertyAndRole);
        } else {
            throw new NotAuthorizedException("Not Authorized to add user role()");
        }
        return updatedUser;
    }


    public RoleRepresentation addUserRole(String applicationTokenId, String adminUserTokenId, String uid, RoleRepresentationRequest roleRequest) {
        RoleRepresentation role;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            role = uibUserConnection.addRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, uid, roleRequest);
        } else {
            throw new NotAuthorizedException("Not Authorized to add user role()");
        }
        return role;
    }

    public void deleteUserRole(String applicationTokenId, String adminUserTokenId, String uid, String userRoleId) {
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
           uibUserConnection.deleteUserRole(credentialStore.getUserAdminServiceTokenId(),adminUserTokenId, uid, userRoleId);
        } else {
            throw new NotAuthorizedException("Not Authorized to delete user role()");
        }
    }

    public UserAggregate updateUserRole(String applicationId,String applicationName, String applicationRoleName, String applicationRoleValue) {
        throw new NotImplementedException();
    }



    public UserAggregate getUser(String applicationTokenId, String userTokenId, String uid) {
        UserAggregate userAggregate;
        if (hasAccess(applicationTokenId, userTokenId)) {
            userAggregate = uibUserConnection.getUser(credentialStore.getUserAdminServiceTokenId(),userTokenId, uid);
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


    /*
    public String getRolesAsString(String applicationTokenId, String userTokenId, String uid) {
        String roles;
        if (hasAccess(applicationTokenId, userTokenId)) {
            roles = uibUserConnection.getRolesAsString(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        } else {
            throw new NotAuthorizedException("Not Authorized to getRolesAsString()");
        }
        return roles;
    }
    */

    public String getRolesAsJson(String applicationTokenId, String userTokenId, String uid) {
        List<RoleRepresentation> roles = getRoles(applicationTokenId, userTokenId, uid);
        String result;
        try {
            result = mapper.writeValueAsString(roles);
        } catch (IOException e) {
            log.error("Error converting List<RoleRepresentation> to json. ", e);
            return null;
        }
        /*
        String result = "";
        for (RoleRepresentation role : roles) {
            result += role.toJson();
        }
        */
        return result;
    }
    public String getRolesAsXml(String applicationTokenId, String userTokenId, String uid) {
        List<RoleRepresentation> roles = getRoles(applicationTokenId, userTokenId, uid);
        String result = "";
        for (RoleRepresentation role : roles) {
            result += role.toXML();
        }
        return result;
    }
    private List<RoleRepresentation> getRoles(String applicationTokenId, String userTokenId, String uid) {
        List<RoleRepresentation> roles;
        if (hasAccess(applicationTokenId, userTokenId)) {
            String rolesJson = uibUserConnection.getRolesAsString(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
            roles = mapRolesFromString(rolesJson);
        } else {
            throw new NotAuthorizedException("Not Authorized to getRolesAsString()");
        }
        return roles;
    }


    private List<RoleRepresentation> mapRolesFromString(String rolesJson) {
        return RoleRepresentationMapper.fromJson(rolesJson);
    }


    public void deleteUser(String applicationTokenId, String userTokenId, String uid) {
        if (hasAccess(applicationTokenId, userTokenId)) {
            uibUserConnection.deleteUser(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        } else {
            throw new NotAuthorizedException("Not Authorized to deleteUser()");
        }
    }
}
