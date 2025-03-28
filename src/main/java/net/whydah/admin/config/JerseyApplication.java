package net.whydah.admin.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("overriddenByWebXml")
public class JerseyApplication extends ResourceConfig {
    private static final Logger log = LoggerFactory.getLogger(JerseyApplication.class);

    public JerseyApplication() {
    
        // Register package for scanning
        packages("net.whydah.admin");

        // Register Spring integration
        register(RequestContextFilter.class);
        
        property("jersey.config.server.disableHk2", true);

        log.info("SpringComponentProvider present: {}", isClassPresent("org.glassfish.jersey.ext.spring6.SpringComponentProvider"));
        log.info("Registered classes: {}", getClasses());
        log.debug(this.getClass().getSimpleName() + " started!");
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}