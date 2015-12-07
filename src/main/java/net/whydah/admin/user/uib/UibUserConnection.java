package net.whydah.admin.user.uib;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.security.UASCredentials;
import net.whydah.admin.user.ConflictExeption;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
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
 * Created by baardl on 17.04.14.
 */
@Component
public class UibUserConnection {
    private static final Logger log = LoggerFactory.getLogger(UibUserConnection.class);
    private static final int STATUS_BAD_REQUEST = 400; //Response.Status.BAD_REQUEST.getStatusCode();
    private static final int STATUS_OK = 200; //Response.Status.OK.getStatusCode();
    private static final int STATUS_FORBIDDEN = 403;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_CONFLICT = 409;
    private static final int STATUS_NO_CONTENT = 204;


    private final WebTarget uib;
    private final UASCredentials uasCredentials;

    @Autowired
    @Configure
    public UibUserConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        Client client = ClientBuilder.newClient();
//        URI useridbackendUri = URI.create(appConfig.getProperty("userIdentityBackendUri"));
        // uib = client.target(userIdentityBackendUri);
        log.info("Connection to UserIdentityBackend on {}" , uibUrl);
        uib = client.target(uibUrl);
    }

    public UserIdentity createUser(String userAdminServiceTokenId, String userTokenId, String userIdentityJson) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("user");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(userIdentityJson, MediaType.APPLICATION_JSON));

        UserIdentity userIdentity;
        //UserAggregateRepresentation userAggregateRepresentation = null;
        // userIdentityJson = "{\"username\":\"per\",\"firstName\":\"per\",\"lastName\":\"per\",\"email\":\"per.per@example.com\",\"cellPhone\":\"123456789\",\"personRef\":\"ref\"}";
        String userJson = response.readEntity(String.class);
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("createUser-Response from UIB {}", userJson);
                userIdentity = UserIdentityMapper.fromUserIdentityJson(userJson);
                break;
            case STATUS_CREATED:
                log.trace("createUser-userCreated {}", userJson);
                userIdentity = UserIdentityMapper.fromUserIdentityJson(userJson);
                break;
            case STATUS_CONFLICT:
                log.info("Duplicate creation of user attempted on {}", userIdentityJson);
                throw new ConflictExeption("DuplicateCreateAttempted on " + userIdentityJson);
            case STATUS_BAD_REQUEST:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
                throw new BadRequestException("BadRequest for Json " + userIdentityJson + ",  Status code " + response.getStatus());
            default:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return userIdentity;
    }

    public UserAggregate addUserAgregate(String userAdminServiceTokenId, String userTokenId, String userAggregateJson) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(userAggregateJson, MediaType.APPLICATION_JSON));

        UserAggregate userAggregate;
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("Response from UIB {}", response.readEntity(String.class));
                userAggregate = UserAggregateMapper.fromJson(userAggregateJson);
                break;
            case STATUS_BAD_REQUEST:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new BadRequestException("BadRequest for Json " + userAggregateJson + ",  Status code " + response.getStatus());
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return userAggregate;
    }


    public Response updateUserIdentity(String userAdminServiceTokenId, String userTokenId, String uid, String userIdentityJson) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("user").path(uid);
        Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(userIdentityJson, MediaType.APPLICATION_JSON));

        Response.ResponseBuilder responseBuilder = Response.status(responseFromUib.getStatusInfo());
        int statusCode = responseFromUib.getStatusInfo().getStatusCode();
        String reasonPhrase = responseFromUib.getStatusInfo().getReasonPhrase();
        String responseBody = null;
        if (responseFromUib.hasEntity()) {
            responseBody = responseFromUib.readEntity(String.class);
            responseBuilder = responseBuilder.entity(responseBody);
        }
        switch (responseFromUib.getStatusInfo().getFamily()) {
            case SUCCESSFUL:
                log.trace("updateUserIdentity was successful. Response from UIB: {} {}, body={}", statusCode, reasonPhrase, responseBody);
                break;
            default:
                log.warn("updateUserIdentity was unsuccessful. Response from UIB: {} {}, body={}", statusCode, reasonPhrase, responseBody);
        }
        return responseBuilder.build();
    }


    public boolean changePassword(String userAdminServiceTokenId, String adminUserTokenId, String userName, String password) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(userName).path("changepassword");
        boolean updatedOk = false;
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(password, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        String passwordJson = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("changePassword-Response from UIB {}", passwordJson);
                updatedOk = true;
                break;
            case STATUS_FORBIDDEN:
                log.error("changePassword-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), passwordJson);
                break;
            default:
                log.error("changePassword-Response from UIB: {}: {}", response.getStatus(), passwordJson);
                throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
        }
        return updatedOk;
    }

    public UserApplicationRoleEntry addRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        String roleJson = response.readEntity(String.class);
        UserApplicationRoleEntry role = null;
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_OK:
                log.trace("addRole-Response from UIB {}", roleJson);
                role = UserRoleMapper.fromJson(roleJson);
                break;
            case STATUS_CREATED:
                log.trace("addRole-roleCreated {}", roleJson);
                role = UserRoleMapper.fromJson(roleJson);
                break;
            case STATUS_CONFLICT:
                log.info("Duplicate creation of role attempted on {}", roleJson);
                throw new ConflictExeption("DuplicateCreateAttempted on " + roleJson);
            case STATUS_BAD_REQUEST:
                log.error("addRole-Response from UIB: {}: {}",statusCode, roleJson);
                throw new BadRequestException("BadRequest for Json " + roleJson + ",  Status code " + statusCode);
            default:
                log.error("addRole-Response from UIB: {}: {}", statusCode, roleJson);
                throw new AuthenticationFailedException("Authentication failed. Status code " + statusCode);
        }
        return role;
    }

    public void deleteUserRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, String userRoleId) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role").path(userRoleId);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_NO_CONTENT:
                log.trace("deleteUserRole-Response from UIB {}", userRoleId);
                break;
            case STATUS_BAD_REQUEST:
                log.error("deleteUserRole-Response from UIB: {}: {}",statusCode, userRoleId);
                throw new BadRequestException("deleteUserRole for userRoleId " + userRoleId + ",  Status code " + statusCode);
            default:
                log.error("deleteUserRole-Response from UIB: {}: {}", statusCode, userRoleId);
                throw new RuntimeException("DeleteUserRole failed. Status code " + statusCode);
        }

    }



    public UserAggregate addPropertyOrRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry userPropertyAndRole) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role");
        UserAggregate updatedUser = null;
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(userPropertyAndRole.toJson(), MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        switch (statusCode) {
            case STATUS_OK:
                log.trace("addPropertyOrRole-Response from UIB {}", response.readEntity(String.class));
                updatedUser = UserAggregateMapper.fromJson(response.readEntity(String.class));
                break;
            case STATUS_FORBIDDEN:
                log.error("addPropertyOrRole-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), response.readEntity(String.class));
                break;
            default:
                log.error("addPropertyOrRole-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                throw new AuthenticationFailedException("addPropertyOrRole failed. Status code " + response.getStatus());
        }
        return updatedUser;
    }


    //TODO Clean up exception/failure handling
    public UserIdentity getUserIdentity(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        String responseBody = response.readEntity(String.class);
        switch (response.getStatus()) {
            case STATUS_OK:
                log.trace("getUserIdentity-Response from Uib {}", responseBody);
                UserIdentity userIdentity = UserIdentityMapper.fromUserIdentityJson(responseBody);
                return userIdentity;
            case STATUS_FORBIDDEN:
                log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                return null;
            default:
                log.error("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
    }


    public UserAggregate getUserAggregateByUid(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        UserAggregate userAggregate = null;
        UserAggregate userAggregateRepresentation;
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        int statusCode = response.getStatus();
        String responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("getUserAggregateByUid-Response from Uib {}", responseBody);
                userAggregate = UserAggregateMapper.fromJson(responseBody);
                break;
            case STATUS_FORBIDDEN:
                log.error("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                break;
            default:
                log.error("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
        return userAggregate;
    }

    public String getUserAggregateByUidAsJson(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        int statusCode = response.getStatus();
        String responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("getUserAggregateByUid-Response from Uib {}", responseBody);
                return responseBody;
            case STATUS_FORBIDDEN:
                log.error("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                break;
            default:
                log.error("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
        return null;
    }


    public String getRolesAsJson(String userAdminServiceTokenId, String userTokenId, String uid) {
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("/user").path(uid).path("roles");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return findResponseBody("getRolesAsJson", response);
    }

    private String findResponseBody(String methodName, Response response) {
        String responseBody;
        int statusCode = response.getStatus();
        responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("{}-Response from UIB {}", methodName,responseBody);
                break;
            case STATUS_FORBIDDEN:
                log.error("{}-Not allowed from UIB: {}: {} ", methodName,response.getStatus(), responseBody);
                responseBody = null;
                break;
            default:
                log.error("{}-Response from UIB: {}: {}", methodName,response.getStatus(), responseBody);
                throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
        }
        return responseBody;
    }


    public void deleteUser(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_NO_CONTENT:
                log.trace("deleteUser-Response from UIB uid={}", uid);
                break;
            case STATUS_BAD_REQUEST:
                log.error("deleteUser-Response from UIB: {}: uid={}", statusCode, uid);
                throw new BadRequestException("deleteUserRole for uid=" + uid + ",  Status code " + statusCode);
            default:
                log.error("deleteUser-Response from UIB: {}, uid=", statusCode, uid);
                throw new RuntimeException("DeleteUser failed. Status code " + statusCode);
        }
    }
}
