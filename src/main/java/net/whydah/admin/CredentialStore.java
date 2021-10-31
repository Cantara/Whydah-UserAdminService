package net.whydah.admin;

import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.ddd.model.application.ApplicationId;
import net.whydah.sso.session.WhydahApplicationSession;
import net.whydah.sso.session.WhydahApplicationSession2;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Repository
public class CredentialStore {

    private static WhydahApplicationSession2 whydahApplicationSession = null;
    private final String stsUri;
    private final String uasUri;
    private final ApplicationCredential uasApplicationCredential;
    private static Set okApplicationTokenSet = new LinkedHashSet<String>();
    private static Map<String, String> okApplicationIDMap = new LinkedHashMap<String, String>();


    @Autowired
    @Configure
    public CredentialStore(@Configuration("securitytokenservice") String stsUri,
                           @Configuration("myuri") String uasUri,
                           @Configuration("applicationid") String applicationid,
                           @Configuration("applicationname") String applicationname,
                           @Configuration("applicationsecret") String applicationsecret) {
        this.stsUri = stsUri;
        this.uasUri = uasUri;
        this.uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
        getWas();//Init WAS first
    }

    public String getMyApplicationID() {
        return uasApplicationCredential.getApplicationID();
    }

    public String getUserAdminServiceTokenId() {
        if (whydahApplicationSession == null) {
            getWas();
        }
        return getWas().getActiveApplicationTokenId();

    }

    public boolean hasValidApplicationSession() {
        return getWas().getActiveApplicationTokenId() != null;
    }

    public boolean isValidApplicationSession(URI tokenServiceUri, String applicationTokenId) {
        if (okApplicationTokenSet.size() > 30) { // small size, no timeout yet
            okApplicationTokenSet = new LinkedHashSet<String>();
        }
        if (okApplicationTokenSet.contains(applicationTokenId)) {
            return true;
        }
        boolean isOk = new CommandValidateApplicationTokenId(tokenServiceUri, getWas().getActiveApplicationTokenId()).execute();
        if (isOk) {
            okApplicationTokenSet.add(applicationTokenId);
        }
        return isOk;

    }

    public String getApplicationID(String callerApplicationTokenId) {
        if (okApplicationIDMap.size() > 50) { // small size, no timeout
            okApplicationIDMap = new LinkedHashMap<String, String>();
        }
        if (okApplicationIDMap.containsKey(callerApplicationTokenId)) {
            return okApplicationIDMap.get(callerApplicationTokenId);
        }
        String appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(getWas().getSTS()), callerApplicationTokenId).execute();
        //HUY: it is a bug, appid can be null for the first time. we should only add the value to the list if it is something not a null value
        if (appId!=null && ApplicationId.isValid(appId)) {
            okApplicationIDMap.put(callerApplicationTokenId, appId);
            return appId;
        }
        return null;

    }

    public WhydahApplicationSession2 getWas() {
        if (whydahApplicationSession == null) {
            setWas(WhydahApplicationSession2.getInstance(stsUri, uasUri, uasApplicationCredential));
        }
        return whydahApplicationSession;
    }

    public synchronized void setWas(WhydahApplicationSession2 was) {
        whydahApplicationSession = was;
    }
}
