package net.whydah.admin.applications;

import net.whydah.admin.CredentialStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationsService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsService.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm");
    private final UibApplicationsConnection uibApplicationsConnection;
    private final CredentialStore credentialStore;


    @Autowired
    public ApplicationsService(UibApplicationsConnection uibApplicationsConnection, CredentialStore credentialStore) {
        this.uibApplicationsConnection = uibApplicationsConnection;
        this.credentialStore = credentialStore;
    }

    public String listAll(String applicationTokenId, String userTokenId) {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.listAll(applicationTokenId, userTokenId);
        } else {
            //FIXME handle no access to this method.
        }
        return applications;
    }

    public String findApplication(String applicationTokenId, String userTokenId, String applicationName) {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.listAll(applicationTokenId, userTokenId);
        } else {
            //FIXME handle no access to this method.
        }
        return applications;
    }

    public String findApplications(String applicationTokenId, String userTokenId, String query) {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, userTokenId, query);
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
