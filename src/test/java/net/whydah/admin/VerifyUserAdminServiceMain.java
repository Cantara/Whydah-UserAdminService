package net.whydah.admin;

import com.jayway.restassured.RestAssured;
import net.whydah.admin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Verify that every interface of UserAdminService respond in a propper way.
 * See https://code.google.com/p/rest-assured/wiki/GettingStarted
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class VerifyUserAdminServiceMain {
    private static final Logger log = LoggerFactory.getLogger(VerifyUserAdminServiceMain.class);

    private WebTarget userAdminService;

    public VerifyUserAdminServiceMain() {
        Client client = ClientBuilder.newClient();
        AppConfig appConfig = new AppConfig();
        String uibUrl = appConfig.getProperty("myuri");
        log.info("Connection to UserAdminService on {}" , uibUrl);
        userAdminService = client.target(uibUrl);
    }

    public static void main(String[] args) {
        System.setProperty("IAM_MODE", "DEV");
        VerifyUserAdminServiceMain verificator = new VerifyUserAdminServiceMain();
        //verificator.findUserByQuery();
        verificator.findUserByQueryRestAssured();
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
}
