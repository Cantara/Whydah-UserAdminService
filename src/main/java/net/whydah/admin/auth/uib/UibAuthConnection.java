package net.whydah.admin.auth.uib;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.auth.UserLogonObservedActivity;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.security.UASCredentials;
import net.whydah.sso.user.helpers.UserXpathHelper;
import net.whydah.sso.util.SSLTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.valuereporter.activity.ObservedActivity;
import org.valuereporter.client.MonitorReporter;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.valuereporter.agent.MonitorReporter;
//import org.valuereporter.agent.activity.ObservedActivity;


/**
 * @author <a href="mailto:bard.lind@gmail.com">Bard Lind</a>
 */
@Component
public class UibAuthConnection {
    private static final Logger log = LoggerFactory.getLogger(UibAuthConnection.class);

    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    public static final int FORBIDDEN = 403;
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
    private static final int STATUS_NOT_ACCEPTABLE = 406;
    private final UASCredentials uasCredentials;

    private WebTarget uib;
    private final String myuibUrl;

    @Autowired
    public UibAuthConnection(@Value("${useridentitybackend}") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        SSLTool.disableCertificateValidation();
        this.myuibUrl = uibUrl;
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
    }

    public String logonUser(String appTokenId, String userCredentialsXml) {
        log.trace("logonUser(userAdminServiceTokenId: {}, userCredentialsXml:{})", appTokenId, userCredentialsXml);
        long startTime = System.currentTimeMillis();
        Client client = ClientBuilder.newClient();
        uib = client.target(myuibUrl);
        WebTarget logonUserResource = uib.path(appTokenId).path("authenticate/user");
        Response response = logonUserResource.request(MediaType.APPLICATION_XML).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(userCredentialsXml, MediaType.APPLICATION_XML_TYPE));
        int statusCode = response.getStatus();
        String userXml = null;
        switch (statusCode) {
            case STATUS_OK:
                userXml = response.readEntity(String.class);
                break;
            case STATUS_BAD_REQUEST:
                //log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                log.error("logonUser failed (STATUS_BAD_REQUEST). url={}, body={}, Response from UIB: {}: {}",
                        logonUserResource.getUri(), userCredentialsXml, response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + response.toString() + ",  Status code " + response.getStatus());
            case FORBIDDEN:
                //log.trace("LogonUser failed, not allowed from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                log.warn("logonUser failed (FORBIDDEN). url={}, body={}, Response from UIB: {}: {}",
                        logonUserResource.getUri(), userCredentialsXml, response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("LogonUser request not allowed.");
            default:
                log.error("logonUser failed. url={}, body={}, Response from UIB: {}: {}",
                        logonUserResource.getUri(), userCredentialsXml, response.getStatus(), response.readEntity(String.class));
                throw new RuntimeException("LogonUser failed. Status code " + response.getStatus());
        }
        String userid = UserXpathHelper.getUserIdFromUserIdentityXml(userXml);
        ObservedActivity observedActivity = new UserLogonObservedActivity(userid);
        MonitorReporter.reportActivity(observedActivity);
        log.trace("Adding activity to cache {}", observedActivity);
        return userXml;
    }

    public String resetPassword(String userAdminServiceTokenId, String username) throws AppException {
        Client client = ClientBuilder.newClient();
        uib = client.target(myuibUrl);
        WebTarget resetPasswordResource = uib.path("password").path(userAdminServiceTokenId).path("reset/username").path(username);
        Response response = resetPasswordResource.request(MediaType.APPLICATION_XML).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity("",MediaType.APPLICATION_XML_TYPE));
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.info("Reset password request ok for username {}", username);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("BadRequest for resetPassword " + response.toString() + ",  Status code " + response.getStatus());
            case STATUS_NOT_ACCEPTABLE:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw AppExceptionCode.MISC_NOT_ACCEPTABLE_9991;
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("ResetPassword failed. Status code " + response.getStatus());
        }
        return output;
    }

    public String resetPassword_new(String userAdminServiceTokenId, String uid) throws AppException {
        // /user/{uid}/reset_password
        Client client = ClientBuilder.newClient();
        uib = client.target(myuibUrl);
        WebTarget resetPasswordResource = uib.path(userAdminServiceTokenId).path("user").path(uid).path("reset_password");
        Response response = resetPasswordResource.request(MediaType.APPLICATION_XML).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity("",MediaType.APPLICATION_XML_TYPE));
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.info("Reset password request ok for uid {}", uid);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("BadRequest for resetPassword " + response.toString() + ",  Status code " + response.getStatus());
            case STATUS_NOT_ACCEPTABLE:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw AppExceptionCode.MISC_NOT_ACCEPTABLE_9991;
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("ResetPassword failed. Status code " + response.getStatus());
        }
        return output;
    }

    public String setPasswordByToken(String userAdminServiceTokenId, String username,String passwordToken,String password) throws AppException {
        Client client = ClientBuilder.newClient();
        uib = client.target(myuibUrl);
        WebTarget resetPasswordResource = uib.path("password").path(userAdminServiceTokenId).path("reset/username").path(username).path("newpassword").path(passwordToken);
        Response response = resetPasswordResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity("{\"newpassword\":\"" + password + "\"}", MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        String output = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.info("Reset password request ok for username {}", username);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new BadRequestException("BadRequest for resetPassword " + response.toString() + ",  Status code " + response.getStatus());
            case STATUS_NOT_ACCEPTABLE:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw AppExceptionCode.MISC_NOT_ACCEPTABLE_9991;
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), output);
                throw new AuthenticationFailedException("ResetPassword failed. Status code " + response.getStatus());
        }
        return output;
    }

    public String getUIBUri() {
        return myuibUrl;
    }
}