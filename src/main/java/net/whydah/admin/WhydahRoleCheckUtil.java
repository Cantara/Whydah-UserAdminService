package net.whydah.admin;

import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.application.types.Application;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandGetUsertokenByUsertokenId;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.mappers.UserTokenMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserToken;
import net.whydah.sso.util.WhydahUtil;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Component
public class WhydahRoleCheckUtil {

	private static final Logger log = LoggerFactory.getLogger(WhydahRoleCheckUtil.class);

	String stsUrl;
	CredentialStore credentialStore;
	UibApplicationsConnection uibApplicationsConnection;
	UibUserConnection uibUserConnection;
	ApplicationModelFacade appStore;
	String uaswa="2219";

	@Autowired
	@Configure
	public WhydahRoleCheckUtil(@Configuration("securitytokenservice") String stsUrl, UibApplicationsConnection uibApplicationsConnection, UibUserConnection uibUserConnection, CredentialStore credentialStore, @Configuration("uaswa") String uaswaId) {
		this.stsUrl = stsUrl;
		this.uibApplicationsConnection = uibApplicationsConnection;
		this.uibUserConnection = uibUserConnection;
		this.credentialStore = credentialStore;
		this.appStore = new ApplicationModelFacade(credentialStore, uibApplicationsConnection);
		if(uaswaId!=null&!uaswaId.equals("")){
			this.uaswa = uaswaId;
		}
	}

	public boolean authorise(String applicationTokenId, String userTokenId){
        log.trace("authorising applicationTokenId {} and usertokenid {}", applicationTokenId, userTokenId);
        if(isValidSession(applicationTokenId, userTokenId)){ //this can be checked at security filter, no need to recheck here
			if (isInternalWhydahAdminApp(applicationTokenId)) {
				//trump all if not a third party app
                log.debug("ApplicationTokenId {} having whydahadmin=true logged in UAS successfully", applicationTokenId);
                return true;
			} else {
				if(isUASAccessGranted(applicationTokenId)){
					if(hasUASAccessAdminRole(applicationTokenId, userTokenId)){
						//2212, Whydah-UserAdminService, Whydah, WhydahUserAdmin, 1
                        log.debug("ApplicationTokenId {} having UASAccess=true and WhydahUserAdmin role logged in UAS successfully", applicationTokenId);
                        return true;
					}
				}
			}
		}
        try {
            credentialStore.getWas().reportThreatSignal("Application authentication failure", new Object[]{ConstantValue.APP_TOKEN_ID, applicationTokenId, ConstantValue.USER_TOKEN_ID, userTokenId});
        } catch (Exception e) {
            // Ignore
        }
        log.debug("ApplicationTokenId {} failed to log in", applicationTokenId);
		return false;
	}

	public boolean authorise(String applicationTokenId){
        log.trace("authorising applicationTokenId {}", applicationTokenId);
        if(isValidSession(applicationTokenId)){ //this can be checked at security filter, no need to recheck here
			if (isInternalWhydahAdminApp(applicationTokenId)) {
				//trump all if not a third party app
                log.debug("ApplicationTokenId {} having whydahadmin=true logged in UAS successfully", applicationTokenId);
                return true;
			} else {
				if(isUASAccessGranted(applicationTokenId)){
                    log.debug("ApplicationTokenId {} having UASAccess=true logged in UAS successfully", applicationTokenId);
                    return true;
				}
			}
		}
        try {
        	credentialStore.getWas().reportThreatSignal("Application authentication failure", new Object[]{ConstantValue.APP_TOKEN_ID, applicationTokenId});
        } catch (Exception e) {
            // Ignore
        }
        log.debug("ApplicationTokenId {} failed to log in", applicationTokenId);
		return false;
	}


	public boolean isValidSession(String applicationTokenId){
		//		Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUrl, applicationTokenId).execute();
		//		if(!applicationTokenIsValid){
		//			log.warn("CommandValidateApplicationTokenId failed, app token " + applicationTokenId + " is invalid");
		//			return false;
		//		}
		return true;
	}

