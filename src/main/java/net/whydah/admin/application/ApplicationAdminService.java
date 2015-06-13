package net.whydah.admin.application;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.applications.StubbedApplicationsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationAdminService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationAdminService.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm");
    private final CredentialStore credentialStore;
    private final StubbedApplicationsRepository stubbedApplicationsRepository;


    @Autowired
    public ApplicationAdminService(CredentialStore credentialStore, StubbedApplicationsRepository stubbedApplicationsRepository) {
        this.credentialStore = credentialStore;
        this.stubbedApplicationsRepository = stubbedApplicationsRepository;
    }

    public void createApplication(String applicationTokenId, String userTokenId,String applicationJson) {
        if (hasAccess(applicationTokenId, userTokenId)) {
            stubbedApplicationsRepository.addApplication(applicationJson);
        } else {
            //FIXME handle no access to this method.
        }
    }

    public String getApplication(String applicationTokenId, String userTokenId, String applicationId) {
        String application = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            application = stubbedApplicationsRepository.findById(applicationId);
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
