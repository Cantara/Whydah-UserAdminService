package net.whydah.admin.applications.uib;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.security.UASCredentials;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


public class CommandGetApplicationsFromUIB extends HystrixCommand<Response> {
    public static final String UAS_APP_CREDENTIAL_XML = "uasAppCredentialXml";
    private static final String APPLICATIONS_PATH = "applications";  //   uib.path(applicationTokenId).path("applications");
    private static final Logger log = getLogger(CommandGetApplicationsFromUIB.class);
    private String uibUri;
    private String stsApplicationtokenId;
    private String uasAppCredentialXml;

    public CommandGetApplicationsFromUIB(String uibUri, String stsApplicationtokenId, String uasAppCredentialXml) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("UASUserAdminGroup")).
                andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3000)));

        this.uibUri = uibUri;
        this.stsApplicationtokenId = stsApplicationtokenId;
        this.uasAppCredentialXml = uasAppCredentialXml;
        if (uibUri == null || stsApplicationtokenId == null || uasAppCredentialXml == null) {
            log.error("{} initialized with null-values - will fail", CommandGetApplicationsFromUIB.class.getSimpleName());
        }
    }

    @Override
    protected Response run() {
        log.trace("{} - stsApplicationtokenId={}, ", CommandGetApplicationsFromUIB.class.getSimpleName(), stsApplicationtokenId);
        Client client = ClientBuilder.newClient();
        WebTarget uib = client.target(uibUri);
        WebTarget webResource = uib.path(stsApplicationtokenId).path(APPLICATIONS_PATH);
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>(2);
        formData.add(UAS_APP_CREDENTIAL_XML, uasAppCredentialXml);
        return webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasAppCredentialXml).get();

    }

    @Override
    protected Response getFallback() {
        log.warn("{} - timeout - uibUri={}", CommandGetApplicationsFromUIB.class.getSimpleName(), uibUri);
        return null;
    }
}
