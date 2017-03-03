package net.whydah.admin.user;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.user.uib.*;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/user")
@Controller
public class UserResource {
    private static final Logger log = LoggerFactory.getLogger(UserResource.class);
    UserService userService;

    @Context
    private Request request;


    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user from xml.
     * <p>
     * <p>
     * Password is left out deliberately. A password belong to user credential as in user login. We will support multiple ways for logging in,
     * where uid/passord is one. Another login is via FB and Windows AD tokens.
     *
     * @param userJson xml representing a User
     * @return Application
     * @throws AppException 
     */
    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createUser(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               String userJson, @Context Request request) throws AppException {
        log.trace("createUser is called with userJson={}", userJson);
        MediaType responseMediaType = findPreferredResponseMediaType();

        UserIdentity createdUser;
        String userResponse;
        UserAggregate userAggregate = null;


        createdUser = userService.createUser(applicationTokenId, userTokenId, userJson);


        if (createdUser != null) {
        	userAggregate = UserAggregateMapper.fromUserAggregateNoIdentityJson(UserIdentityMapper.toJson(createdUser));
        	return Response.ok(UserAggregateMapper.toJson(userAggregate)).build();
        } else {
        	return Response.status(Response.Status.NO_CONTENT).build();
        }

    }

    @Deprecated //TODO merge with normal endpoint
    @POST
    @Path("/xml")
    public Response createUserFromXml(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, String userXml, @Context Request request) throws AppException {
        return createUser(applicationTokenId, userTokenId, userXml, request);
    }


    @GET
    @Path("/{uid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserIdentity(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                    @PathParam("uid") String uid, @Context Request req) throws AppException {
        MediaType mediaType = findPreferredResponseMediaType();
        //MediaType responseMediaType = findPreferedResponseType(req);
        log.trace("getUserIdentity is called with uid={}. Preferred mediatype from client {}", uid, mediaType);
        String userResponse;
        UserIdentity userIdentity = null;


        userIdentity = userService.getUserIdentity(applicationTokenId, userTokenId, uid);
        userResponse = UserIdentityMapper.toJson(userIdentity);
        return Response.ok(userResponse).build();
       
    }

    @PUT
    @Path("/{uid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserIdentity(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                       @PathParam("uid") String uid, String userIdentityJson) throws AppException {
        log.trace("updateUserIdentity: uid={}, userIdentityJson={}", uid, userIdentityJson);
        return userService.updateUserIdentity(applicationTokenId, userTokenId, uid, userIdentityJson);
    }


    @DELETE
    @Path("/{uid}")
    public Response deleteUser(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               @PathParam("uid") String uid) throws AppException {
        log.trace("deleteUser, uid={}, roleid={}", uid);


        userService.deleteUser(applicationTokenId, userTokenId, uid);
        return Response.status(Response.Status.NO_CONTENT).build();
       
    }

    @POST
    @Path("/changePassword/{username}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response changePassword(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                   @PathParam("username") String userName, String password) throws AppException {
        log.trace("changePassword is called with username={}", userName);
        boolean isPasswordUpdated = false;
        isPasswordUpdated = userService.changePassword(applicationTokenId, userTokenId, userName, password);
        String passwdOk = "<passwordUpdated><username>" + userName + "</username><status>" + isPasswordUpdated + "</status></passwordUpdated>";
        return Response.ok(passwdOk).build();
    }


    @GET
    @Path("/{uid}/roles")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoles(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, @PathParam("uid") String uid) {
        log.trace("getRoles, uid={}", uid);
        MediaType responseMediaType = findPreferredResponseMediaType();

        try {
            String body = null;
            if (responseMediaType == MediaType.APPLICATION_XML_TYPE) {
                body = userService.getRolesAsXml(applicationTokenId, userTokenId, uid);
            } else if (responseMediaType == MediaType.APPLICATION_JSON_TYPE) {
                body = userService.getRolesAsJson(applicationTokenId, userTokenId, uid);
            }
            log.trace("getRoles for uid={}, response: {}", uid, body);
            return Response.ok(body).build();
        } catch (Exception e) {
            log.error("getRoles failed. uid={}", uid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType findPreferredResponseMediaType() {
        List<Variant> availableVariants = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).add().build();
        Variant bestMatch = request.selectVariant(availableVariants);
        return bestMatch.getMediaType();
    }


    /*
     * @param roleXmlOrJson for json
     * {"uid":"test.me@example.com","
     *                 applicationId":"12",
     *                 "applicationRoleName":"developer",
     *                 "applicationRoleValue":"30",
     *                 "applicationName":"UserAdminService",
     *                 "organizationName":"Verification"}
     */
    @POST
    @Path("/{uid}/role")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response addRole(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("uid") String uid, String roleXmlOrJson) throws AppException {
        log.trace("addRole is called with uid={}, roleJson={}", uid, roleXmlOrJson);

        UserApplicationRoleEntry roleRequest = UserRoleMapper.fromJson(roleXmlOrJson);
        UserApplicationRoleEntry roleRepresentation = userService.addUserRole(applicationTokenId, userTokenId, uid, roleRequest);
        return Response.ok(roleRepresentation.toJson()).build();

    }

    @PUT
    @Path("/{uid}/role/{roleid}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateRole(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("uid") String uid, @PathParam("roleid") String roleid, String roleJson) throws AppException {
        log.trace("addRole is called with uid={}, roleid={},roleJson={}", uid, roleid,roleJson);

        UserApplicationRoleEntry roleRequest = UserRoleMapper.fromJson(roleJson);
        UserApplicationRoleEntry updatedRole = userService.updateUserRole(applicationTokenId, userTokenId, uid, roleRequest);
        return Response.ok(updatedRole.toJson()).build();
        
    }


    @DELETE
    @Path("/{uid}/role/{roleid}")
    public Response deleteRole(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               @PathParam("uid") String uid, @PathParam("roleid") String roleid) throws AppException {
        log.trace("deleteRole, uid={}, roleid={}", uid, roleid);
        userService.deleteUserRole(applicationTokenId, userTokenId, uid, roleid);
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }


}
