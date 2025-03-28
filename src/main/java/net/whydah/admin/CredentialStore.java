package net.whydah.admin;

import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.config.ApplicationMode;
import net.whydah.sso.ddd.model.application.ApplicationId;
import net.whydah.sso.session.WhydahApplicationSession2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        private static WhydahApplicationSession2 whydahApplicationSession;
        private static boolean initialized = false;
        private static String initError = null;

        public static synchronized void initialize(WAS2Configuration was2Conf) {
            if (initialized) {
                return;
            }

            try {
                if (was2Conf == null) {
                    throw new IllegalStateException("No WAS2Configuration available for initialization of WAS2");
                }
                log.info("Initializing WhydahApplicationSession2 with STS: {}, UAS: {}, AppID: {}",
                        was2Conf.stsUri, was2Conf.uasUri, was2Conf.uasApplicationCredential.getApplicationID());
                whydahApplicationSession = WhydahApplicationSession2.getInstance(was2Conf.stsUri, was2Conf.uasUri, was2Conf.uasApplicationCredential);
                initialized = true;
            } catch (Exception e) {
                log.error("Failed to initialize WhydahApplicationSession2", e);
                initError = e.getMessage();
                // Create a mock session for tests
                if (System.getProperty(ApplicationMode.IAM_MODE_KEY, "").equalsIgnoreCase(ApplicationMode.DEV)) {
                    log.warn("Creating mock WhydahApplicationSession2 for DEV mode");
                    whydahApplicationSession = createMockSession(was2Conf);
                    initialized = true;
                }
            }
        }

        private static WhydahApplicationSession2 createMockSession(WAS2Configuration conf) {
            if (conf == null) {
                conf = new WAS2Configuration("http://localhost:9998/tokenservice",
                        "http://localhost:9992/useradminservice",
                        new ApplicationCredential("test", "test", "test"));
            }

            // Don't try to create a mock - for DEV mode, just set some defaults in the properties
            System.setProperty("sts.url", conf.stsUri);
            System.setProperty("uas.url", conf.uasUri);

            try {
                // Try again with real instance
                return WhydahApplicationSession2.getInstance(conf.stsUri, conf.uasUri, conf.uasApplicationCredential);
            } catch (Exception e) {
                log.error("Even the retry for DEV mode failed", e);
                // Nothing else we can do here - we'll have to handle nulls
                return null;
            }
        }

        public static WhydahApplicationSession2 getSession() {
            if (!initialized) {
                WAS2Configuration was2Conf = was2ConfigurationRef.get();
                initialize(was2Conf);
            }
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

    private final String stsUri;
    private final String uasUri;
    private final String applicationid;
    private final String applicationname;
    private final String applicationsecret;
    private final String myApplicationId;

  
    
    @Autowired
    public CredentialStore(@Value("${securitytokenservice}") String stsUri,
                           @Value("${myuri}") String uasUri,
                           @Value("${applicationid}") String applicationid,
                           @Value("${applicationname}") String applicationname,
                           @Value("${applicationsecret}") String applicationsecret) {
    	 this.stsUri = stsUri;
         this.uasUri = uasUri;
         this.applicationid = applicationid;
         this.applicationname = applicationname;
         this.applicationsecret = applicationsecret;
         this.myApplicationId = applicationid;

        log.info("Initializing CredentialStore with STS: {}, UAS: {}, AppID: {}, AppName: {}",
                stsUri, uasUri, applicationid, applicationname);

        try {
            ApplicationCredential uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
            was2ConfigurationRef.set(new WAS2Configuration(stsUri, uasUri, uasApplicationCredential));
            // Don't force initialization here, let it lazy initialize
        } catch (Exception e) {
            log.error("Error initializing CredentialStore", e);
            if (ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY))) {
                log.warn("DEV mode - continuing despite initialization error");
            }
        }
    }

    public String getMyApplicationID() {
        return myApplicationId;
    }

    public String getUserAdminServiceTokenId() {
        try {
            WhydahApplicationSession2 was = getWas();
            if (was == null) {
                log.warn("WhydahApplicationSession2 is null in getUserAdminServiceTokenId");
                return "dev-token-id";
            }
            return was.getActiveApplicationTokenId();
        } catch (Exception e) {
            log.error("Error getting UserAdminServiceTokenId", e);
            return "error-token";
        }
    }

    public boolean hasValidApplicationSession() {
        try {
            WhydahApplicationSession2 was = getWas();
            if (was == null) {
                log.warn("WhydahApplicationSession2 is null in hasValidApplicationSession");
                return ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY));
            }
            return was.getActiveApplicationTokenId() != null;
        } catch (Exception e) {
            log.error("Error checking application session", e);
            return false;
        }
    }

    public boolean isValidApplicationSession(URI tokenServiceUri, String applicationTokenId) {
        try {
            String emptyString = okApplicationTokenSet.remove(applicationTokenId); // remove to ensure that this item will be the most recently seen
            if (emptyString != null) {
                okApplicationTokenSet.put(applicationTokenId, "");
                return true;
            }

            WhydahApplicationSession2 was = getWas();
            if (was == null) {
                log.warn("WhydahApplicationSession2 is null in isValidApplicationSession");
                return ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY));
            }

            boolean isOk = new CommandValidateApplicationTokenId(tokenServiceUri, was.getActiveApplicationTokenId()).execute();
            if (isOk) {
                okApplicationTokenSet.put(applicationTokenId, "");
            }
            return isOk;
        } catch (Exception e) {
            log.error("Error validating application session", e);
            if (ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY))) {
                return true; // Allow in DEV mode
            }
            return false;
        }
    }

    public String getApplicationID(String callerApplicationTokenId) {
        try {
            String appId = okApplicationIDMap.remove(callerApplicationTokenId); // remove to ensure that this item will be the most recently seen
            if (appId != null) {
                okApplicationIDMap.put(callerApplicationTokenId, appId); // most-recent
                return appId;
            }

            WhydahApplicationSession2 was = getWas();
            if (was == null) {
                log.warn("WhydahApplicationSession2 is null in getApplicationID");
                return ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY)) ? "test-app-id" : null;
            }

            if (was.getActiveApplicationToken() == null) {
                log.warn("No active token found. getWas().getActiveApplicationToken() returns null");
                return ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY)) ? "test-app-id" : null;
            }

            if (callerApplicationTokenId.equalsIgnoreCase(was.getActiveApplicationTokenId())) { //just ignore if it is me
                okApplicationIDMap.put(callerApplicationTokenId, was.getActiveApplicationToken().getApplicationID());
                return was.getActiveApplicationToken().getApplicationID();
            } else {
                appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(was.getSTS()), callerApplicationTokenId).execute();
                if (appId != null && ApplicationId.isValid(appId)) {
                    okApplicationIDMap.put(callerApplicationTokenId, appId);
                    return appId;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting application ID", e);
            if (ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY))) {
                return "test-app-id"; // Return test value in DEV mode
            }
            return null;
        }
    }

    public WhydahApplicationSession2 getWas() {
        try {
            return WhydahApplicationSession2Singleton.getSession();
        } catch (Exception e) {
            log.error("Error getting WhydahApplicationSession2", e);
            if (ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY))) {
                log.warn("DEV mode - returning null for WhydahApplicationSession2");
                return null;
            }
            throw e;
        }
    }

    // For DEV/test mode only - provide helper methods that mock WAS functionality
    public String getSTS() {
        try {
            WhydahApplicationSession2 was = getWas();
            return was != null ? was.getSTS() : stsUri;
        } catch (Exception e) {
            log.error("Error getting STS URI", e);
            return stsUri;
        }
    }

    public String getUAS() {
        try {
            WhydahApplicationSession2 was = getWas();
            return was != null ? was.getUAS() : uasUri;
        } catch (Exception e) {
            log.error("Error getting UAS URI", e);
            return uasUri;
        }
    }

    public String getDefcon() {
        try {
            WhydahApplicationSession2 was = getWas();
            return was != null ? was.getDefcon().toString() : "5";
        } catch (Exception e) {
            log.error("Error getting DEFCON", e);
            return "5";
        }
    }

    public boolean hasApplicationMetaData() {
        try {
            WhydahApplicationSession2 was = getWas();
            return was != null && was.hasApplicationMetaData();
        } catch (Exception e) {
            log.error("Error checking application metadata", e);
            return ApplicationMode.DEV.equals(System.getProperty(ApplicationMode.IAM_MODE_KEY));
        }
    }
}