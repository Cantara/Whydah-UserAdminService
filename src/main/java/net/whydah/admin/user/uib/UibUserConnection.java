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
 * Adjusted by Huy on 3.3.2017
 */
@Component
public class UibUserConnection {
    private static final Logger log = LoggerFactory.getLogger(UibUserConnection.class);



    private  WebTarget uib;
    private final UASCredentials uasCredentials;
    private final String myUibUrl;

    @Autowired
    @Configure
    public UibUserConnection(@Configuration("useridentitybackend") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        this.myUibUrl=uibUrl;
    }

    private Response copyResponse(Response responseFromUib) {
		Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
		if (responseFromUib.hasEntity()) {
			rb.entity(responseFromUib.getEntity());
		}
		return rb.build();
	}
    
    public Response createUser(String userAdminServiceTokenId, String userTokenId, String userIdentityJson) throws AppException {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("user");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(userIdentityJson, MediaType.APPLICATION_JSON));
        return copyResponse(response);
    }

	private WebTarget getWebTarget() {
		Client client = ClientBuilder.newClient();
        log.info("Connection to UserIdentityBackend on {}" , myUibUrl);
        return client.target(myUibUrl);
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

    public Response changePassword(String userAdminServiceTokenId, String userTokenId, String userName, String password) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + userTokenId + "/user").path(userName).path("changepassword");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(password, MediaType.APPLICATION_JSON));
        return copyResponse(response);
    }

    public Response hasUserSetPassword(String userAdminServiceTokenId, String userName) {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/user").path(userName).path("password_login_enabled");
        Response responseFromUib = webResource.request(MediaType.TEXT_PLAIN).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(responseFromUib);
    }

    public Response addRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        return copyResponse(response);
    }
    
    public Response updateRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid).path("role").path(roleRequest.getId());
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        return copyResponse(response);
    }

    public Response deleteUserRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, String userRoleId) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid).path("role").path(userRoleId);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        return copyResponse(response);
    }

    public Response getUserIdentity(String userAdminServiceTokenId, String adminUserTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }

    public Response getUserAggregateByUid(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
    	uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }

    public Response getUserAggregateByUidAsJson(String userAdminServiceTokenId, String adminUserTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return response;
    }

    public Response getRolesAsJson(String userAdminServiceTokenId, String userTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("/user").path(uid).path("roles");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }

    public Response deleteUser(String userAdminServiceTokenId, String adminUserTokenId, String uid) throws AppException {
    	uib = getWebTarget();
        WebTarget webResource = uib.path("/" + userAdminServiceTokenId + "/" + adminUserTokenId + "/user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        return copyResponse(response);
    }
}
