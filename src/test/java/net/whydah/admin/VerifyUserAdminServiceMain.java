package net.whydah.admin;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertTrue;
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
    private static final String UIB_USERNAME = "useradmin";
    private static final String UIB_PASSWORD = "admin";
    private static final String UAS_APPLICATION_ID = "12";
    private static final String UAS_APPLICATION_SECRET = "9ju592A4t8dzz8mz7a5QQJ7Px";
    private final String userAdminServiceUri = "http://localhost:9992/useradminservice";
    private final String userTokenServiceUri = "http://localhost:9998/tokenservice";

    public VerifyUserAdminServiceMain() {
        Client client = ClientBuilder.newClient();
        //uasUrl = appConfig.getProperty("myuri");
        uasUrl = "http://localhost:9992/useradminservice";
        log.info("Connection to UserAdministrationService on {}", uasUrl);
        userAdminService = client.target(uasUrl);
    }

    public static void main(String[] args) {
        System.setProperty("IAM_MODE", "DEV");
        VerifyUserAdminServiceMain verificator = new VerifyUserAdminServiceMain();
        verificator.logonUser();
        //verificator.stsUserInterface();
        verificator.userAdminWebUserInterface();
        verificator.userAdminWebApplicationInterface();
    }


    public void logonUser() {
//        String uasAppToken = WhydahUtil.logOnApplication(userTokenServiceUri, UAS_APPLICATION_ID, UAS_APPLICATION_SECRET);
//        log.debug("usaAppToken {}", uasAppToken);
//        String adminUserToken = WhydahUtil.logOnApplicationAndUser(userTokenServiceUri,UAS_APPLICATION_ID,UAS_APPLICATION_SECRET,UIB_USERNAME,UIB_PASSWORD);
        String userAdminServiceTokenId = "c20ad4d76fe97759aa27a0c99bff6710";
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
                "      <username>"+UIB_PASSWORD+"</username>\n" +
                "      <password>"+UIB_PASSWORD +"</password>\n" +
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
     * Implement Interfaces and proxy methods supporting UserAdminWebapp
     */
    public void userAdminWebUserInterface() {


        //UserRoles
        getUserRoles();
        String roleId = addUserRole();
        deleteUserRole(roleId);
        //User
        String userId = addUser();
        deleteUser(userId);
        getUserAggregate();

        findUsers();
        resetPassword(); //This one will probably give an error, when mail-sender might not be configured in DEV mode.

        //IGNORED
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //- String url = getUibUrl(apptokenid, usertokenid, "user/");
        //putUserRole - ignored now not in use?
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid+"/role/"+roleId);
        //putUser - ignored now not in use?
        //- String url = getUibUrl(apptokenid, usertokenid, "user/" + uid);
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);
        //getUserIdentity
        //- String url = getUibUrl(apptokenid, usertokenid, "user/"+uid);

    }


    private void getUserAggregate() {
        WebTarget userResource = buildUserPath().path(USER_ID);
        log.info("getUserAggregateByUid by url {}, ", userResource.getUri().toString());
        Response response = userResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("getUserAggregateByUid ,StatusCode {}", statusCode);
        assertEquals("Could not getUserAggregateByUid via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);

        assertNotNull(output);
        assertTrue(output.contains(USER_ID));
    }

    private String addUser() {

        WebTarget userResource = buildUserPath();
        String userJson = UserIdentityMapper.toJson(buildStubUser());
        log.info("AddUser by url {}, ", userResource.getUri().toString());
        Response response = userResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(userJson, MediaType.APPLICATION_JSON));
        String responseText = response.readEntity(String.class);
        log.debug("Response: {}", responseText);
        int statusCode = response.getStatus();
        log.info("addUserRole ,StatusCode {}", statusCode);
        assertEquals("Could not add user-role via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        UserIdentity createdUserIdentity = UserIdentityMapper.fromUserIdentityJson(output);
        String userId = createdUserIdentity.getUid();
        assertNotNull(userId);
        return userId;

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

    private WebTarget buildUserPath() {
        return userAdminService.path(USER_ADMIN_SERVICE_TOKEN_ID).path(USER_TOKEN_ID).path("user/");
    }

    private WebTarget buildUsersPath() {
        return userAdminService.path(USER_ADMIN_SERVICE_TOKEN_ID).path(USER_TOKEN_ID).path("users");
    }

    public String addUserRole() {

        UserApplicationRoleEntry role = buildStubUserRole();
        WebTarget userRolesResource = buildBasePath().path("role/");

        log.info("AddUserRole by url {}, ", userRolesResource.getUri().toString());
        Response response = userRolesResource.request(MediaType.APPLICATION_JSON).post(Entity.entity(role.toJson(), MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        log.info("addUserRole ,StatusCode {}", statusCode);
        assertEquals("Could not add user-role via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        UserApplicationRoleEntry createdRole = UserRoleMapper.fromJson(output);
        String roleId = createdRole.getId();
        assertNotNull(roleId);
        return roleId;

    }

    private UserIdentity buildStubUser() {
        String firstName = "firstName-" + System.currentTimeMillis();
        String email = firstName + "@example.com";
        UserIdentity userIdentity = new UserIdentity(email, firstName, "testlastName", "test-personRef", email,"+4793333697");
        return userIdentity;
    }

    private UserApplicationRoleEntry buildStubUserRole() {
        String roleName = "testRole-" + System.currentTimeMillis();
        UserApplicationRoleEntry role = new UserApplicationRoleEntry();
        role.setApplicationId("12");
        role.setApplicationName("UserAdminService");
        role.setOrgName("Verification");
        role.setRoleName(roleName);
        role.setRoleValue("30");
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


    /*
    Find users... UAWA - search for users using query and return a list of user aggregates
     */
    public void findUsers() {
        String query = "test";
        WebTarget findResource = buildUsersPath().path("find").path(query);

        log.info("findUsers by url {}, ", findResource.getUri().toString());
        Response response = findResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("findUsers ,StatusCode {}", statusCode);
        assertEquals("Could find via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        assertTrue(output.contains(USER_ID));
    }

    /*
    Search users... 3Part application directory search  using query and return a list of user identities
     */
    public void searchUsers() {
        String query = "test";
        WebTarget findResource = buildUsersPath().path("search").path(query);

        log.info("searchUsers by url {}, ", findResource.getUri().toString());
        Response response = findResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("searchUsers ,StatusCode {}", statusCode);
        assertEquals("Could search via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        assertTrue(output.contains(USER_ID));
    }

    /*
    Application(s) list and edit applications.
     */
    private void userAdminWebApplicationInterface() {
        String query = "test";
        WebTarget findResource = userAdminService.path(USER_ADMIN_SERVICE_TOKEN_ID).path(USER_TOKEN_ID).path("applications");

        log.info("listAllApplications by url {}, ", findResource.getUri().toString());
        Response response = findResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("listAllApplications ,StatusCode {}", statusCode);
        assertEquals("listAllApplications via UserAdminService", 200, statusCode);
        String output = response.readEntity(String.class);
        assertTrue(output.contains("UserAdminService"));

    }


    /*
    Reset Password
     */
    public void resetPassword() {
        WebTarget resetPasswordResource = userAdminService.path(USER_ADMIN_SERVICE_TOKEN_ID).path("auth/password/reset/username").path("change.me@example.com");
        log.info("resetPassword by url {}, ", resetPasswordResource.getUri().toString());
        Response response = resetPasswordResource.request(MediaType.APPLICATION_JSON).get();
        int statusCode = response.getStatus();
        log.info("resetPassword ,StatusCode {}", statusCode);
        assertEquals("Could resetPassword via UserAdminService", 200, statusCode);
    }
}
