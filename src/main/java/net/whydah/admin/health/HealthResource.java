package net.whydah.admin.health;

import net.whydah.admin.CredentialStore;
import net.whydah.sso.util.WhydahUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Endpoint for health check, copied from UIB.
 */
@Component
@Path("/health")
public class HealthResource {
    private static final Logger log = LoggerFactory.getLogger(HealthResource.class);
    private final CredentialStore credentialStore;


    @Autowired
    public HealthResource(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;

    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isHealthy() {
        boolean ok = true;
        String statusText = WhydahUtil.getPrintableStatus(credentialStore.getWas());
        log.trace("isHealthy={}, status: {}", ok, statusText);
        if (ok) {
            return Response.ok(getHealthTextJson()).build();
        } else {
            //Intentionally not returning anything the client can use to determine what's the error for security reasons.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public static String getHealthTextJson() {
        return "{\n" +
                "  \"Status\": \"OK\",\n" +
                "  \"Version\": \"" + getVersion() + "\",\n" +
                "  \"DEFCON\": \"" + "DEFCON5" + "\"\n" +
                "}\n";
    }

    private static String getVersion() {
        Properties mavenProperties = new Properties();
        String resourcePath = "/META-INF/maven/net.whydah.identity/UserAdminService/pom.properties";
        URL mavenVersionResource = HealthResource.class.getResource(resourcePath);
        if (mavenVersionResource != null) {
            try {
                mavenProperties.load(mavenVersionResource.openStream());
                return mavenProperties.getProperty("version", "missing version info in " + resourcePath);
            } catch (IOException e) {
                log.warn("Problem reading version resource from classpath: ", e);
            }
        }
        return "(DEV VERSION)";
    }

}
