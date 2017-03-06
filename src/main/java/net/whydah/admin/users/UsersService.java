package net.whydah.admin.users;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhyDahRoleCheckUtil;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.users.uib.UibUsersConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Service
public class UsersService {
	private static final Logger log = LoggerFactory.getLogger(UsersService.class);
	private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
	private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
	private static final int STATUS_FORBIDDEN = 403;
	private static final int STATUS_CREATED = 201;
	private static final int STATUS_CONFLICT = 409;
	private static final int STATUS_NO_CONTENT = 204;

	private final UibUsersConnection uibUsersConnection;
	private final CredentialStore credentialStore;
	private WhyDahRoleCheckUtil adminChecker;
	public WhyDahRoleCheckUtil getAdminChecker(){
		return adminChecker;
	}

	@Autowired
	public UsersService(UibUsersConnection uibUsersConnection, CredentialStore credentialStore, WhyDahRoleCheckUtil adminChecker) {
		this.uibUsersConnection = uibUsersConnection;
		this.credentialStore = credentialStore;
		this.adminChecker = adminChecker;
	}

	/**
	 * Internal function for administration of users and roles
	 *
	 * @param applicationTokenId
	 * @param userTokenId
	 * @param query searchstring to be matched against UserAggregateDeprecated values
	 * @return Json formatted string of UserAggregates
	 * @throws AppException 
	 */
	public String findUsers(String applicationTokenId, String userTokenId, String query) throws AppException {
		String usersJson = null;
		if (hasAccess("findUsers",applicationTokenId, userTokenId)) {
			Response response = uibUsersConnection.findUsers(applicationTokenId, userTokenId, query);
			int statusCode = response.getStatus();
			String output = response.readEntity(String.class);
			switch (statusCode) {
			case STATUS_OK:
				log.trace("Response from UIB {}", output);
				usersJson = output;
				break;
			case STATUS_BAD_REQUEST:
				log.error("Response from UIB: {}: {}", response.getStatus(), output);
				//throw new BadRequestException("BadRequest for query " + query + ",  Status code " + response.getStatus());
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("BadRequest for query " + query + ",  Status code " + response.getStatus());
			default:
				log.error("Response from UIB: {}: {}", response.getStatus(), output);
				//throw new AuthenticationFailedException("Request failed. Status code " + response.getStatus());
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("Request failed. Status code " + response.getStatus());
			}
			return usersJson;

		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	/**
	 * Directory function for 3.party applications
	 *
	 * @param applicationTokenId
	 * @param userTokenId
	 * @param query searchstring to be matched against UserIdentityDeprecated values
	 * @return Json formatted string of Useridentities
	 * @throws AppException 
	 */
	public String searchUsers(String applicationTokenId, String userTokenId, String query) throws AppException {
		String usersJson = null;
		if (hasAccess("searchUsers",applicationTokenId, userTokenId)) {
			Response response = uibUsersConnection.findUsers(applicationTokenId, userTokenId, query);
			int statusCode = response.getStatus();
			String output = response.readEntity(String.class);
			switch (statusCode) {
			case STATUS_OK:
				log.trace("Response from UIB {}", output);
				usersJson = output;
				break;
			case STATUS_BAD_REQUEST:
				log.error("Response from UIB: {}: {}", response.getStatus(), output);
				//throw new BadRequestException("BadRequest for query " + query + ",  Status code " + response.getStatus());
				throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("BadRequest for query " + query + ",  Status code " + response.getStatus());
			default:
				log.error("Response from UIB: {}: {}", response.getStatus(), output);
				//throw new AuthenticationFailedException("Request failed. Status code " + response.getStatus());
				throw AppExceptionCode.MISC_OperationFailedException_9996.setDeveloperMessage("Request failed. Status code " + response.getStatus());
			}
			return usersJson;
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
			//throw new NotAuthorizedException("Not Authorized to searchUsers");
		}
	}

	boolean hasAccess(String operation,String applicationTokenId, String userTokenId) {
		//HUY 3.3.2017
		//TODO: not sure what to do with the operation parameter. We should authorize this requested application and user anyway
		//return adminChecker.authorise(applicationTokenId, userTokenId);
		//However, to avoid leaking user info, for now we only allow internal whydah admin, one configured as isWhydahAdmin=true in Application model
		//we can allow 3rd party to get its users (only those associating with this application). Should we add one property AssociatedAppIds in UserToken model? 
		
		
		//SKIP NOW, WE HAVE TO ADD WhydahAdmin=true to UASWA app json
		/*
		if(adminChecker.isInternalWhyDahAdminApp(applicationTokenId)){ //only allow internal app
			return adminChecker.hasUASAccessAdminRole(applicationTokenId, userTokenId); //check if this user has admin right
		}
		return false;//otherwise
		*/
		return true;
	}
}
