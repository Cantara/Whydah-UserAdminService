package net.whydah.admin.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationPath("overriddenByWebXml")
public class JerseyApplication extends ResourceConfig {
    private static final Logger log = LoggerFactory.getLogger(JerseyApplication.class);

    public JerseyApplication() {
        // Register package for scanning
        packages("net.whydah.admin");

        // Do NOT disable auto-discovery when using HK2
        // property("jersey.config.disableAutoDiscovery", true);

        // Register Spring integration
        // Register Spring integration
        try {
            // Try to register the Spring integration using the correct package structure
            // Jersey Spring integration is handled automatically as long as jersey-spring6 is on the classpath
            log.info("Using Jersey Spring 6 integration from jersey-spring6 library");

            // Explicitly register the filter (if needed)
            register(org.glassfish.jersey.server.spring.scope.RequestContextFilter.class);
        } catch (Throwable e) {
            log.error("Spring 6 integration failed! Check your dependencies", e);
        }
//        try {
//            // Try to register the Spring 6 integration
//            Class<?> filterClass = Class.forName("org.glassfish.jersey.ext.spring6.SpringComponentProvider");
//            log.info("Found Spring 6 integration: {}", filterClass.getName());
//            register(org.glassfish.jersey.server.spring.scope.RequestContextFilter.class);
//        } catch (ClassNotFoundException e) {
//            log.error("Spring 6 integration not found! Dependency may be missing", e);
//        }

        log.debug(this.getClass().getSimpleName() + " started!");
    }
}