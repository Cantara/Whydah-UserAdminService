package net.whydah.admin.applications;

import net.whydah.admin.CredentialStore;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.text.SimpleDateFormat;

/**
 * Created by baardl on 29.03.14.
 */
@Service
public class ApplicationsService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsService.class);
    private final UibApplicationsConnection uibApplicationsConnection;
    private final CredentialStore credentialStore;
    private static final String UAWA_ID="2219";

    public static String stsUrl;


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
        if (isUAWA(applicationTokenId, userTokenId)){
            applications= ApplicationMapper.toJson(ApplicationMapper.fromJsonList(applications));
        } else {
            applications= ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }

    public String findApplication(String applicationTokenId, String userTokenId, String applicationName) {
        String applications = null;
        if (hasAccess(applicationTokenId, userTokenId)) {
            applications = uibApplicationsConnection.findApplications(applicationTokenId, userTokenId,applicationName);
        } else {
            //FIXME handle no access to this method.
        }
        if (isUAWA(applicationTokenId, userTokenId)){
            applications= ApplicationMapper.toJson(ApplicationMapper.fromJsonList(applications));
        } else {
            applications= ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
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
        if (isUAWA(applicationTokenId, userTokenId)){
            applications= ApplicationMapper.toJson(ApplicationMapper.fromJsonList(applications));
        } else {
            applications= ApplicationMapper.toSafeJson(ApplicationMapper.fromJsonList(applications));
        }
        return applications;
    }

    boolean hasAccess(String applicationTokenId, String userTokenId) {
        //FIXME validate user and applciation trying to create a new application.
        return true;
    }

    boolean isUAWA(String applicationTokenId, String userTokenId){
        log.trace("Checking isUAWA. UAWA_ID:{}applicationTokenId:{} userTokenId:{} ",UAWA_ID,applicationTokenId, userTokenId);
        String applicationID = new CommandGetApplicationIdFromApplicationTokenId(UriBuilder.fromUri(stsUrl).build(), applicationTokenId).execute();
        log.trace("CommandGetApplicationIdFromApplicationTokenId return appID:{} ",applicationID);
        return (UAWA_ID.equals(applicationID));
        //return true;
    }
}
