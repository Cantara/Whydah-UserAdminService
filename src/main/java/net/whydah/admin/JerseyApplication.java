package net.whydah.admin;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a>
 */
@ApplicationPath("overriddenByWebXml")
public class JerseyApplication extends ResourceConfig {
    private static final Logger log = LoggerFactory.getLogger(JerseyApplication.class);

    public JerseyApplication() {
        //https://java.net/jira/browse/JERSEY-2175
        ResourceConfig resourceConfig = packages("net.whydah.admin");

        //resourceConfig.register(MultiPartFeature.class);
        log.debug(this.getClass().getSimpleName() + " started!");
    }
}
