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

        log.debug(this.getClass().getSimpleName() + " started!");
    }
}