package net.whydah.admin;

import net.whydah.admin.application.ApplicationResource;
import net.whydah.admin.application.ApplicationService;
import net.whydah.admin.application.UibApplicationConnection;
import net.whydah.admin.auth.LogonController;
import net.whydah.admin.config.AppConfig;
import net.whydah.admin.user.UserResource;
import net.whydah.admin.user.UserService;
import net.whydah.admin.users.UsersResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
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

        register(RequestContextFilter.class);
        register(AppConfig.class);
        register(UibApplicationConnection.class);

        register(ApplicationService.class);
        register(ApplicationResource.class);
        register(UserService.class);
        register(UserResource.class);
        register(UsersResource.class);
        register(LogonController.class);
        //register(SpringRequestResource.class);
        //register(CustomExceptionMapper.class);
        //https://java.net/jira/browse/JERSEY-2175
       // ResourceConfig resourceConfig = packages("net.whydah.admin");

        //resourceConfig.register(MultiPartFeature.class);
        log.debug(this.getClass().getSimpleName() + " started!");
    }
}
