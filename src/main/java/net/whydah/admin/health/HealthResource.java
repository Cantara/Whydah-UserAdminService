package net.whydah.admin.health;

import net.whydah.admin.CredentialStore;
import net.whydah.sso.util.WhydahUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Endpoint for health check, copied from UIB.
 */
@Component
@Path("/health")
public class HealthResource {
    private static final Logger log = LoggerFactory.getLogger(HealthResource.class);
    private final CredentialStore credentialStore;


    public HealthResource(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;

    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isHealthy() {
        boolean ok = true;
        String statusText = WhydahUtil.getPrintableStatus(credentialStore.getWas());
        log.trace("isHealthy={}, status: {}", ok, statusText);
        if (ok) {
            return Response.ok("Status OK!\n" + statusText).build();
        } else {
            //Intentionally not returning anything the client can use to determine what's the error for security reasons.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
