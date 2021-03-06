package net.whydah.admin.applications;

import net.whydah.admin.WhydahRoleCheckUtil;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.sso.application.mappers.ApplicationMapper;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static net.whydah.sso.util.LoggerUtil.first50;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationsService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsService.class);
    private final UibApplicationsConnection uibApplicationsConnection;
    private WhydahRoleCheckUtil adminChecker;

    public WhydahRoleCheckUtil getAdminChecker() {
        return adminChecker;
	}
    
    @Autowired
    @Configure
    public ApplicationsService(UibApplicationsConnection uibApplicationsConnection, WhydahRoleCheckUtil adminChecker) {
        this.uibApplicationsConnection = uibApplicationsConnection;
        this.adminChecker = adminChecker;
    }

    public String listAll(String applicationTokenId) throws AppException {
    	log.trace("listAll is called ");
        String applications = null;
        if (adminChecker.authorise(applicationTokenId)) {
            applications = uibApplicationsConnection.listAll(applicationTokenId);
            if (!adminChecker.isInternalWhydahAdminApp(applicationTokenId)) {
            	applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
            }
        } else {
            log.warn("adminChecked failed, returning 9992");
            throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        log.trace("listAll {}", first50(applications));
        return applications;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String applicationName) throws AppException {
        String applications = null;
        log.info("findByName - findApplications is called, query {}", applicationName);
        if (adminChecker.authorise(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
            log.warn("findByName - adminChecked failed, returning 9992");
            throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        if(!adminChecker.isInternalWhydahAdminApp(applicationTokenId)){
            log.info("findByName - isInternalWhydahAdminApp({} = false - filtering applications", applicationTokenId);
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        log.info("findByName {}", first50(applications));
        return applications;
    }
    

    public String findApplications(String applicationTokenId, String applicationName) throws AppException {
        String applications = null;
        log.info("findByName - findApplications is called, query {}", applicationName);
        if (adminChecker.authorise(applicationTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
            log.warn("findByName - adminChecked failed, returning 9992");
            throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
        if(!adminChecker.isInternalWhydahAdminApp(applicationTokenId)){
            log.info("findByName - isInternalWhydahAdminApp({} = false - filtering applications", applicationTokenId);
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        log.info("findByName {}", first50(applications));
        return applications;
    }
   

}
