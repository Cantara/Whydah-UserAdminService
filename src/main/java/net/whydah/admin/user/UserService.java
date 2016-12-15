package net.whydah.admin.user;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.errorhandling.AppException;
import net.whydah.errorhandling.AppExceptionCode;
import net.whydah.sso.user.helpers.UserRoleXpathHelper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotAuthorizedException;

import java.util.Arrays;
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
        this.mapper = new ObjectMapper();
    }


    public UserIdentity createUser(String applicationTokenId, String adminUserTokenId, String userJson) throws AppException {
        UserIdentity userIdentity;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            userIdentity = uibUserConnection.createUser(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userJson);
        } else {
            throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
        return userIdentity;
    }

    public UserIdentity createUserFromXml(String applicationTokenId, String userTokenId, String userXml) {
        UserIdentity createdUser = UserIdentityMapper.fromUserAggregateJson(userXml);
        return createdUser;
    }

    public UserIdentity getUserIdentity(String applicationTokenId, String userTokenId, String uid) throws AppException {
        UserIdentity userIdentity;
        if (hasAccess(applicationTokenId, userTokenId)) {
            userIdentity = uibUserConnection.getUserIdentity(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
        /*
        UserIdentityDeprecated userIdentity = new UserIdentityDeprecated("uid","username","first", "last", "", "first.last@example.com", "12234", "");
        List<UserPropertyAndRole> roles = new ArrayList<>();
        roles.add(buildStubRole());
        userAggregate = new UserAggregateDeprecated(userIdentity, roles);
        */
        log.trace("found {}", userIdentity);
        return userIdentity;
    }

    public javax.ws.rs.core.Response updateUserIdentity(String applicationTokenId, String userTokenId, String uid, String userIdentityJson) {
        return uibUserConnection.updateUserIdentity(applicationTokenId, userTokenId, uid, userIdentityJson);
    }


    public boolean changePassword(String applicationTokenId, String adminUserTokenId, String userName, String password) throws AppException {
        boolean isUpdated;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            isUpdated = uibUserConnection.changePassword(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, userName, password);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
        return isUpdated;
    }

    public boolean hasUserSetPassword(String applicationTokenId, String userName) {
        boolean hasSetPW;
        hasSetPW = uibUserConnection.hasUserSetPassword(credentialStore.getUserAdminServiceTokenId(), userName);
        return hasSetPW;
    }

    public UserApplicationRoleEntry addUserRole(String applicationTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
        UserApplicationRoleEntry role;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            role = uibUserConnection.addRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, uid, roleRequest);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
        return role;
    }

    public void deleteUserRole(String applicationTokenId, String adminUserTokenId, String uid, String userRoleId) throws AppException {
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
           uibUserConnection.deleteUserRole(credentialStore.getUserAdminServiceTokenId(),adminUserTokenId, uid, userRoleId);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
    }

    public UserApplicationRoleEntry updateUserRole(String applicationTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
        UserApplicationRoleEntry role;
        if (hasAccess(applicationTokenId, adminUserTokenId)) {
            role = uibUserConnection.updateRole(credentialStore.getUserAdminServiceTokenId(), adminUserTokenId, uid, roleRequest);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
        return role;

    }



    public UserAggregate getUserAggregateByUid(String applicationTokenId, String userTokenId, String uid) throws AppException {
        UserAggregate userAggregate;
        if (!hasAccess(applicationTokenId, userTokenId)) {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }

        userAggregate = uibUserConnection.getUserAggregateByUid(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        log.trace("found UserAggregateDeprecated {}", userAggregate);
        return userAggregate;
    }

    public String getUserAggregateByUidAsJson(String applicationTokenId, String userTokenId, String uid) throws AppException {
        if (!hasAccess(applicationTokenId, userTokenId)) {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }

        String userAggregate = uibUserConnection.getUserAggregateByUidAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        log.trace("found UserAggregateDeprecated {}", userAggregate);
        return userAggregate;
    }


    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new user.
        return true;
    }



    public String getRolesAsJson(String applicationTokenId, String userTokenId, String uid) throws AppException {
        if (hasAccess(applicationTokenId, userTokenId)) {
            return uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }

        /*
        List<UserApplicationRoleEntry> roles = getRoles(applicationTokenId, userTokenId, uid);
        String result;
        try {
            result = mapper.writeValueAsString(roles);
        } catch (IOException e) {
            log.error("Error converting List<RoleRepresentationDeprecated> to json. ", e);
            return null;
        }
        return result;
        */
        /*
        String result = "";
        for (RoleRepresentationDeprecated role : roles) {
            result += role.toJson();
        }
        */

    }
    public String getRolesAsXml(String applicationTokenId, String userTokenId, String uid) throws AppException {
        List<UserApplicationRoleEntry> roles = getRoles(applicationTokenId, userTokenId, uid);
        String result = "<applications>";
        for (UserApplicationRoleEntry role : roles) {
            result += role.toXML();
        }
        result += "</applications>";
        return result;
    }
    private List<UserApplicationRoleEntry> getRoles(String applicationTokenId, String userTokenId, String uid) throws AppException {
        List<UserApplicationRoleEntry> roles;
        if (hasAccess(applicationTokenId, userTokenId)) {
            String rolesJson = uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
            log.debug("rolesJson {}", rolesJson);
            roles = mapRolesFromString(rolesJson);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
        return roles;
    }


    private List<UserApplicationRoleEntry> mapRolesFromString(String rolesJson) {
        UserApplicationRoleEntry[] roleArray= UserRoleXpathHelper.getUserRoleFromUserAggregateJson(rolesJson);
        return Arrays.asList(roleArray);
    }


    public void deleteUser(String applicationTokenId, String userTokenId, String uid) throws AppException {
        if (hasAccess(applicationTokenId, userTokenId)) {
            uibUserConnection.deleteUser(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
        }
    }
}
