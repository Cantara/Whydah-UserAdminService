package net.whydah.admin;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whydah.admin.application.ApplicationResource;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandVerifyUASAccessByApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandGetUsertokenByUsertokenId;
import net.whydah.sso.commands.userauth.CommandValidateUsertokenId;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.mappers.UserTokenMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserToken;
import net.whydah.sso.util.WhydahUtil;

public class WhyDahRoleCheckUtil {

	 private static final Logger log = LoggerFactory.getLogger(WhyDahRoleCheckUtil.class);
	 
	String stsUrl;
	UibUserConnection uibUserConnection; 
	CredentialStore credentialStore;
	
	public WhyDahRoleCheckUtil(String stsUrl, UibUserConnection uibUserConnection, CredentialStore credentialStore){
		this.stsUrl = stsUrl;
	}
	
	public boolean hasAccess(String applicationTokenId, String userTokenId) throws AppException {


		Boolean userTokenIsValid = new CommandValidateUsertokenId(URI.create(stsUrl), applicationTokenId, userTokenId).execute();
		if(!userTokenIsValid){
			log.warn("CommandValidateUsertokenId failed, user token " + userTokenId + " is invalid");
			return false;
		}
		Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUrl, applicationTokenId).execute();
		if(!applicationTokenIsValid){
			log.warn("CommandValidateApplicationTokenId failed, app token " + applicationTokenId + " is invalid");
			return false;
		}



		Boolean userTokenIsAdmin = hasWhydahAdminRole(applicationTokenId, userTokenId);
		if(!userTokenIsAdmin){
			log.warn("user token " + userTokenId + " does not have admin role");
			return false;
		}
		Boolean isUASAccessOpen = new CommandVerifyUASAccessByApplicationTokenId(stsUrl, applicationTokenId).execute();
		if(!isUASAccessOpen){
			log.warn("CommandVerifyUASAccessByApplicationTokenId failed, app token " + applicationTokenId + " does not have UAS access");
			return false;
		}

		return true;
	}

	boolean hasWhydahAdminRole(String applicationTokenId, String userTokenId) throws AppException{
		String userTokenXml = new CommandGetUsertokenByUsertokenId(URI.create(stsUrl), applicationTokenId, null, userTokenId).execute();
		UserToken userToken = UserTokenMapper.fromUserTokenXml(userTokenXml);
		String userRolesJson = uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, userToken.getUid());
		System.out.println("Roles returned:" + userRolesJson);
		List<UserApplicationRoleEntry> roles = UserRoleMapper.fromJsonAsList(userRolesJson);
		UserApplicationRoleEntry adminRole = WhydahUtil.getWhydahUserAdminRole();
		for (UserApplicationRoleEntry role : roles) {
			log.debug("Checking for adminrole user UID:{} roleName: {} ", userToken.getUid(), role.getRoleName());
			if (role.getApplicationId().equalsIgnoreCase(adminRole.getApplicationId())) {
				if (role.getApplicationName().equalsIgnoreCase(adminRole.getApplicationName())) {
					if (role.getOrgName().equalsIgnoreCase(adminRole.getOrgName())) {
						if (role.getRoleName().equalsIgnoreCase(adminRole.getRoleName())) {
							if (role.getRoleValue().equalsIgnoreCase(adminRole.getRoleValue())) {
								log.info("Whydah Admin user is true for uid={}", userToken.getUid());
								return true;
							}
						}
					}
				}
			}
		}
		log.info("Whydah Admin user is false for uid={}", userToken.getUid());
		return false;
	}

	public boolean hasAccess(String applicationTokenId) {
		Boolean isUASAccessOpen = new CommandVerifyUASAccessByApplicationTokenId(stsUrl, applicationTokenId).execute();
		if(!isUASAccessOpen){
			log.warn("CommandVerifyUASAccessByApplicationTokenId failed, app token " + applicationTokenId + " does not have UAS access");
			return false;
		}
		return true;
	}
	

	private static final String UAWA_ID = "2219";
    public boolean isUAWA(String applicationTokenId, String userTokenId) {
        log.trace("Checking isUAWA. UAWA_ID:{}applicationTokenId:{} userTokenId:{} ", UAWA_ID, applicationTokenId, userTokenId);
        String applicationID = new CommandGetApplicationIdFromApplicationTokenId(UriBuilder.fromUri(stsUrl).build(), applicationTokenId).execute();
        log.trace("CommandGetApplicationIdFromApplicationTokenId return appID:{} ", applicationID);
        return (UAWA_ID.equals(applicationID));
    }
}
