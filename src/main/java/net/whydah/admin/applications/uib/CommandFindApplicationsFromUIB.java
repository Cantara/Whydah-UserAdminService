package net.whydah.admin.applications.uib;


import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import net.whydah.admin.security.UASCredentials;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;


public class CommandFindApplicationsFromUIB extends HystrixCommand<Response> {
    public static final String UAS_APP_CREDENTIAL_XML = "uasAppCredentialXml";
    private static final String APPLICATIONS_FIND_PATH = "applications/find";  //   uib.path(applicationTokenId).path("applications");
    private static final Logger log = getLogger(CommandFindApplicationsFromUIB.class);
    private String uibUri;
    private String stsApplicationtokenId;
    private String uasAppCredentialXml;
    private String query;

    public CommandFindApplicationsFromUIB(String uibUri, String stsApplicationtokenId, String uasAppCredentialXml, String query) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("UASUserAdminGroup")).
                andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3000)));

        this.uibUri = uibUri;
        this.stsApplicationtokenId = stsApplicationtokenId;
        this.uasAppCredentialXml = uasAppCredentialXml;
        this.query = query;
        if (uibUri == null || stsApplicationtokenId == null || uasAppCredentialXml == null) {
            log.error("{} initialized with null-values - will fail", CommandFindApplicationsFromUIB.class.getSimpleName());
        }
    }

    @Override
    protected Response run() {
        log.trace("{} - stsApplicationtokenId={}, ", CommandFindApplicationsFromUIB.class.getSimpleName(), stsApplicationtokenId);
        Client client = ClientBuilder.newClient();
        WebTarget uib = client.target(uibUri);
        WebTarget webResource = uib.path(stsApplicationtokenId).path(APPLICATIONS_FIND_PATH).path(query);
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>(2);
        formData.add(UAS_APP_CREDENTIAL_XML, uasAppCredentialXml);
        return webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, UASCredentials.encode(uasAppCredentialXml)).get();


    }


    @Override
    protected Response getFallback() {
        log.warn("{} - timeout - uibUri={}", CommandFindApplicationsFromUIB.class.getSimpleName(), uibUri);
        return null;
    }
}
