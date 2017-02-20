package net.whydah.admin;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.application.types.Application;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandGetUsertokenByUsertokenId;
import net.whydah.sso.commands.userauth.CommandValidateUsertokenId;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.mappers.UserTokenMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserToken;
import net.whydah.sso.util.WhydahUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhyDahRoleCheckUtil {

	 private static final Logger log = LoggerFactory.getLogger(WhyDahRoleCheckUtil.class);
	 
	String stsUrl;
	UibUserConnection uibUserConnection; 
	CredentialStore credentialStore;
	UibApplicationsConnection uibApplicationsConnection;
	ApplicationModelFacade appStore;
	
	public WhyDahRoleCheckUtil(String stsUrl, UibUserConnection uibUserConnection, UibApplicationsConnection uibApplicationsConnection, CredentialStore credentialStore){
		this.stsUrl = stsUrl;
		this.uibUserConnection = uibUserConnection;
		this.uibApplicationsConnection = uibApplicationsConnection;
		this.credentialStore = credentialStore;
		this.appStore = new ApplicationModelFacade(credentialStore, uibApplicationsConnection);
	}
	
	public boolean hasAccess(String applicationTokenId, String userTokenId) throws AppException {


		Boolean userTokenIsValid = new CommandValidateUsertokenId(URI.create(stsUrl), applicationTokenId, userTokenId).execute();
		if(!userTokenIsValid){
			log.warn("CommandValidateUsertokenId failed for userTokenId {}", userTokenId);
			return false;
		}
		
		Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUrl, applicationTokenId).execute();
		if(!applicationTokenIsValid){
			log.warn("CommandValidateApplicationTokenId failed, app token " + applicationTokenId + " is invalid");
			return false;
		}

		Boolean userTokenIsAdmin = hasWhydahAdminRole(applicationTokenId, userTokenId);
		if(!userTokenIsAdmin){
			return false;
		}
		
		return checkUASAccess(applicationTokenId);
		
		
	}

	public boolean hasAccess(String applicationTokenId) {
		Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUrl, applicationTokenId).execute();
		if(!applicationTokenIsValid){
			log.warn("CommandValidateApplicationTokenId failed, app token " + applicationTokenId + " is invalid");
			return false;
		}
		return checkUASAccess(applicationTokenId);
	}
	
	private boolean checkUASAccess(String applicationTokenId) {
		//we have to get the appId from the sts
		String appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
		//get application data
		Application app = appStore.getApplication(appId);
		if(app!=null && app.getSecurity()!=null){
			
			boolean hasUASAccess = app.getSecurity().isWhydahUASAccess();
			if(!hasUASAccess){
				log.warn("Application " + app.getName() +  " with apptokenid=" + applicationTokenId + " does not have UAS access");
			}
			return hasUASAccess;
		} else {
			if(app==null){
				log.warn(appStore.apps.size()>0? "App not found" : "Application list is empty");
			} else {
				log.error("app.getSecurity() is null. This error should not happen");
			}
			return false;
		}
	}
	
	
	//For tests to work, we have to grant admin role for systest user
	//systest, 2212, Whydah-UserAdminService, Whydah, WhydahUserAdmin, 1
	boolean hasWhydahAdminRole(String applicationTokenId, String userTokenId) throws AppException{
		String userTokenXml = new CommandGetUsertokenByUsertokenId(URI.create(stsUrl), applicationTokenId, "", userTokenId).execute();
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
								log.info("Whydah Admin user is true for name={}, uid={}", userToken.getUserName(), userToken.getUid());
								return true;
							}
						}
					}
				}
			}
		}
		log.info("Whydah Admin user is false for name={}, uid={}", userToken.getUserName(), userToken.getUid());
		return false;
	}

	
	

	private static final String UAWA_ID = "2219";
    public boolean isUAWA(String applicationTokenId, String userTokenId) {
        log.trace("Checking isUAWA. UAWA_ID:{}applicationTokenId:{} userTokenId:{} ", UAWA_ID, applicationTokenId, userTokenId);
        String applicationID = new CommandGetApplicationIdFromApplicationTokenId(UriBuilder.fromUri(stsUrl).build(), applicationTokenId).execute();
        log.trace("CommandGetApplicationIdFromApplicationTokenId return appID:{} ", applicationID);
        return (UAWA_ID.equals(applicationID));
    }
}
