package net.whydah.admin.user.uib;

import net.whydah.admin.security.UASCredentials;
import net.whydah.sso.commands.userauth.CommandRefreshUserTokenByUserName;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Created by baardl on 17.04.14.
 * Adjusted by Huy on 3.3.2017
 */
@Component
public class UibUserConnection {
    private static final Logger log = LoggerFactory.getLogger(UibUserConnection.class);

    private WebTarget uib;
    private final UASCredentials uasCredentials;
    private final String myUibUrl;
    private final String myStsUrl;

    @Autowired
    public UibUserConnection(@Value("${securitytokenservice}") String stsUrl, @Value("${useridentitybackend}") String uibUrl, UASCredentials uasCredentials) {
        this.uasCredentials = uasCredentials;
        this.myStsUrl = stsUrl;
        this.myUibUrl = uibUrl;
    }

    private Response copyResponse(Response responseFromUib) {
//		Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
//		if (responseFromUib.hasEntity()) {
//			rb.entity(responseFromUib.getEntity());
//		}
//		return rb.build();
        return responseFromUib;
    }

    public Response createUser(String userAdminServiceTokenId, String userTokenId, String userIdentityJson){
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
                //we should call STS to sync user info here
                refreshSTSCommand(userAdminServiceTokenId, userIdentityJson);
                break;
            default:
                log.warn("updateUserIdentity was unsuccessful. Response from UIB: {} {}, body={}", statusCode, reasonPhrase, responseBody);
        }
        return responseBuilder.build();
    }

    private void refreshSTSCommand(String userAdminServiceTokenId, String userIdentityJson) {
        try {
            UserIdentity userIdentity = UserIdentityMapper.fromJson(userIdentityJson);
            String userName = userIdentity.getUsername();
            log.warn("resolved userName: " + userName);
            new CommandRefreshUserTokenByUserName(URI.create(myStsUrl), userAdminServiceTokenId, "", userName).queue();
        } catch (Exception e) {
            log.error("Unable to refresh UserToken in STS", e);
        }

    }


    public Response changePassword(String userAdminServiceTokenId, String userTokenId, String userName, String password) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("user").path(userName).path("changepassword");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(password, MediaType.APPLICATION_JSON));
        return copyResponse(response);
    }

    public Response hasUserSetPassword(String userAdminServiceTokenId, String userName) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path("user").path(userName).path("password_login_enabled");
        Response responseFromUib = webResource.request(MediaType.TEXT_PLAIN).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(responseFromUib);
    }

    public Response hasThirdPartyLogin(String userAdminServiceTokenId, String userName, String provider) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path("user").path(userName).path(provider).path("thirdparty_login_enabled");
        Response responseFromUib = webResource.request(MediaType.TEXT_PLAIN).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(responseFromUib);
    }

    public Response addRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid).path("role");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        refreshSTS(userAdminServiceTokenId, adminUserTokenId, uid, response);
        return copyResponse(response);
    }

    public Response updateRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, UserApplicationRoleEntry roleRequest)  {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid).path("role").path(roleRequest.getId());
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(roleRequest.toJson(), MediaType.APPLICATION_JSON));
        refreshSTS(userAdminServiceTokenId, adminUserTokenId, uid, response);
        return copyResponse(response);
    }

    private void refreshSTS(String userAdminServiceTokenId, String adminUserTokenId, String uid, Response response) {
        int statusCode = 200;  // Accept null as statiusCode as we have to refresh before we delete the user to get the userName
        if (response != null) {
            statusCode = response.getStatus();
        }
        if(statusCode==200||statusCode==201||statusCode==204){
            Response responseGetUserIdentity = getUserIdentity(userAdminServiceTokenId, adminUserTokenId, uid);
            if (responseGetUserIdentity.getStatus() == 200) {
                String userIdentityJson = responseGetUserIdentity.readEntity(String.class);
                refreshSTSCommand(userAdminServiceTokenId, userIdentityJson);
            } else {
                log.error("Failed to get UserIdentity to sync with STS");
            }
        }
    }

    public Response deleteUserRole(String userAdminServiceTokenId, String adminUserTokenId, String uid, String userRoleId) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid).path("role").path(userRoleId);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        refreshSTS(userAdminServiceTokenId, adminUserTokenId, uid, response);
        return copyResponse(response);
    }

    public Response getUserIdentity(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }

    public Response getUserAggregateByUid(String userAdminServiceTokenId, String adminUserTokenId, String uid) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }

    public Response getUserAggregateByUidAsJson(String userAdminServiceTokenId, String adminUserTokenId, String uid){
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("useraggregate").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return response;
    }

    public Response getRolesAsJson(String userAdminServiceTokenId, String userTokenId, String uid) {
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(userTokenId).path("user").path(uid).path("roles");
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
        return copyResponse(response);
    }

    public Response deleteUser(String userAdminServiceTokenId, String adminUserTokenId, String uid)  {
        try {
            refreshSTS(userAdminServiceTokenId, adminUserTokenId, uid, null);
        } catch (Exception e) {
            log.error("Exception in refreshSTS - ignoring");
        }
        uib = getWebTarget();
        WebTarget webResource = uib.path(userAdminServiceTokenId).path(adminUserTokenId).path("user").path(uid);
        Response response = webResource.request(MediaType.APPLICATION_JSON).header(UASCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
        return copyResponse(response);
    }
}