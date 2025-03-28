package net.whydah.admin.config;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Context;

@ApplicationPath("overriddenByWebXml")
public class JerseyApplication extends ResourceConfig {
    private static final Logger log = LoggerFactory.getLogger(JerseyApplication.class);

    @Context
    private ServletContext servletContext;

    
    public JerseyApplication() {
    
    	packages("net.whydah.admin");

    	// Register Spring integration
    	register(RequestContextFilter.class);
    	register(org.glassfish.jersey.server.spring.SpringComponentProvider.class);
    	register(JerseyBindings.class);
     
        log.info("Registered classes: {}", getClasses());
    	log.debug(this.getClass().getSimpleName() + " started!");

        
    }
   
    
}