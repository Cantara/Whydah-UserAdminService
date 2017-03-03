package net.whydah.admin.applications;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhyDahRoleCheckUtil;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.application.mappers.ApplicationMapper;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationsService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsService.class);
    private final UibApplicationsConnection uibApplicationsConnection;
    private WhyDahRoleCheckUtil adminChecker;
    public WhyDahRoleCheckUtil getAdminChecker(){
		return adminChecker;
	}
    
    @Autowired
    @Configure
    public ApplicationsService(UibApplicationsConnection uibApplicationsConnection, WhyDahRoleCheckUtil adminChecker) {
        this.uibApplicationsConnection = uibApplicationsConnection;
        this.adminChecker = adminChecker;
    }

    public String listAll(String applicationTokenId) throws AppException {
    	log.trace("listAll is called ");
        String applications = null;
        if (adminChecker.authorise(applicationTokenId)) {
            applications = uibApplicationsConnection.listAll(applicationTokenId);
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        log.trace("listAll {}", applications);
        return applications;
    }

    public String findApplication(String applicationTokenId, String userTokenId, String applicationName) throws AppException {
        String applications = null;
    	log.trace("findByName - listAll is called, query {}", applicationName);
        if (adminChecker.authorise(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        log.trace("findByName {}", applications);
        return applications;
    }
    
    public String findApplication(String applicationTokenId, String applicationName) throws AppException {
        String applications = null;
        log.trace("findByName - listAll is called, query {}", applicationName);
        if (adminChecker.authorise(applicationTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        log.trace("findByName {}", applications);
        return applications;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String query) throws AppException {
        String applications = null;
        if (adminChecker.authorise(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, query);
        } else {
        	throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        if (adminChecker.isInternalWhyDahAdminApp(applicationTokenId)) {
            applications = ApplicationMapper.toJson(ApplicationMapper.fromJsonList(applications));
        } else {
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }
    
   

}
