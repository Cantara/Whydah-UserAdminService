package net.whydah.admin;

import net.whydah.admin.config.AppConfig;
import net.whydah.admin.user.uib.RoleRepresentation;
import net.whydah.admin.user.uib.RoleRepresentationRequest;
import net.whydah.admin.user.uib.UserIdentity;
import net.whydah.admin.user.uib.UserIdentityRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Verify that every interface of UserAdminService respond in a propper way.
 * See https://code.google.com/p/rest-assured/wiki/GettingStarted
 *
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class VerifyUserAdminServiceMain {
    private static final Logger log = LoggerFactory.getLogger(VerifyUserAdminServiceMain.class);
    private static final String USER_AUTHENTICATION_PATH = "auth/logon/user";
    private static final String UIB_CREATE_AND_LOGON_OPERATION = "createandlogon";
    private final String uasUrl;

    private WebTarget userAdminService;
    public static final String USER_ADMIN_SERVICE_TOKEN_ID = "1";
    public static final String USER_TOKEN_ID = "1";
    public static final String USER_ID = "test.me@example.com";

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
        //verificator.logonUser();
        //verificator.stsUserInterface();
        verificator.userAdminWebUserInterface();
    }



    public void logonUser() {
        String userAdminServiceTokenId = "1";
        WebTarget userLogonResource = userAdminService.path("/" + userAdminServiceTokenId).path(USER_AUTHENTICATION_PATH);

        String credentials = userCredentialXml();
        log.info("Logging on the user by url {}, credentials {}", userLogonResource.getUri().toString(), credentials);
        Response response = userLogonResource.request(MediaType.APPLICATION_XML).post(Entity.entity(credentials, MediaType.APPLICATION_XML_TYPE));
        int statusCode = response.getStatus();
        log.info("logonUser ,StatusCode {}", statusCode);
        assertEquals("Could not logon user via UserAdminService", 200, statusCode);
    }

    public void createAndLogonUser() {
        String userAdminServiceTokenId = "1";
        String createAndLogonPath = "create_logon_facebook_user"; // "/createlogon/user"; //createandlogon
        WebTarget webResource = userAdminService.path("/" + userAdminServiceTokenId).path(createAndLogonPath);
        String userId = "createValidTest-" + System.currentTimeMillis();
        String userName = userId;
        String fbUserXml = fbUserXml(userId, userName);
        Response response = webResource.request(MediaType.APPLICATION_XML).post(Entity.entity(fbUserXml, MediaType.APPLICATION_XML));
        int statusCode = response.getStatus();
        log.info("createAndLogonUser url {}, StatusCode {}",webResource.getUri(), statusCode);
        assertEquals("Could not  crated and logon user via UserAdminService", 200,statusCode);
    }
    /**
     * Interfaces and proxy methods supporting SecurityTokenService
     */
    public void stsUserInterface() {

        //1. Logon existing user via xml
        logonUser();
        //2. Create and Logon new user via xml
        createAndLogonUser();

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
                "      <username>testMe</username>\n" +
                "      <password>testMe1234</password>\n" +
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
        //putUserRole - ignored now not in use?
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid+"/role/"+roleId);
        /*
        getUserRoles();
        String roleId = addUserRole();
        deleteUserRole(roleId);
        */
        //postUser
        String userId = addUser();
        deleteUser(userId);
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //- String url = getUibUrl(apptokenid, usertokenid, "user/");
        //putUser
        //- String url = getUibUrl(apptokenid, usertokenid, "user/" + uid);

        //getUserAggregate
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //getUser
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);

        //findUsers
        //- String url = getUibUrl(apptokenid, usertokenid, "users/find/"+query);
    }

    private String addUser() {

        WebTarget userResource = buildUserPath();
        String userJson = buildStubUser().toJson();
        log.info("AddUser by url {}, ", userResource.getUri().toString());
        Response response = userResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userJson, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        log.info("addUserRole ,StatusCode {}", statusCode);
        assertEquals("Could not add user-role via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        UserIdentity createdUserIdentity = UserIdentity.fromJson(output);
        String userId = createdUserIdentity.getUid();
        assertNotNull(userId);
        return userId;

    }

    private WebTarget buildUserPath() {
        return userAdminService.path(USER_ADMIN_SERVICE_TOKEN_ID).path(USER_TOKEN_ID).path("user/");
    }

    public void deleteUser(String userId) {
        WebTarget userRolesResource = buildUserPath().path(userId);

        log.info("deleteUser by url {}, ", userRolesResource.getUri().toString());
        Response response = userRolesResource.request(MediaType.APPLICATION_JSON).delete();
        int statusCode = response.getStatus();
        log.info("deleteUser ,StatusCode {}", statusCode);
        assertEquals("Could not delete user via UserAdminService", 204, statusCode);
    }

    public void getUserRoles() {
        WebTarget userRolesResource = buildBasePath().path("roles");

        log.info("GetUserRoles by url {}, ", userRolesResource.getUri().toString());
        Response response = userRolesResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("getUserRoles ,StatusCode {}", statusCode);
        assertEquals("Could not find user-roles via UserAdminService", 200, statusCode);

    }

    private WebTarget buildBasePath() {
        return userAdminService.path(USER_ADMIN_SERVICE_TOKEN_ID).path(USER_TOKEN_ID).path("user").path(USER_ID);
    }

    public String addUserRole() {

        RoleRepresentationRequest role = buildStubUserRole();
        WebTarget userRolesResource = buildBasePath().path("role/");

        log.info("AddUserRole by url {}, ", userRolesResource.getUri().toString());
        Response response = userRolesResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(role.toJson(), MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        log.info("addUserRole ,StatusCode {}", statusCode);
        assertEquals("Could not add user-role via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        RoleRepresentation createdRole = RoleRepresentation.fromJson(output);
        String roleId = createdRole.getId();
        assertNotNull(roleId);
        return roleId;

    }

    private UserIdentityRepresentation buildStubUser() {
        String firstName = "firstName-" + System.currentTimeMillis();
        String email = firstName + "@example.com";
        UserIdentityRepresentation userIdentity = new UserIdentityRepresentation(email, firstName, "testlastName", "test-personRef", email,"+4793333697");
        return userIdentity;
    }

    private RoleRepresentationRequest buildStubUserRole() {
        String roleName = "testRole-" + System.currentTimeMillis();
        RoleRepresentationRequest role = new RoleRepresentationRequest();
        role.setApplicationId("12");
        role.setApplicationName("UserAdminService");
        role.setOrganizationName("Verification");
        role.setApplicationRoleName(roleName);
        role.setApplicationRoleValue("30");
        return role;
    }

    public void deleteUserRole(String roleId) {
        WebTarget userRolesResource = buildBasePath().path("/role/").path(roleId);

        log.info("deleteUserRole by url {}, ", userRolesResource.getUri().toString());
        Response response = userRolesResource.request(MediaType.APPLICATION_JSON).delete();
        int statusCode = response.getStatus();
        log.info("deleteUserRole ,StatusCode {}", statusCode);
        assertEquals("Could not delete user-role via UserAdminService", 204, statusCode);
    }
}
