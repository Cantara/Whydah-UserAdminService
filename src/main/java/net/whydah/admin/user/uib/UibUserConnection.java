package net.whydah.admin.user.uib;

import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
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
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
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
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_NO_CONTENT = 204;
    private static final int STATUS_UNAUTHORIZED = 401;
    private static final int STATUS_FORBIDDEN = 403;
    private static final int STATUS_CONFLICT = 409;


    private  WebTarget uib;
    private final UASCredentials uasCredentials;
    private final String myUibUrl;

    @Autowired
    @Configure
    public UibUserConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        this.myUibUrl=uibUrl;
//        URI useridbackendUri = URI.create(appConfig.getProperty("userIdentityBackendUri"));
        // uib = client.target(userIdentityBackendUri);
    }

    public UserIdentity createUser(String userAdminServiceTokenId, String userTokenId, String userIdentityJson) throws AppException {
        uib = getWebTarget();
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
                userIdentity = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
                break;
            case STATUS_CREATED:
                log.trace("createUser-userCreated {}", userJson);
                userIdentity = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
                break;
            case STATUS_CONFLICT:
                log.info("Duplicate creation of user attempted on {}", userIdentityJson);
                //throw new ConflictExeption("DuplicateCreateAttempted on " + userIdentityJson);
                throw AppExceptionCode.MISC_ConflictException_9995.setDeveloperMessage("Duplicate creation of user attempted on {}", userIdentityJson);
            case STATUS_BAD_REQUEST:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
                //throw new BadRequestException("BadRequest for Json " + userIdentityJson + ",  Status code " + response.getStatus());
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
            default:
                log.error("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
                //throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("createUser-Response from UIB: {}: {}", response.getStatus(), userJson);
        }
        return userIdentity;
    }

	private WebTarget getWebTarget() {
		Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , myUibUrl);
        return client.target(myUibUrl);
	}

    public UserAggregate addUserAgregate(String userAdminServiceTokenId, String userTokenId, String userAggregateJson) throws AppException {
    	uib = getWebTarget();
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
                //throw new BadRequestException("BadRequest for Json " + userAggregateJson + ",  Status code " + response.getStatus());
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
            default:
                log.error("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
                //throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
        }
        return userAggregate;
    }


    public Response updateUserIdentity(String userAdminServiceTokenId, String userTokenId, String uid, String userIdentityJson) {
    	uib = getWebTarget();
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


    public boolean changePassword(String userAdminServiceTokenId, String adminUserTokenId, String userName, String password) throws AppException {
    	uib = getWebTarget();
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
                //throw new AuthenticationFailedException("Authentication failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("changePassword-Response from UIB: {}: {}", response.getStatus(), passwordJson);
        }
        return updatedOk;
    }

    public boolean hasUserSetPassword(String userAdminServiceTokenId, String userName) {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/user").path(userName).path("password_login_enabled");
        Response responseFromUib = webResource.request(MediaType.TEXT_PLAIN).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        log.debug("Response from UIB: {}", responseFromUib.getEntity().toString());
        if (responseFromUib.hasEntity()) {
            Boolean responseBody = Boolean.valueOf(responseFromUib.readEntity(String.class));
            return responseBody;
        }
        return false;
    }

    public UserApplicationRoleEntry addRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
    	uib = getWebTarget();
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
                //throw new ConflictExeption("DuplicateCreateAttempted on " + roleJson);
                throw AppExceptionCode.MISC_ConflictException_9995.setDeveloperMessage("Duplicate creation of role attempted on {}", roleJson);
            case STATUS_BAD_REQUEST:
                log.error("addRole-Response from UIB: {}: {}",statusCode, roleJson);
                //throw new BadRequestException("BadRequest for Json " + roleJson + ",  Status code " + statusCode);
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("addRole-Response from UIB: {}: {}",statusCode, roleJson);
            default:
                log.error("addRole-Response from UIB: {}: {}", statusCode, roleJson);
                //throw new AuthenticationFailedException("Authentication failed. Status code " + statusCode);
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("addRole-Response from UIB: {}: {}", statusCode, roleJson);
        }
        return role;
    }
    public UserApplicationRoleEntry updateRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid).path("role").path(roleRequest.getId());
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        String roleJson = response.readEntity(String.class);
        UserApplicationRoleEntry role = null;
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_OK:
                log.trace("updateRole-Response from UIB {}", roleJson);
                role = UserRoleMapper.fromJson(roleJson);
                break;
            case STATUS_CREATED:
                log.trace("updateRole-roleCreated {}", roleJson);
                role = UserRoleMapper.fromJson(roleJson);
                break;
            case STATUS_CONFLICT:
                log.info("Duplicate creation of role attempted on {}", roleJson);
                //throw new ConflictExeption("DuplicateCreateAttempted on " + roleJson);
                throw AppExceptionCode.MISC_ConflictException_9995.setDeveloperMessage("Duplicate creation of role attempted on {}", roleJson);
            case STATUS_BAD_REQUEST:
                log.error("updateRole-Response from UIB: {}: {}",statusCode, roleJson);
                //throw new BadRequestException("BadRequest for Json " + roleJson + ",  Status code " + statusCode);
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("updateRole-Response from UIB: {}: {}",statusCode, roleJson);
            default:
                log.error("updateRole-Response from UIB: {}: {}", statusCode, roleJson);
                //throw new AuthenticationFailedException("Authentication failed. Status code " + statusCode);
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("updateRole-Response from UIB: {}: {}", statusCode, roleJson);
        }
        return role;
    }

    public void deleteUserRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, String userRoleId) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role").path(userRoleId);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_NO_CONTENT:
                log.trace("deleteUserRole-Response from UIB {}", userRoleId);
                break;
            case STATUS_BAD_REQUEST:
                log.error("deleteUserRole-Response from UIB: {}: {}",statusCode, userRoleId);
                //throw new BadRequestException("deleteUserRole for userRoleId " + userRoleId + ",  Status code " + statusCode);
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("deleteUserRole-Response from UIB: {}: {}",statusCode, userRoleId);
            default:
                log.error("deleteUserRole-Response from UIB: {}: {}", statusCode, userRoleId);
                //throw new RuntimeException("DeleteUserRole failed. Status code " + statusCode);
                throw AppExceptionCode.MISC_RuntimeException_9994.setDeveloperMessage("deleteUserRole-Response from UIB: {}: {}", statusCode, userRoleId);
        }

    }



