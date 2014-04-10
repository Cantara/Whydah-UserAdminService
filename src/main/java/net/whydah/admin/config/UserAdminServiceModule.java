package net.whydah.admin.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: asbkar
 * Date: 2/15/11
 * Time: 10:58 AM
 */
public class UserAdminServiceModule  {       //extends AbstractModule
    private final static Logger logger = LoggerFactory.getLogger(UserAdminServiceModule.class);

    private final AppConfig appConfig;
    private final String applicationmode;

    public UserAdminServiceModule(AppConfig appConfig, String applicationmode) {
        this.appConfig = appConfig;
        this.applicationmode = applicationmode;
    }

   // @Override
    protected void configure() {
        /*
        bind(AppConfig.class).toInstance(appConfig);
        if(applicationmode.equals(ApplicationMode.DEV)) {
            logger.info("Using TestUserAuthenticator to handle usercredentials");
            bind(UserAuthenticator.class).to(TestUserAuthenticator.class);
        } else {
            bind(UserAuthenticator.class).to(UserAuthenticatorImpl.class);
            URI useridbackendUri = URI.create(appConfig.getProperty("useridbackendUri"));
            bind(URI.class).annotatedWith(Names.named("useridbackendUri")).toInstance(useridbackendUri);
        }
        */
    }

}
