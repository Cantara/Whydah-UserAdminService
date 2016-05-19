package net.whydah.admin.application;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import net.whydah.admin.security.UASCredentials;
import net.whydah.sso.internal.commands.uib.adminapi.application.CommandAuthenticateApplicationUAS;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Used by UAS to autenticate application against UIB.
 *
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-11-21.
 */
public class CommandAuthenticateApplicationUIB extends HystrixCommand<Response> {
    public static final String UAS_APP_CREDENTIAL_XML = "uasAppCredentialXml";
    private static final String APPLICATION_AUTH_PATH = "application/auth";
    private static final Logger log = getLogger(CommandAuthenticateApplicationUIB.class);
    private String uibUri;
    private String stsApplicationtokenId;
    private String uasAppCredentialXml;
    private String appCredentialXml;

    public CommandAuthenticateApplicationUIB(String uibUri, String stsApplicationtokenId, String uasAppCredentialXml,
                                             String appCredentialXml) {
        super(HystrixCommandGroupKey.Factory.asKey("UIBApplicationAdminGroup"));
        this.uibUri = uibUri;
        this.stsApplicationtokenId = stsApplicationtokenId;
        this.uasAppCredentialXml = uasAppCredentialXml;
        this.appCredentialXml = appCredentialXml;
        if (uibUri == null || stsApplicationtokenId == null || uasAppCredentialXml == null || appCredentialXml == null) {
            log.error("{} initialized with null-values - will fail", CommandAuthenticateApplicationUIB.class.getSimpleName());
        }
    }

    @Override
    protected Response run() {
        log.trace("{} - stsApplicationtokenId={}, ", CommandAuthenticateApplicationUIB.class.getSimpleName(), stsApplicationtokenId);
        Client client = ClientBuilder.newClient();
        WebTarget uib = client.target(uibUri);
        WebTarget webResource = uib.path(stsApplicationtokenId).path(APPLICATION_AUTH_PATH);
        MultivaluedMap<String,String> formData = new MultivaluedHashMap<>(2);
        formData.add(UAS_APP_CREDENTIAL_XML, uasAppCredentialXml);
        formData.add(CommandAuthenticateApplicationUAS.APP_CREDENTIAL_XML, appCredentialXml);
        return webResource.request(MediaType.APPLICATION_FORM_URLENCODED)
                .header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML,UASCredentials.encode(uasAppCredentialXml))
                          .post(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED));
    }

    @Override
    protected Response getFallback() {
        log.warn("{} - timeout - uibUri={}", CommandAuthenticateApplicationUIB.class.getSimpleName(), uibUri);
        return null;
    }
}
