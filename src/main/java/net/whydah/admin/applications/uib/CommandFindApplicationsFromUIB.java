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
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;


public class CommandFindApplicationsFromUIB extends HystrixCommand<Response> {
    private static final String APPLICATIONS_FIND_PATH = "applications/find";  //   uib.path(applicationTokenId).path("applications");
    private static final Logger log = getLogger(CommandFindApplicationsFromUIB.class);
    private String uibUri;
    private String stsApplicationtokenId;
    //private String userTokenId;
    private String uasAppCredentialXml;
    private String query;

    public CommandFindApplicationsFromUIB(String uibUri, String stsApplicationtokenId, String uasAppCredentialXml, String query) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("UASUserAdminGroup")).
                andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3000)));

        this.uibUri = uibUri;
        this.stsApplicationtokenId = stsApplicationtokenId;
        this.uasAppCredentialXml = uasAppCredentialXml;
        //this.userTokenId = userTokenId;
        this.query = query;
        if (uibUri == null || stsApplicationtokenId == null || uasAppCredentialXml == null) {
            log.error("{} initialized with null-values - will fail", CommandFindApplicationsFromUIB.class.getSimpleName());
        }
    }

    @Override
    protected Response run() {
        log.info("{} - stsApplicationtokenId={}, userTokenId:{}, query:{}", CommandFindApplicationsFromUIB.class.getSimpleName(), stsApplicationtokenId, query);
        Client client = ClientBuilder.newClient();
        WebTarget uib = client.target(uibUri);
        WebTarget webResource = uib.path(stsApplicationtokenId).path("find").path("applications").path(query);
//        WebTarget webResource = uib.path(stsApplicationtokenId).path(userTokenId).path("find").path("applications").path(query);
        return webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasAppCredentialXml).get();


    }

}
