package net.whydah.admin.config;

import net.whydah.admin.application.ApplicationResource;
import net.whydah.admin.applications.ApplicationsResource;
import net.whydah.admin.auth.LogonController;
import net.whydah.admin.auth.PasswordController;
import net.whydah.admin.createlogon.CreateLogonUserController;
import net.whydah.admin.health.HealthResource;
import net.whydah.admin.user.UserAggregateResource;
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
        register(RequestContextFilter.class);   //Should this be here?

        register(ApplicationResource.class);
        register(ApplicationsResource.class);
        register(UserService.class);        //Should this be here?
        register(UserResource.class);
        register(UserAggregateResource.class);
        register(UsersResource.class);
        register(LogonController.class);
        register(CreateLogonUserController.class);
        register(PasswordController.class);
        register(HealthResource.class);

        //register(SpringRequestResource.class);
        //register(CustomExceptionMapper.class);
        //https://java.net/jira/browse/JERSEY-2175
       // ResourceConfig resourceConfig = packages("net.whydah.admin");

        //resourceConfig.register(MultiPartFeature.class);
        log.debug(this.getClass().getSimpleName() + " started!");
    }
}
