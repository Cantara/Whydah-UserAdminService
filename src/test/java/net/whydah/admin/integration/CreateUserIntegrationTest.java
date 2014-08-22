package net.whydah.admin.integration;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class CreateUserIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(CreateUserIntegrationTest.class);
    private static final String RESPONSE_TYPES = "application/xml";

    private final Client c;
    private WebTarget target;

    private final String SECURITY_TOKEN_SERVICE_URL= "http://ec2-54-77-145-230.eu-west-1.compute.amazonaws.com:9998/tokenservice";
    private final String MEDIA_TYPE = "application/x-www-form-urlencoded";

    public CreateUserIntegrationTest() {
        c = ClientBuilder.newClient();
    }


    /**
     * Authorization info is found in:
     * https://github.com/altran/Whydah-UserIdentityBackend/blob/master/src/main/resources/prodInitData/users.csv
     * https://github.com/altran/Whydah-UserIdentityBackend/blob/master/src/main/resources/prodInitData/applications.csv
     *
     * @param args
     */
    public static void main(String[] args) {
        CreateUserIntegrationTest userTest = new CreateUserIntegrationTest();
        String result = userTest.logonApplication();
        log.info("Logon Application: {} ", result );
        // FIXME  get the tokenID :)
        String applicationToken="d41d8cd98f00b204e9800998ecf8427e";
        String userResult = userTest.logonUserAdmin(applicationToken);
        log.info("Logon User {}", userResult);
    }

    public String logonApplication() {
        target = uasPath().path("/logon");
        Response response = target.request(MediaType.APPLICATION_XML).post(Entity.entity(uasCredentialXml(),MediaType.APPLICATION_FORM_URLENCODED));
        String responseBody = readResponse("UserAdminService", response);

        return responseBody;
    }

    public String logonUserAdmin(String appTokenID) {
        //     @Path("/{applicationtokenid}/usertoken")
        //    public Response getUserToken(@PathParam("applicationtokenid") String applicationtokenid,
        //        @FormParam("apptoken") String appTokenXml,
        //        @FormParam("usercredential") String userCredentialXml) {

        target = uasPath().path("/token/"+appTokenID+"/usertoken");
        MultivaluedMap<String,String> formData = new MultivaluedMapImpl();
        formData.add("apptoken", uasCredentialXml());
        formData.add("ticket", userAdminCredentialXml());

        Response response = target.request(MediaType.APPLICATION_XML).post(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED));
        String responseBody = readResponse("UserAdmin", response);

        return responseBody;
    }


    private String uasCredentialXml() {
        return "<applicationcredential>\n" +
                "      <params>\n" +
                "         <applicationID>UserAdminService</applicationID>\n" +
                "         <applicationSecret>9ju592A4t8dzz8mz7a5QQJ7P</applicationSecret>\n" +
                "      </params>\n" +
                "   </applicationcredential>";
    }
    private String userAdminCredentialXml() {
        return "<usercredential>\n" +
                "   <params>\n" +
                "      <username>admin</username>\n" +
                "      <password>whydahadmin</password>\n" +
                "   </params>\n" +
                "</usercredential>";
    }


    private WebTarget uasPath() {
        return c.target(SECURITY_TOKEN_SERVICE_URL);
    }

    private Invocation.Builder request() {
        return target.request(RESPONSE_TYPES);
    }

    private String readResponse(String ref, Response response) {
        String responseBody = response.readEntity(String.class);
        boolean success = handleResponseStatus(response, ref, responseBody);
        if (!success) {
            throw new RuntimeException("XML post failed. Reference: " + ref +"\n  Response:[ " + responseBody +"]");
        }
        return responseBody;
    }

    private boolean handleResponseStatus(Response response, String request, String responseBody) {
        switch(response.getStatusInfo().getFamily()) {
            case SUCCESSFUL:
                return true;
            case CLIENT_ERROR:
            case INFORMATIONAL:
            case OTHER:
            case REDIRECTION:
            case SERVER_ERROR:
            default:
                log.info("Request failed, Response-Status-Family {}, ResponseStatus={} {}, jsonRequest={}, ResponseEntity(body)={}", response.getStatusInfo().getFamily(),response.getStatus(), response.getStatusInfo().getReasonPhrase(), request, responseBody);
        }
        return false;
    }


}
