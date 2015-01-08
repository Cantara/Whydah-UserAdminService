package net.whydah.admin.auth;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.config.AppConfig;
import org.glassfish.jersey.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Component
public class UibAuthConnection {
    private static final Logger log = LoggerFactory.getLogger(UibAuthConnection.class);

    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    public static final int FORBIDDEN = 403;
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();

    private final WebTarget uib;

    @Autowired
    public UibAuthConnection(AppConfig appConfig) {
        Client client = ClientBuilder.newClient();
        String uibUrl = appConfig.getProperty("useridentitybackend");
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    public String logonUser(String userAdminServiceTokenId, String userCredentialsXml) {
        WebTarget logonUserResource = uib.path("/" + userAdminServiceTokenId).path("authenticate/user");
        Response response = logonUserResource.request(MediaType.APPLICATION_XML).get();
        int statusCode = response.getStatus();
        String userXml = null;
        switch (statusCode) {
            case STATUS_OK:
                userXml = response.readEntity(String.class);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + response.toString() + ",  Status code " + response.getStatus());
            case FORBIDDEN:
                log.trace("LogonUser failed, not allowed from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("LogonUser request not allowed.");
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new RuntimeException("LogonUser failed. Status code " + response.getStatus());
        }
        return userXml;
    }

    public String resetPassword(String userAdminServiceTokenId, String username) {
        WebTarget resetPasswordResource = uib.path("password").path(userAdminServiceTokenId).path("reset/username").path(username);
        Response response = resetPasswordResource.request(MediaType.APPLICATION_XML).post(Entity.entity("",MediaType.APPLICATION_XML_TYPE));
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.info("Reset password request ok for username {}", username);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("BadRequest for resetPassword " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("ResetPassword failed. Status code " + response.getStatus());
        }
        return output;
    }


    public String setPasswordByToken(String userAdminServiceTokenId, String username,String passwordToken,String password) {
        WebTarget resetPasswordResource = uib.path("password").path(userAdminServiceTokenId).path("reset/username").path(username).path("newpassword").path(passwordToken);

        Response response = resetPasswordResource.request(MediaType.APPLICATION_XML).post(Entity.entity("{\"newpassword\":\"" + password + "\"}",MediaType.MULTIPART_FORM_DATA));
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.info("Reset password request ok for username {}", username);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("BadRequest for resetPassword " + response.toString() + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("ResetPassword failed. Status code " + response.getStatus());
        }
        return output;
    }
}
