package net.whydah.admin.config;

import net.whydah.admin.errorhandling.AppExceptionMapper;
import net.whydah.admin.errorhandling.ExceptionConfig;
import net.whydah.admin.errorhandling.GenericExceptionMapper;
import net.whydah.admin.errorhandling.NotFoundExceptionMapper;

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
       ResourceConfig resourceConfig = packages("net.whydah.admin");
       log.debug(this.getClass().getSimpleName() + " started!");
    }
}
