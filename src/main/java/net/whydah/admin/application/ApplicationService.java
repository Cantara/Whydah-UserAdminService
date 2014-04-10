package net.whydah.admin.application;

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
public class ApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm");
    private final UibConnection uibConnection;
    private final CredentialStore credentialStore;


    @Autowired
    public ApplicationService(UibConnection uibConnection, CredentialStore credentialStore) {
        this.uibConnection = uibConnection;
        this.credentialStore = credentialStore;
    }

    public Application createApplication(String applicationTokenId, String userTokenId,String applicationJson) {
        Application application = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            application = uibConnection.addApplication(credentialStore.getUserAdminServiceTokenId(), userTokenId, applicationJson);
        } else {
            //FIXME handle no access to this method.
        }
        return application;
    }
    public Application createApplicationFromXml(String applicationTokenId, String userTokenId,String applicationXml) {
        Application createdApplication = null;
        Application application = Application.fromXml(applicationXml);
        if (application != null) {
            String applicationJson = application.toJson();
            createdApplication = createApplication(applicationTokenId, userTokenId, applicationJson);
        }
        return createdApplication;
    }

    public Application getApplication(String applicationTokenId, String userTokenId, String applicationId) {
        Application application = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            application = uibConnection.getApplication(credentialStore.getUserAdminServiceTokenId(), userTokenId, applicationId);
        } else {
            //FIXME handle no access to this method.
        }
        return application;
    }


    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new application.
        return true;
    }
}
