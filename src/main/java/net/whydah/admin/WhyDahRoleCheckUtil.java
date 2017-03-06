package net.whydah.admin;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;

import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.application.types.Application;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
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

@Component
public class WhyDahRoleCheckUtil {

	private static final Logger log = LoggerFactory.getLogger(WhyDahRoleCheckUtil.class);

	String stsUrl;
	CredentialStore credentialStore;
	UibApplicationsConnection uibApplicationsConnection;
	UibUserConnection uibUserConnection;
	ApplicationModelFacade appStore;

	@Autowired
	@Configure
	public WhyDahRoleCheckUtil(@Configuration("securitytokenservice") String stsUrl, UibApplicationsConnection uibApplicationsConnection, UibUserConnection uibUserConnection, CredentialStore credentialStore){
		this.stsUrl = stsUrl;
		this.uibApplicationsConnection = uibApplicationsConnection;
		this.uibUserConnection = uibUserConnection;
		this.credentialStore = credentialStore;
		this.appStore = new ApplicationModelFacade(credentialStore, uibApplicationsConnection);
	}

	public boolean authorise(String applicationTokenId, String userTokenId){
		//return true;
		if(!isValidSession(applicationTokenId, userTokenId)){ //this can be checked at security filter, no need to recheck here
			return false;
		} else if(!hasUASAccessAdminRole(applicationTokenId, userTokenId)){
			//admin user must have this role configured
			//2212, Whydah-UserAdminService, Whydah, WhydahUserAdmin, 1
			return false;
		} else {
			if(isInternalWhyDahAdminApp(applicationTokenId)){
				//trump all if not a third party app
				log.info("AppTokenId {} logged in UAS successfully", applicationTokenId);
				return true;
			} else {
				boolean ok = isUASAccessGranted(applicationTokenId);
				if(ok){
					log.debug("AppTokenId {} logged in UAS successfully", applicationTokenId);
				} else {
					log.debug("AppTokenId {} failed to log in", applicationTokenId);
				}
				return ok;
			}
		}
	}
	
	public boolean authorise(String applicationTokenId){
		//return true;
		if(!isValidSession(applicationTokenId)){ //this can be checked at security filter, no need to recheck here
			return false;
		} else {
			if(isInternalWhyDahAdminApp(applicationTokenId)){
				//trump all if not a third party app
				log.info("AppTokenId {} logged in UAS successfully", applicationTokenId);
				return true;
			} else {
				boolean ok = isUASAccessGranted(applicationTokenId);
				if(ok){
					log.debug("AppTokenId {} logged in UAS successfully", applicationTokenId);
				} else {
					log.debug("AppTokenId {} failed to log in", applicationTokenId);
				}
				return ok;
			}
		}
	}
	

	public boolean isValidSession(String applicationTokenId){
		Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUrl, applicationTokenId).execute();
		if(!applicationTokenIsValid){
			log.warn("CommandValidateApplicationTokenId failed, app token " + applicationTokenId + " is invalid");
			return false;
		}
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

	//a must-have role to be an admin 
	//systest, 2212, Whydah-UserAdminService, Whydah, WhydahUserAdmin, 1
	public boolean hasUASAccessAdminRole(String applicationTokenId, String userTokenId) {
		String userTokenXml = new CommandGetUsertokenByUsertokenId(URI.create(stsUrl), applicationTokenId, "", userTokenId).execute();
		UserToken userToken = UserTokenMapper.fromUserTokenXml(userTokenXml);
		
		//get all roles from uib to check whether this user has admin right
		Response response = uibUserConnection.getRolesAsJson(credentialStore.getUserAdminServiceTokenId(), userTokenId, userToken.getUid());
		int statusCode = response.getStatus();
	    String userRolesJson = response.readEntity(String.class);
	    if(statusCode==200){
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
	    	log.info("Whydah Admin user is false for name={}, uid={}. Cannot log in", userToken.getUserName(), userToken.getUid());
	    } else {
	    	log.error("Error when getting role list - status code from UIB: " + statusCode);
	    }
		
		return false;
	}

	//check internal Whydah admin app
	public boolean isInternalWhyDahAdminApp(String applicationTokenId){
		String appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUrl), applicationTokenId).execute();
		//get application data
		Application app = appStore.getApplication(appId);
		if(app!=null && app.getSecurity()!=null){

			boolean isWhyDahAdmin = app.getSecurity().isWhydahAdmin();
			if(isWhyDahAdmin){
				log.info("Application " + app.getName() +  " with apptokenid=" + applicationTokenId + " has isWhyDahAdmin right");
			}
			return isWhyDahAdmin;
		} else {
			if(app==null){
				log.warn(appStore.apps.size()>0? "App not found" : "Application list is empty");
			} else {
				log.error("app.getSecurity() is null. This error should not happen");
			}
			return false;
		}
	}

	//THIS MAY BE NOT USED ANYMORE, SO WE COMMENT OFF
//	private static final String UAWA_ID = "2219";
//	public boolean isUAWA(String applicationTokenId, String userTokenId) {
//		log.trace("Checking isUAWA. UAWA_ID:{}applicationTokenId:{} userTokenId:{} ", UAWA_ID, applicationTokenId, userTokenId);
//		String applicationID = new CommandGetApplicationIdFromApplicationTokenId(UriBuilder.fromUri(stsUrl).build(), applicationTokenId).execute();
//		log.trace("CommandGetApplicationIdFromApplicationTokenId return appID:{} ", applicationID);
//		return (UAWA_ID.equals(applicationID));
//	}
}
