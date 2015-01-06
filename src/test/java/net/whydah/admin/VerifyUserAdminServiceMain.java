package net.whydah.admin;

import com.jayway.restassured.RestAssured;
import net.whydah.admin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Verify that every interface of UserAdminService respond in a propper way.
 * See https://code.google.com/p/rest-assured/wiki/GettingStarted
 *
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class VerifyUserAdminServiceMain {
    private static final Logger log = LoggerFactory.getLogger(VerifyUserAdminServiceMain.class);
    private static final String UIB_USER_AUTHENTICATION_PATH = "/authenticate/user";
    private static final String UIB_CREATE_AND_LOGON_OPERATION = "createandlogon";
    private final String uasUrl;

    private WebTarget userAdminService;

    public VerifyUserAdminServiceMain() {
        Client client = ClientBuilder.newClient();
        AppConfig appConfig = new AppConfig();
        uasUrl = appConfig.getProperty("myuri");
        log.info("Connection to UserAdministrationService on {}", uasUrl);
        userAdminService = client.target(uasUrl);
    }

    public static void main(String[] args) {
        System.setProperty("IAM_MODE", "DEV");
        VerifyUserAdminServiceMain verificator = new VerifyUserAdminServiceMain();
      //  verificator.logonUser();
        verificator.stsUserInterface();
        //verificator.findUserByQuery();
        //verificator.findUserByQueryRestAssured();
        //verificator.stsUserInterface();
    }

    public void findUserByQuery() {
        String userAdminServiceTokenId = "1";
        String adminUserTokenId = "2";
        String userId = "useradmin";
        WebTarget webResource = userAdminService.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(userId);
        Response response = webResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("StatusCode {}", statusCode);
    }


    public void findUserByQueryRestAssured() {
        //"users/find/"+query
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9992;
        RestAssured.urlEncodingEnabled = false;
        //String json = given().accept("application/json").get("/useradminservice/1/2/user/useradmin").asString();
        given().
                when().
                accept(MediaType.APPLICATION_JSON).
                get("/useradminservice/1/2/user/useradmin").
                then().
                contentType(MediaType.APPLICATION_JSON).
                statusCode(200).
                body("identity.username", equalTo("admin"));
        log.info("findUser by Json OK.");

    }

    public void logonUser() {
        String userAdminServiceTokenId = "1";
        WebTarget webResource = userAdminService.path("/" + userAdminServiceTokenId + "/auth/logon");
        Response response = webResource.request(MediaType.APPLICATION_XML).post(Entity.entity(userCredentialXml(), MediaType.APPLICATION_XML_TYPE));
        int statusCode = response.getStatus();
        log.info("logonUser,StatusCode {}", statusCode);
        assertEquals("Could not logon user via UserAdminService", 200, statusCode);
    }

    public void createAndLogonUser() {
        String userAdminServiceTokenId = "1";
        String createAndLogonPath = "/createlogon/user"; //createandlogon
        WebTarget webResource = userAdminService.path("/" + userAdminServiceTokenId +"/111").path(createAndLogonPath);
        String userId = "createValidTest-" + System.currentTimeMillis();
        String userName = userId;
        String fbUserXml = fbUserXml(userId, userName);
        Response response = webResource.request(MediaType.APPLICATION_XML).post(Entity.entity(fbUserXml, MediaType.APPLICATION_XML));
        int statusCode = response.getStatus();
        log.info("createAndLogonUser url {}, StatusCode {}",webResource.getUri(), statusCode);
        assertEquals("Could not  crated and logon user via UserAdminService", 200,statusCode);
    }
    /**
     * FIXME implement Interfaces and proxy methods supporting SecurityTokenService
     * <p/>
     * FIXME  Pri 1.
     */
    public void stsUserInterface() {

        //1. Logon existing user via xml
       //FIXME logonUser();
        //2. Create and Logon new user via xml
        createAndLogonUser();
        //- WebResource webResource = uibResource.path(applicationTokenId).path(UIB_USER_AUTHENTICATION_PATH);
        //- ClientResponse response = webResource.type(UIB_MediaType.APPLICATION_XML).post(ClientResponse.class, userCredentialXml);
        //createAndLogonUser
        //- WebResource webResource = uibResource.path(applicationtokenid).path(UIB_USER_AUTHENTICATION_PATH).path(UIB_CREATE_AND_LOGON_OPERATION);
        //- logger.debug("createAndLogonUser - Calling createandlogon " + webResource.toString());
        //- ClientResponse response = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, fbUserXml);


    }

    private String fbUserXml(String userId, String userName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n" +
                "     <user>\n" +
                "        <params>\n" +
                "            <fbAccessToken>accessMe1234567</fbAccessToken>\n" +
                "            <userId>" + userId + "</userId>\n" +
                "            <firstName>validFirstName</firstName>\n" +
                "            <lastName>validLastName</lastName>\n" +
                "            <username>" + userName + "</username>\n" +
                "            <gender>male</gender>\n" +
                "            <email>" + userName +"@example.com</email>\n" +
                "            <birthday></birthday>\n" +
                "            <hometown>Oslo</hometown>\n" +
                "        </params> \n" +
                "    </user>";
    }

    private String userCredentialXml() {
        return "<usercredential>\n" +
                "   <params>\n" +
                "      <username>admin</username>\n" +
                "      <password>admin</password>\n" +
                "   </params>\n" +
                "</usercredential>";
    }

    /**
     * FIXME implement Interfaces and proxy methods supporting SecurityTokenService
     * <p/>
     * FIXME  Pri 9.  (Maybe STS should havdle the applicationsessions, just querying UIB for the application config
     */
    public void stsApplicationInterface() {
        //logonApplication
        //-  WebResource webResource = restClient.resource(useridbackendUri).path("logon");
        //- ClientResponse response = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, applicationCredential.toXML());


    }

    /**
     * FIXME implement Interfaces and proxy methods supporting SecurityTokenService
     * <p/>
     * FIXME  Pri 4.  (Maybe STS should havdle the applicationsessions, just querying UIB for the application config
     */
    public void appUserInterface() {
        //userSearch
        //


    }

    /**
     * FIXME implement Interfaces and proxy methods supporting UserAdminWebapp
     * <p/>
     * <p/>
     * FIXME  Pri 3.
     */
    public void userAdminWebAppInterface() {
        //getApplications
        //- String url = getUibUrl(apptokenid, usertokenid, "applications");
        // FIXME This API need to be reworked to new DomainModel for Applications and full REST methods for applications
        //       Exiting API is just a temporary bolt-on for the missing API


    }


    /**
     * FIXME implement Interfaces and proxy methods supporting UserAdminWebapp
     * <p/>
     * <p/>
     * FIXME  Pri 2.
     */
    public void userAdminWebUserInterface() {
        //resetPassword
        //- String url = uibUrl + "password/" + apptokenid +"/reset/username/" + username;
        //putUserRole
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid+"/role/"+roleId);
        //deleteUserRole
        //-  String url = getUibUrl(apptokenid, usertokenid, "user/"+uid+"/role/"+roleId);
        //postUserRole
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid+"/role/");
        //getUserRoles
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid+"/roles");
        //postUser
        //- String url = getUibUrl(apptokenid, usertokenid, "user/");
        //putUser
        //- String url = getUibUrl(apptokenid, usertokenid, "user/" + uid);
        //deleteUser
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //getUserAggregate
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //getUser
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //findUsers
        //- String url = getUibUrl(apptokenid, usertokenid, "users/find/"+query);
    }
}
