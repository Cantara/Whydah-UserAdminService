package net.whydah.admin.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhydahRoleCheckUtil;
import net.whydah.admin.auth.UserLogonObservedActivity;
import net.whydah.admin.auth.UserRemoveObservedActivity;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.user.helpers.UserRoleJsonPathHelper;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
import net.whydah.sts.user.statistics.UserSessionObservedActivity;

import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.valuereporter.activity.ObservedActivity;
import org.valuereporter.client.MonitorReporter;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * Created by baardl on 18.04.14.
 */
@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
	private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
	private static final int STATUS_CREATED = 201;
	private static final int STATUS_NO_CONTENT = 204;
	private static final int STATUS_UNAUTHORIZED = 401;
	private static final int STATUS_FORBIDDEN = 403;
	private static final int STATUS_CONFLICT = 409;

	private final UibUserConnection uibUserConnection;

	private final CredentialStore credentialStore;
	private final ObjectMapper mapper;
	private WhydahRoleCheckUtil adminChecker;

	public WhydahRoleCheckUtil getAdminChecker() {
		return adminChecker;
	}

	@Autowired
	@Configure
	public UserService(UibUserConnection uibUserConnection, CredentialStore credentialStore, WhydahRoleCheckUtil adminChecker) {
		this.uibUserConnection = uibUserConnection;
		this.credentialStore = credentialStore;
		this.adminChecker = adminChecker;
		this.mapper = new ObjectMapper();
	}


	public UserIdentity createUser(String applicationTokenId, String userTokenId, String userJsonIdentity) throws AppException {
		UserIdentity userIdentity = null;
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			UserIdentity signupUser = UserIdentityMapper.fromUserIdentityJson(userJsonIdentity);
			//Response userCheckResponse = uibUsersConnection.checkExist(applicationTokenId, userTokenId, signupUser.getUsername());
			//int userCheckStatusCode = userCheckResponse.getStatus();
			Response response = uibUserConnection.createUser(credentialStore.getUserAdminServiceTokenId(), userTokenId, userJsonIdentity);
			String userJson = response.readEntity(String.class);
			int statusCode = response.getStatus();
			switch (statusCode) {
				case STATUS_OK:
					log.trace("createUser-Response from UIB {}", userJson);
					userIdentity = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
					break;
				case STATUS_CREATED:
					log.trace("createUser-userCreated {}", userJson);
				userIdentity = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
				break;
			case STATUS_CONFLICT:
				log.info("Duplicate creation of user attempted on {}", userJson);
				//throw new ConflictExeption("DuplicateCreateAttempted on " + userIdentityJson);
				throw AppExceptionCode.MISC_ConflictException_9995.setDeveloperMessage("Duplicate creation of user attempted on {}", userJson);
			case STATUS_BAD_REQUEST:
				log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJsonIdentity);
				//throw new BadRequestException("BadRequest for Json " + userIdentityJson + ",  Status code " + response.getStatus());
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
			default:
				log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
				//throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
			}
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

		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response response = uibUserConnection.getUserIdentity(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
			String responseBody = response.readEntity(String.class);
			switch (response.getStatus()) {
			case STATUS_OK:
				log.info("getUserIdentity-Response from Uib {}", responseBody);
                UserIdentity userIdentity = UserIdentityMapper.fromJson(responseBody);
                return userIdentity;
			case STATUS_FORBIDDEN:
				log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
				//return null;
				throw AppExceptionCode.MISC_FORBIDDEN_9993.setDeveloperMessage("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
			case STATUS_UNAUTHORIZED:
				log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
				//return null;
				throw AppExceptionCode.MISC_NotAuthorizedException_9992.setDeveloperMessage("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
			default:
				log.error("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
				//throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
			}

		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public Response updateUserIdentity(String applicationTokenId, String userTokenId, String uid, String userIdentityJson) throws AppException {
		if(adminChecker.authorise(applicationTokenId, userTokenId)){
			return uibUserConnection.updateUserIdentity(applicationTokenId, userTokenId, uid, userIdentityJson);
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public boolean changePassword(String applicationTokenId, String userTokenId, String userName, String password) throws AppException {
		boolean isUpdated=false;
		if (adminChecker.authorise(applicationTokenId)) {
			Response response = uibUserConnection.changePassword(credentialStore.getUserAdminServiceTokenId(), userTokenId, userName, password);
			int statusCode = response.getStatus();
			String passwordJson = response.readEntity(String.class);
			switch (statusCode) {
			case STATUS_OK:
				log.trace("changePassword-Response from UIB {}", passwordJson);
				isUpdated = true;
				break;
			case STATUS_FORBIDDEN:
				log.error("changePassword-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), passwordJson);
				throw AppExceptionCode.MISC_FORBIDDEN_9993;
			default:
				log.error("changePassword-Response from UIB: {}: {}", response.getStatus(), passwordJson);
				//throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("changePassword-Response from UIB: {}: {}", response.getStatus(), passwordJson);
			}
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
		return isUpdated;
	}

	public boolean hasUserSetPassword(String applicationTokenId, String userName) {
		Response responseFromUib = uibUserConnection.hasUserSetPassword(credentialStore.getUserAdminServiceTokenId(), userName);
		log.debug("Response from UIB: {}", responseFromUib.getEntity().toString());
        if (responseFromUib.hasEntity()) {
            Boolean responseBody = Boolean.valueOf(responseFromUib.readEntity(String.class));
            return responseBody;
        }
        return false;
	}

	public UserApplicationRoleEntry addUserRole(String applicationTokenId, String userTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
		UserApplicationRoleEntry role;
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response response = uibUserConnection.addRole(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid, roleRequest);

			String roleJson = response.readEntity(String.class);
			int statusCode = response.getStatus();

			switch (statusCode) {
			case STATUS_OK:
				log.trace("addRole-Response from UIB {}", roleJson);
				role = UserRoleMapper.fromJson(roleJson);
				break;
			case STATUS_CREATED:
				log.trace("addRole-roleCreated {}", roleJson);
				role = UserRoleMapper.fromJson(roleJson);
				break;
			case STATUS_CONFLICT:
				log.info("Duplicate creation of role attempted on {}", roleJson);
				//throw new ConflictExeption("DuplicateCreateAttempted on " + roleJson);
				throw AppExceptionCode.MISC_ConflictException_9995.setDeveloperMessage("Duplicate creation of role attempted on {}", roleJson);
			case STATUS_BAD_REQUEST:
				log.error("addRole-Response from UIB: {}: {}",statusCode, roleJson);
				//throw new BadRequestException("BadRequest for Json " + roleJson + ",  Status code " + statusCode);
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("addRole-Response from UIB: {}: {}",statusCode, roleJson);
			default:
				log.error("addRole-Response from UIB: {}: {}", statusCode, roleJson);
				//throw new AuthenticationFailedException("Authentication failed. Status code " + statusCode);
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("addRole-Response from UIB: {}: {}", statusCode, roleJson);
			}
			return role;
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public void deleteUserRole(String applicationTokenId, String userTokenId, String uid, String userRoleId) throws AppException {
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response response = uibUserConnection.deleteUserRole(credentialStore.getUserAdminServiceTokenId(),userTokenId, uid, userRoleId);
			int statusCode = response.getStatus();
			switch (statusCode) {
			case STATUS_NO_CONTENT:
				log.trace("deleteUserRole-Response from UIB {}", userRoleId);
				break;
			case STATUS_BAD_REQUEST:
				log.error("deleteUserRole-Response from UIB: {}: {}",statusCode, userRoleId);
				//throw new BadRequestException("deleteUserRole for userRoleId " + userRoleId + ",  Status code " + statusCode);
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("deleteUserRole-Response from UIB: {}: {}",statusCode, userRoleId);
			default:
				log.error("deleteUserRole-Response from UIB: {}: {}", statusCode, userRoleId);
				//throw new RuntimeException("DeleteUserRole failed. Status code " + statusCode);
				throw AppExceptionCode.MISC_RuntimeException_9994.setDeveloperMessage("deleteUserRole-Response from UIB: {}: {}", statusCode, userRoleId);
			}

		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public UserApplicationRoleEntry updateUserRole(String applicationTokenId, String userTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {

		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response response = uibUserConnection.updateRole(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid, roleRequest);
			String roleJson = response.readEntity(String.class);
			UserApplicationRoleEntry role = null;
			int statusCode = response.getStatus();

			switch (statusCode) {
			case STATUS_OK:
				log.trace("updateRole-Response from UIB {}", roleJson);
				role = UserRoleMapper.fromJson(roleJson);
				break;
			case STATUS_CREATED:
				log.trace("updateRole-roleCreated {}", roleJson);
				role = UserRoleMapper.fromJson(roleJson);
				break;
			case STATUS_CONFLICT:
				log.info("Duplicate creation of role attempted on {}", roleJson);
				//throw new ConflictExeption("DuplicateCreateAttempted on " + roleJson);
				throw AppExceptionCode.MISC_ConflictException_9995.setDeveloperMessage("Duplicate creation of role attempted on {}", roleJson);
			case STATUS_BAD_REQUEST:
				log.error("updateRole-Response from UIB: {}: {}",statusCode, roleJson);
				//throw new BadRequestException("BadRequest for Json " + roleJson + ",  Status code " + statusCode);
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("updateRole-Response from UIB: {}: {}",statusCode, roleJson);
			default:
				log.error("updateRole-Response from UIB: {}: {}", statusCode, roleJson);
				//throw new AuthenticationFailedException("Authentication failed. Status code " + statusCode);
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("updateRole-Response from UIB: {}: {}", statusCode, roleJson);
			}
			return role;


		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}

	}



	public UserAggregate getUserAggregateByUid(String applicationTokenId, String userTokenId, String uid) throws AppException {

		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response response = uibUserConnection.getUserAggregateByUid(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
			UserAggregate userAggregate = null;
			int statusCode = response.getStatus();
			String responseBody = response.readEntity(String.class);
			switch (statusCode) {
			case STATUS_OK:
				log.trace("getUserAggregateByUid-Response from Uib {}", responseBody);
				userAggregate = UserAggregateMapper.fromUserAggregateNoIdentityJson(responseBody);
				break;
			case STATUS_FORBIDDEN:
				log.error("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
				throw AppExceptionCode.MISC_FORBIDDEN_9993;
			default:
				log.error("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
				throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
			}        
			log.trace("found UserAggregateDeprecated {}", userAggregate);
			return userAggregate;
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public String getUserAggregateByUidAsJson(String applicationTokenId, String userTokenId, String uid) throws AppException {
		if (!adminChecker.authorise(applicationTokenId, userTokenId)) {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}

		Response response = uibUserConnection.getUserAggregateByUidAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
		int statusCode = response.getStatus();
        String responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("getUserAggregateByUid-Response from Uib {}", responseBody);
                return responseBody;
            case STATUS_FORBIDDEN:
                log.error("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                throw AppExceptionCode.MISC_FORBIDDEN_9993.setDeveloperMessage("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
            default:
                log.error("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
                //throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
        }
	}


	public String getRolesAsJson(String applicationTokenId, String userTokenId, String uid) throws AppException {
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			return findResponseBody("getRolesAsJson", uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid));
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}
	
	 private String findResponseBody(String methodName, Response response) throws AppException {
	        String responseBody;
	        int statusCode = response.getStatus();
	        responseBody = response.readEntity(String.class);
	        switch (statusCode) {
	            case STATUS_OK:
	                log.trace("{}-Response from UIB {}", methodName,responseBody);
	                break;
	            case STATUS_FORBIDDEN:
	                log.error("{}-Not allowed from UIB: {}: {} ", methodName,response.getStatus(), responseBody);
	                throw AppExceptionCode.MISC_FORBIDDEN_9993.setDeveloperMessage("{}-Not allowed from UIB: {}: {} ", methodName,response.getStatus(), responseBody);
	            default:
	                log.error("{}-Response from UIB: {}: {}", methodName,response.getStatus(), responseBody);
	                throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("{}-Response from UIB: {}: {}", methodName,response.getStatus(), responseBody);
	        }
	        return responseBody;
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
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			String rolesJson = findResponseBody("getRolesAsJson", uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid));
			log.debug("rolesJson {}", rolesJson);
			roles = mapRolesFromString(rolesJson);
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
		return roles;
	}


	private List<UserApplicationRoleEntry> mapRolesFromString(String rolesJson) {
		UserApplicationRoleEntry[] roleArray= UserRoleJsonPathHelper.getUserRoleFromUserAggregateJson(rolesJson);
		return Arrays.asList(roleArray);
	}


	public void deleteUser(String applicationTokenId, String userTokenId, String uid) throws AppException {
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response response = uibUserConnection.deleteUser(credentialStore.getUserAdminServiceTokenId(), userTokenId, uid);
			int statusCode = response.getStatus();
			switch (statusCode) {
			case STATUS_NO_CONTENT:
				try {
					ObservedActivity observedActivity = new UserRemoveObservedActivity(uid, "userDeleted", applicationTokenId, credentialStore.getApplicationID(applicationTokenId));
					MonitorReporter.reportActivity(observedActivity);
				} catch(Exception ex) {
					ex.printStackTrace();
					log.error("failed to send observed activity when deleting user");
				}
				log.trace("deleteUser-Response from UIB uid={}", uid);
				break;
			case STATUS_BAD_REQUEST:
				log.error("deleteUser-Response from UIB: {}: uid={}", statusCode, uid);
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("deleteUser-Response from UIB: {}: uid={}", statusCode, uid);
			default:
				log.error("deleteUser-Response from UIB: {}, uid=", statusCode, uid);
				throw AppExceptionCode.MISC_RuntimeException_9994.setDeveloperMessage("deleteUser-Response from UIB: {}, uid=", statusCode, uid);
			}
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}
}