//    public UserAggregate addPropertyOrRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry userPropertyAndRole) throws AppException {
//    	uib = getWebTarget();
//        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role");
//        UserAggregate updatedUser = null;
//        UserAggregate userAggregateRepresentation = null;
//        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(userPropertyAndRole.toJson(), MediaType.APPLICATION_JSON));
//        int statusCode = response.getStatus();
//        switch (statusCode) {
//            case STATUS_OK:
//                log.trace("addPropertyOrRole-Response from UIB {}", response.readEntity(String.class));
//                updatedUser = UserAggregateMapper.fromJson(response.readEntity(String.class));
//                break;
//            case STATUS_FORBIDDEN:
//                log.error("addPropertyOrRole-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), response.readEntity(String.class));
//                break;
//            default:
//                log.error("addPropertyOrRole-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
//                //throw new AuthenticationFailedException("addPropertyOrRole failed. Status code " + response.getStatus());
//                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("addPropertyOrRole-Response from UIB: {}: {}", response.getStatus(), response.readEntity(String.class));
//        }
//        return updatedUser;
//    }


    //TODO Clean up exception/failure handling
    public UserIdentity getUserIdentity(String userAdminServiceTokenId, String adminUserTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        String responseBody = response.readEntity(String.class);
        switch (response.getStatus()) {
            case STATUS_OK:
                log.info("getUserIdentity-Response from Uib {}", responseBody);
                UserIdentity userIdentity = UserIdentityMapper.fromUserIdentityJson(responseBody);
                return userIdentity;
            case STATUS_FORBIDDEN:
                log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                //return null;
                throw AppExceptionCode.MISC_FORBIDDEN_9993.setDeveloperMessage("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
            case STATUS_UNAUTHORIZED:
                log.error("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
                //return null;
                throw AppExceptionCode.MISC_NotAuthorizedException_9992.setDeveloperMessage("getUserIdentity-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
            default:
                log.error("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
                //throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("getUserIdentity-Response from UIB: {}: {}", response.getStatus(), responseBody);
        }
    }


    public UserAggregate getUserAggregateByUid(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
    	 uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        UserAggregate userAggregate = null;
        UserAggregate userAggregateRepresentation;
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        int statusCode = response.getStatus();
        String responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("getUserAggregateByUid-Response from Uib {}", responseBody);
                userAggregate = UserAggregateMapper.fromUserAggregateNoIdentityJson(responseBody);
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

    public String getUserAggregateByUidAsJson(String userAdminServiceTokenId, String adminUserTokenId, String uid) throws AppException {
    	uib = getWebTarget();
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
                //break;
                throw AppExceptionCode.MISC_FORBIDDEN_9993.setDeveloperMessage("getUserAggregateByUid-Not allowed from UIB: {}: {} Using adminUserTokenId {}, userName {}", response.getStatus(), responseBody);
            default:
                log.error("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
                //throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("getUserAggregateByUid-Response from UIB: {}: {}", response.getStatus(), responseBody);
        }
    }


    public String getRolesAsJson(String userAdminServiceTokenId, String userTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("/user").path(uid).path("roles");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return findResponseBody("getRolesAsJson", response);
    }

    private String findResponseBody(String methodName, Response response) throws AppException {
        String responseBody;
        int statusCode = response.getStatus();
        responseBody = response.readEntity(String.class);
        switch (statusCode) {
            case STATUS_OK:
                log.trace("{}-Response from UIB {}", methodName,responseBody);
                break;
            case STATUS_FORBIDDEN:
                log.error("{}-Not allowed from UIB: {}: {} ", methodName,response.getStatus(), responseBody);
                //responseBody = null;
                //break;
                throw AppExceptionCode.MISC_FORBIDDEN_9993.setDeveloperMessage("{}-Not allowed from UIB: {}: {} ", methodName,response.getStatus(), responseBody);
            default:
                log.error("{}-Response from UIB: {}: {}", methodName,response.getStatus(), responseBody);
                //throw new AuthenticationFailedException("getUserIdentity failed. Status code " + response.getStatus());
                throw AppExceptionCode.MISC_AuthenticationFailedException_9996.setDeveloperMessage("{}-Response from UIB: {}: {}", methodName,response.getStatus(), responseBody);
        }
        return responseBody;
    }


    public void deleteUser(String userAdminServiceTokenId, String adminUserTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        int statusCode = response.getStatus();

        switch (statusCode) {
            case STATUS_NO_CONTENT:
                log.trace("deleteUser-Response from UIB uid={}", uid);
                break;
            case STATUS_BAD_REQUEST:
                log.error("deleteUser-Response from UIB: {}: uid={}", statusCode, uid);
                //throw new BadRequestException("deleteUserRole for uid=" + uid + ",  Status code " + statusCode);
                throw AppExceptionCode.MISC_BadRequestException_9997.setDeveloperMessage("deleteUser-Response from UIB: {}: uid={}", statusCode, uid);
            default:
                log.error("deleteUser-Response from UIB: {}, uid=", statusCode, uid);
                //throw new RuntimeException("DeleteUser failed. Status code " + statusCode);
                throw AppExceptionCode.MISC_RuntimeException_9994.setDeveloperMessage("deleteUser-Response from UIB: {}, uid=", statusCode, uid);
        }
    }
}
