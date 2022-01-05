package net.whydah.admin;

import net.whydah.admin.applications.ApplicationsAdminResource;
import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.ddd.model.application.ApplicationId;
import net.whydah.sso.session.WhydahApplicationSession2;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Repository
public class CredentialStore {
	private static final Logger log = LoggerFactory.getLogger(CredentialStore.class);
	
    public static final int OK_APPLICATION_TOKEN_SET_MAX_SIZE = 30;
    public static final int OK_APPLICATION_TOKEN_ID_MAP_MAX_SIZE = 50;

    private static class WhydahApplicationSession2Singleton {
        private final static WhydahApplicationSession2 whydahApplicationSession;

        static {
            WAS2Configuration was2Conf = was2ConfigurationRef.get();
            if (was2Conf == null) {
                throw new IllegalStateException("No WAS2Configuration available for initialization of WAS2");
            }
            whydahApplicationSession = WhydahApplicationSession2.getInstance(was2Conf.stsUri, was2Conf.uasUri, was2Conf.uasApplicationCredential);
        }

        public static WhydahApplicationSession2 getSession() {
            return whydahApplicationSession;
        }
    }

    private static class WAS2Configuration {
        private final String stsUri;
        private final String uasUri;
        private final ApplicationCredential uasApplicationCredential;

        private WAS2Configuration(String stsUri, String uasUri, ApplicationCredential uasApplicationCredential) {
            this.stsUri = stsUri;
            this.uasUri = uasUri;
            this.uasApplicationCredential = uasApplicationCredential;
        }
    }

    private static final AtomicReference<WAS2Configuration> was2ConfigurationRef = new AtomicReference<>();

    private static Map<String, String> okApplicationTokenSet = Collections.synchronizedMap(new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > OK_APPLICATION_TOKEN_SET_MAX_SIZE;
        }
    });

    private static Map<String, String> okApplicationIDMap = Collections.synchronizedMap(new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > OK_APPLICATION_TOKEN_ID_MAP_MAX_SIZE;
        }
    });

    private final String myApplicationId;

    @Autowired
    @Configure
    public CredentialStore(@Configuration("securitytokenservice") String stsUri,
                           @Configuration("myuri") String uasUri,
                           @Configuration("applicationid") String applicationid,
                           @Configuration("applicationname") String applicationname,
                           @Configuration("applicationsecret") String applicationsecret) {
        this.myApplicationId = applicationid;
        ApplicationCredential uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
        was2ConfigurationRef.set(new WAS2Configuration(stsUri, uasUri, uasApplicationCredential));
        WhydahApplicationSession2Singleton.getSession(); // force WAS2 initialization
    }

    public String getMyApplicationID() {
        return myApplicationId;
    }

    public String getUserAdminServiceTokenId() {
        return getWas().getActiveApplicationTokenId();
    }

    public boolean hasValidApplicationSession() {
        return getWas().getActiveApplicationTokenId() != null;
    }

    public boolean isValidApplicationSession(URI tokenServiceUri, String applicationTokenId) {
        String emptyString = okApplicationTokenSet.remove(applicationTokenId); // remove to ensure that this item will be the most recently seen
        if (emptyString != null) {
            okApplicationTokenSet.put(applicationTokenId, "");
            return true;
        }
        boolean isOk = new CommandValidateApplicationTokenId(tokenServiceUri, getWas().getActiveApplicationTokenId()).execute();
        if (isOk) {
            okApplicationTokenSet.put(applicationTokenId, "");
        }
        return isOk;
    }

    public String getApplicationID(String callerApplicationTokenId) {
        String appId = okApplicationIDMap.remove(callerApplicationTokenId); // remove to ensure that this item will be the most recently seen
        if (appId != null) {
            okApplicationIDMap.put(callerApplicationTokenId, appId); // most-recent
            return appId;
        }
        if(getWas().getActiveApplicationToken() ==null) {
        	log.warn("No active token found. getWas().getActiveApplicationToken() returns null");
        }
        if(callerApplicationTokenId.equalsIgnoreCase(getWas().getActiveApplicationTokenId())) { //just ignore if it is me
        	okApplicationIDMap.put(callerApplicationTokenId, getWas().getActiveApplicationToken().getApplicationID());
            return getWas().getActiveApplicationToken().getApplicationID(); 
        } else {
        	appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(getWas().getSTS()), callerApplicationTokenId).execute();
        	if (appId != null && ApplicationId.isValid(appId)) {
        		okApplicationIDMap.put(callerApplicationTokenId, appId);
        		return appId;
        	}
        }
        return null;
    }

    public WhydahApplicationSession2 getWas() {
        return WhydahApplicationSession2Singleton.getSession();
    }
}
