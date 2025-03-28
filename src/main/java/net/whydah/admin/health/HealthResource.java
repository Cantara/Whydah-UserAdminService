package net.whydah.admin.health;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.CredentialStore;
import net.whydah.sso.util.WhydahUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Properties;

/**
 * Endpoint for health check, copied from UIB.
 */
@Component
@Path("/health")
public class HealthResource {
    private static final Logger log = LoggerFactory.getLogger(HealthResource.class);
    @Autowired
    private CredentialStore credentialStore;
    
    @Value("${applicationname}") 
    private String applicationInstanceName;
    private static boolean ok = true;


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isHealthy() {
//        String statusText = WhydahUtil.getPrintableStatus(credentialStore.getWas());
//        log.trace("isHealthy={}, status: {}", ok, statusText);
        if (ok) {
            return Response.ok(getHealthTextJson()).build();
        } else {
            //Intentionally not returning anything the client can use to determine what's the error for security reasons.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    public String getHealthTextJson() {
        return "{\n" +
                "  \"Status\": \"" + ok + "\",\n" +
                "  \"Version\": \"" + getVersion() + "\",\n" +
                "  \"DEFCON\": \"" + credentialStore.getWas().getDefcon() + "\",\n" +
                "  \"STS\": \"" + credentialStore.getWas().getSTS() + "\",\n" +
                "  \"UAS\": \"" + credentialStore.getWas().getUAS() + "\",\n" +
                "  \"hasApplicationToken\": \"" + Boolean.toString((credentialStore.getWas().getActiveApplicationTokenId() != null)) + "\",\n" +
                // "  \"hasValidApplicationToken\": \"" + Boolean.toString(credentialStore.getWas().checkActiveSession()) + "\",\n" +
                "  \"hasApplicationsMetadata\": \"" + Boolean.toString(credentialStore.getWas().hasApplicationMetaData()) + "\",\n" +

                "  \"now\": \"" + Instant.now() + "\",\n" +
                "  \"running since\": \"" + WhydahUtil.getRunningSince() + "\"\n\n" +

                "}\n";
    }


    private String getVersion() {
        Properties mavenProperties = new Properties();
        String resourcePath = "/META-INF/maven/net.whydah.identity/UserAdminService/pom.properties";
        URL mavenVersionResource = HealthResource.class.getResource(resourcePath);
        if (mavenVersionResource != null) {
            try {
                mavenProperties.load(mavenVersionResource.openStream());
                return mavenProperties.getProperty("version", "missing version info in " + resourcePath) + " [" + applicationInstanceName + " - " + WhydahUtil.getMyIPAddresssesString() + "]";
            } catch (IOException e) {
                log.warn("Problem reading version resource from classpath: ", e);
            }
        }
        return "(DEV VERSION)" + " [" + applicationInstanceName + " - " + WhydahUtil.getMyIPAddresssesString() + "]";
    }
}