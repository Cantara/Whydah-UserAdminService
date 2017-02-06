package net.whydah.admin.applications;

import java.net.URI;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandVerifyUASAccessByApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandValidateUsertokenId;
import net.whydah.sso.commands.userauth.CommandValidateWhydahAdminByUserTokenId;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationsService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsService.class);
    private final UibApplicationsConnection uibApplicationsConnection;
    private final CredentialStore credentialStore;
    private final String stsUrl;
    private static final String UAWA_ID = "2219";


    @Autowired
    @Configure
    public ApplicationsService(UibApplicationsConnection uibApplicationsConnection, CredentialStore credentialStore, @Configuration("securitytokenservice") String stsUri) {
        this.uibApplicationsConnection = uibApplicationsConnection;
        this.credentialStore = credentialStore;
        this.stsUrl = stsUri;
    }

    public String listAll(String applicationTokenId) throws AppException {
        String applications = null;
        if (hasAccess(applicationTokenId)) {
            applications = uibApplicationsConnection.listAll(applicationTokenId);
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }

    public String findApplication(String applicationTokenId, String userTokenId, String applicationName) throws AppException {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
            //FIXME handle no access to this method.
        }
        applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        return applications;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String query) throws AppException {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, query);
        } else {
            //FIXME handle no access to this method.
        }
        if (isUAWA(applicationTokenId, userTokenId)) {
            applications = ApplicationMapper.toJson(ApplicationMapper.fromJsonList(applications));
        } else {
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }

    public boolean hasAccess(String applicationTokenId, String userTokenId) {
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
    	Boolean userTokenIsAdmin = new CommandValidateWhydahAdminByUserTokenId(URI.create(stsUrl), applicationTokenId, userTokenId).execute();
    	if(!userTokenIsAdmin){
    		log.warn("CommandValidateWhydahAdminByUserTokenId failed, user token " + userTokenId + " does not have admin role");
    		return false;
    	}
    	Boolean isUASAccessOpen = new CommandVerifyUASAccessByApplicationTokenId(stsUrl, applicationTokenId).execute();
    	if(!isUASAccessOpen){
    		log.warn("CommandVerifyUASAccessByApplicationTokenId failed, app token " + applicationTokenId + " does not have UAS access");
    		return false;
    	}
    	return true;
    }

    public boolean hasAccess(String applicationTokenId) {
    	Boolean isUASAccessOpen = new CommandVerifyUASAccessByApplicationTokenId(stsUrl, applicationTokenId).execute();
    	if(!isUASAccessOpen){
    		log.warn("CommandVerifyUASAccessByApplicationTokenId failed, app token " + applicationTokenId + " does not have UAS access");
    		return false;
    	}
        return true;
    }

    boolean isUAWA(String applicationTokenId, String userTokenId) {
        log.trace("Checking isUAWA. UAWA_ID:{}applicationTokenId:{} userTokenId:{} ", UAWA_ID, applicationTokenId, userTokenId);
        String applicationID = new CommandGetApplicationIdFromApplicationTokenId(UriBuilder.fromUri(stsUrl).build(), applicationTokenId).execute();
        log.trace("CommandGetApplicationIdFromApplicationTokenId return appID:{} ", applicationID);
        return (UAWA_ID.equals(applicationID));
    }
}