	public boolean isValidSession(String applicationTokenId, String userTokenId){

		//this can be checked at security filter, no need to recheck here
		return true;
		/*
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
		return true;*/
	}

	//check if 3rd party is granted UASAccess
	public boolean isUASAccessGranted(String applicationTokenId) {
		//we have to get the appId from the sts
		//String appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
		String appId=null;
		if(appStore.apptopkenId_appId_Map.containsKey(applicationTokenId)){
			appId = appStore.apptopkenId_appId_Map.get(applicationTokenId);
		} else {
			appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
			appStore.apptopkenId_appId_Map.put(applicationTokenId, appId);
		}
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

	//a must-have role to be an admin 
	//systest, 2212, Whydah-UserAdminService, Whydah, WhydahUserAdmin, 1
	public boolean hasUASAccessAdminRole(String applicationTokenId, String userTokenId) {
		String userTokenXml = new CommandGetUsertokenByUsertokenId(URI.create(stsUrl), applicationTokenId, "", userTokenId).execute();
		UserToken userToken = UserTokenMapper.fromUserTokenXml(userTokenXml);
		//TODO: should have systest role or something pre-configured properly, now we just skip it
		//FIX ME
		if(userToken.getUserName().equals("systest")){
			return true;
		}
		
		
		
		//get all roles from uib to check whether this user has admin right
		Response response = uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, userToken.getUid());
		int statusCode = response.getStatus();
		String userRolesJson = response.readEntity(String.class);
		if(statusCode==200){
			log.debug("Roles returned:" + userRolesJson);
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
			credentialStore.getWas().reportThreatSignal("Whydah Admin user is false for user trying to access Whydah Admin API");
			log.info("Whydah Admin user is false for name={}, uid={}. Cannot log in", userToken.getUserName(), userToken.getUid());
		} else {
			log.error("Error when getting role list - status code from UIB: " + statusCode);
		}

		return false;
	}

	//check internal Whydah admin app
	public boolean isInternalWhydahAdminApp(String applicationTokenId) {
		String appId=null;
		if(appStore.apptopkenId_appId_Map.containsKey(applicationTokenId)){
			appId = appStore.apptopkenId_appId_Map.get(applicationTokenId);
		} else {
			appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
			appStore.apptopkenId_appId_Map.put(applicationTokenId, appId);
		}
		//get application data
		Application app = appStore.getApplication(appId);
		if(app!=null){			
			if(app.getSecurity()!=null){
				boolean isWhyDahAdmin = app.getSecurity().isWhydahAdmin();
				if(isWhyDahAdmin){
					log.info("Application " + app.getName() + " with apptokenid=" + applicationTokenId + " has isWhydahAdmin right");
				}
				return isWhyDahAdmin;
			} else {
				log.error("app.getSecurity() is null. This error should not happen");
				return false;
			}
		} else {
			if(app==null){
				log.warn(appStore.apps.size()>0? "App not found" : "Application list is empty");
			}
			return false;
		}
	}

	public boolean isUAWA(String applicationTokenId) {
		//2 conditions: - has whydahadmin=true and has a correct appid
		//String applicationId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
		String appId=null;
		if(appStore.apptopkenId_appId_Map.containsKey(applicationTokenId)){
			appId = appStore.apptopkenId_appId_Map.get(applicationTokenId);
		} else {
			appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
			appStore.apptopkenId_appId_Map.put(applicationTokenId, appId);
		}
		//get application data
		Application app = appStore.getApplication(appId);
		log.trace("CommandGetApplicationIdFromApplicationTokenId return appID:{} ", appId);
		if(app.getSecurity().isWhydahAdmin() && app.getId().equals(uaswa)){
			return true;
		} else {
			return false;
		}
	}
}
