package net.whydah.admin.applications;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationsAdminService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsAdminService.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm");
    private final UibApplicationsConnection uibApplicationsConnection;
    private final CredentialStore credentialStore;
    private final StubbedApplicationsRepository applicationsRepository;


    @Autowired
    public ApplicationsAdminService(UibApplicationsConnection uibApplicationsConnection, CredentialStore credentialStore, StubbedApplicationsRepository applicationsRepository) {
        this.uibApplicationsConnection = uibApplicationsConnection;
        this.credentialStore = credentialStore;
        this.applicationsRepository = applicationsRepository;
    }

    public String listAll(String applicationTokenId, String userTokenId) {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
//            applications = uibApplicationsConnection.listAll(credentialStore.getUserAdminServiceTokenId(), userTokenId);
            applications = applicationsRepository.findAll();
        } else {
            //FIXME handle no access to this method.
        }
        return applications;
    }

    public String findApplication(String applicationTokenId, String userTokenId, String applicationName) {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
//            applications = uibApplicationsConnection.listAll(credentialStore.getUserAdminServiceTokenId(), userTokenId);
            applications = applicationsRepository.findByName(applicationName);
        } else {
            //FIXME handle no access to this method.
        }
        return applications;
    }

    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new application.
        return true;
    }
}
