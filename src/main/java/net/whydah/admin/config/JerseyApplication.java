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

        log.debug(this.getClass().getSimpleName() + " started!");
    }
}