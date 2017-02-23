package net.whydah.admin.applications;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhyDahRoleCheckUtil;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
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
    private final UibUserConnection uibUserConnection;
    private final CredentialStore credentialStore;
    private final String stsUrl;
    private WhyDahRoleCheckUtil adminChecker;

    @Autowired
    @Configure
    public ApplicationsService(UibApplicationsConnection uibApplicationsConnection, UibUserConnection uibUserConnection, CredentialStore credentialStore, @Configuration("securitytokenservice") String stsUri) {
        this.uibApplicationsConnection = uibApplicationsConnection;
        this.uibUserConnection = uibUserConnection;
        this.credentialStore = credentialStore;
        this.stsUrl = stsUri;
        setAdminChecker(new WhyDahRoleCheckUtil(stsUri, uibUserConnection, uibApplicationsConnection, credentialStore));
    }

    public String listAll(String applicationTokenId) throws AppException {
        String applications = null;
        if (getAdminChecker().hasAccess(applicationTokenId)) {
            applications = uibApplicationsConnection.listAll(applicationTokenId);
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }

    public String findApplication(String applicationTokenId, String userTokenId, String applicationName) throws AppException {
        String applications = null;
        if (getAdminChecker().hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
            //FIXME handle no access to this method.
        }
        applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        return applications;
    }
    
    public String findApplication(String applicationTokenId, String applicationName) throws AppException {
        String applications = null;
        if (getAdminChecker().hasAccess(applicationTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, applicationName);
        } else {
            //FIXME handle no access to this method.
        }
        applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        return applications;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String query) throws AppException {
        String applications = null;
        if (getAdminChecker().hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, query);
        } else {
            //FIXME handle no access to this method.
        }
        if (getAdminChecker().isUAWA(applicationTokenId, userTokenId)) {
            applications = ApplicationMapper.toJson(ApplicationMapper.fromJsonList(applications));
        } else {
            applications = ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }

	public WhyDahRoleCheckUtil getAdminChecker() {
		return adminChecker;
	}

	public void setAdminChecker(WhyDahRoleCheckUtil adminChecker) {
		this.adminChecker = adminChecker;
	}

   
}
