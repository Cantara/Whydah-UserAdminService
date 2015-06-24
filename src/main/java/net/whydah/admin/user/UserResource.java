package net.whydah.admin.user;

import net.whydah.admin.ConflictExeption;
import net.whydah.admin.application.Application;
import net.whydah.admin.user.uib.*;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;
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
    ObjectMapper mapper = new ObjectMapper();


    @Context
    private Request request;


    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user from xml.
     *
     *
     * Password is left out deliberately. A password belong to user credential as in user login. We will support multiple ways for logging in,
     * where uid/passord is one. Another login is via FB and Windows AD tokens.
     *
     * @param userXml xml representing a User
     * @return Application
     */
    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createUser(@PathParam("applicationtokenid") String applicationTokenId,
                               @PathParam("userTokenId") String userTokenId, String userXml, @Context Request request) {
        log.trace("createUser is called with userXml={}", userXml);
        MediaType responseMediaType = findPreferedResponseType(request);
        UserIdentity createdUser;
        String userResponse;
        UserAggregate userAggregate = null;
        try {
            if (isXmlContent(userXml)) {
                createdUser = userService.createUserFromXml(applicationTokenId, userTokenId, userXml);
            } else {
                createdUser = userService.createUser(applicationTokenId, userTokenId, userXml);
            }




            if (createdUser != null) {
                userAggregate = new UserAggregate(createdUser, new ArrayList<UserPropertyAndRole>());
                if (responseMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)){
                    userResponse = mapper.writeValueAsString(userAggregate);
                } else {
                    userResponse = buildUserXml(userAggregate);
                }

                return Response.ok(userResponse).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        } catch (IllegalArgumentException iae) {
            log.error("createUser: Invalid xml={}", userXml, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.info(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (ConflictExeption ce) {
            log.info(ce.getMessage());
            ResponseBuilderImpl builder = new ResponseBuilderImpl();
            builder.status(Response.Status.CONFLICT);
            builder.entity("An existing user record conflicts with your input. Future versions will notify which parameter is in conflict. Unique parmeters are: username and email. ");
            Response response = builder.build();
            return Response.status(Response.Status.CONFLICT).build();
        }  catch (JsonMappingException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JsonGenerationException e) {
            log.error("Could not map created user to Json. User: {}", userAggregate.toString(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            log.error("Could not map created user to Json. User: {}", userAggregate.toString(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isXmlContent(String userXml) {
        boolean isXml = false;
        if (userXml != null) {
          isXml = userXml.trim().startsWith("<");
        }
        return isXml;
    }

    @POST
    @Path("/xml")
    public Response createUserFromXml(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, String userXml, @Context Request request) {
        return  createUser(applicationTokenId,userTokenId,userXml,request);
    }


        @POST
    @Path("/changePassword/{username}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response changePassword(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                   @PathParam("username") String userName, String password) {
        log.trace("changePassword is called with username={}", userName);
        boolean isPasswordUpdated = false;
        try {
            isPasswordUpdated = userService.changePassword(applicationTokenId, userTokenId, userName, password);

        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        String passwdOk = "<passwordUpdated><username>" + userName +"</username><status>" + isPasswordUpdated+"</status></passwordUpdated>";
            return Response.ok(passwdOk).build();
    }

    @GET
    @Path("/{uid}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getUser(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                   @PathParam("uid") String uid, @Context Request req) {
        MediaType responseMediaType = findPreferedResponseType(req);
        log.trace("getUser is called with uid={}. Preferred mediatype from client {}", uid, responseMediaType.toString());
        String userResponse;
        UserAggregate userAggregate = null;

        try {
            userAggregate = userService.getUser(applicationTokenId, userTokenId, uid);
            if (responseMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)){
                userResponse = mapper.writeValueAsString(userAggregate);
            } else {
                userResponse = buildUserXml(userAggregate);
            }
            return Response.ok(userResponse).build();
        } catch (IllegalArgumentException iae) {
            log.error("getUser: Invalid xml={}", uid, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (JsonMappingException e) {
            log.warn("Could not create json from {}", userAggregate.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JsonGenerationException e) {
            log.warn("Could not create json from {}", userAggregate.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            log.warn("Could not responseobject from {}", userAggregate.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{uid}")
    public Response deleteUser(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               @PathParam("uid") String uid) {
        log.trace("deleteUser, uid={}, roleid={}", uid);

        try {
            userService.deleteUser(applicationTokenId, userTokenId,uid);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (RuntimeException e) {
            log.error("deleteUser-RuntimeException. uid {}", uid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GET
    @Path("/{uid}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoles(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, @PathParam("uid") String uid) {
        log.trace("getRoles, uid={}", uid);
        String roles = userService.getRolesAsString(applicationTokenId, userTokenId,uid);
        return Response.ok(roles).build();
    }

    @GET
    @Path("/{uid}/roles")
    @Produces(MediaType.APPLICATION_XML)
    public Response getRolesAsXml(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, @PathParam("uid") String uid) {
        log.trace("getRoles, uid={}", uid);

        List<RoleRepresentation> roles = userService.getRoles(applicationTokenId, userTokenId, uid);
        String rolesXml = "";
        for (RoleRepresentation role : roles) {
            rolesXml += role.toXML();
        }

        return Response.ok(rolesXml).build();
    }


    private MediaType findPreferedContentType(Request req) {
        /**
         * /* This method builds a list of possible variants. */
        /* You must call Variant.VariantListBuilder.add() object before calling the Variant.VariantListBuilder.build() object. */
        List<Variant> contentVariants =
                Variant
                        .mediaTypes(
                                MediaType.valueOf(MediaType.APPLICATION_XML ),
                                MediaType.valueOf(MediaType.APPLICATION_XML ),
                                MediaType
                                        .valueOf(MediaType.APPLICATION_JSON))
//                        .encodings("gzip", "identity", "deflate").languages(Locale.ENGLISH,
//                        Locale.FRENCH,
//                        Locale.US)
                        .add().build();

          /* Based on the Accept* headers, an acceptable response variant is chosen.  If there is no acceptable variant,
          selectVariant will return a null value. */

        Variant bestResponseVariant = req.selectVariant(contentVariants);
//        if(bestResponseVariant == null) {
//
//             /* Based on results, the optimal response variant can not be determined from the list given.  */
//
//            return Response.notAcceptable().build();
//        }
        return bestResponseVariant.getMediaType();
    }
    private MediaType findPreferedResponseType(Request req) {
        /**
         * /* This method builds a list of possible variants. */
        /* You must call Variant.VariantListBuilder.add() object before calling the Variant.VariantListBuilder.build() object. */
        List<Variant> responseVariants =
                Variant
                        .mediaTypes(
                                MediaType.valueOf(MediaType.APPLICATION_XML ),
                                MediaType.valueOf(MediaType.APPLICATION_XML ),
                                MediaType
                                        .valueOf(MediaType.APPLICATION_JSON))
//                        .encodings("gzip", "identity", "deflate").languages(Locale.ENGLISH,
//                        Locale.FRENCH,
//                        Locale.US)
                            .add().build();

          /* Based on the Accept* headers, an acceptable response variant is chosen.  If there is no acceptable variant,
          selectVariant will return a null value. */

        Variant bestResponseVariant = req.selectVariant(responseVariants);
//        if(bestResponseVariant == null) {
//
//             /* Based on results, the optimal response variant can not be determined from the list given.  */
//
//            return Response.notAcceptable().build();
//        }
        return bestResponseVariant.getMediaType();
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addRole(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("uid") String uid, String roleXmlOrJson) {
        log.trace("addRole is called with uid={}, roleXmlOrJson={}", uid, roleXmlOrJson);

        List<Variant> availableVariants = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).add().build();
        Variant bestMatch = request.selectVariant(availableVariants);
        MediaType mediaType = bestMatch.getMediaType();

        try {
            RoleRepresentationRequest roleRequest;
            if (mediaType == MediaType.APPLICATION_XML_TYPE) {
                roleRequest = RoleRepresentationRequest.fromXml(roleXmlOrJson);
                RoleRepresentation roleRepresentation = userService.addUserRole(applicationTokenId, userTokenId, uid, roleRequest);
                return Response.ok(roleRepresentation.toXML()).build();
            } else if (mediaType == MediaType.APPLICATION_JSON_TYPE) {
                roleRequest = RoleRepresentationRequest.fromJson(roleXmlOrJson);
                RoleRepresentation roleRepresentation = userService.addUserRole(applicationTokenId, userTokenId, uid, roleRequest);
                return Response.ok(roleRepresentation.toJson()).build();
            }  else {
                log.error("addRole failed. Invalid roleXmlOrJson={}, uid={}", roleXmlOrJson, uid);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (IllegalArgumentException iae) {
            log.error("addRole: Invalid xml={}, uid={}", roleXmlOrJson,uid, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error("addRole: IllegalStateException roleXmlOrJson={}, uid={}", roleXmlOrJson, uid, ise);
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("addRole: RuntimeException roleXmlOrJson={}, uid={}", roleXmlOrJson, uid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     *
     * @param applicationTokenId
     * @param userTokenId
     * @param uid
     * @param roleJson expect to be on the forme like this
     *                 {"uid":"test.me@example.com","
     *                 applicationId":"12",
     *                 "applicationRoleName":"developer",
     *                 "applicationRoleValue":"30",
     *                 "applicationName":"UserAdminService",
     *                 "organizationName":"Verification"}
     * @return
     */
    /*
    @POST
    @Path("/{uid}/role/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoleJson(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                @PathParam("uid") String uid, String roleJson) {
        log.trace("addRoleJson is called with uid={}, roleJson {}", uid, roleJson);
        try {
            RoleRepresentationRequest roleRequest = RoleRepresentationRequest.fromJson(roleJson);
            RoleRepresentation roleRepresentation = userService.addUserRole(applicationTokenId, userTokenId, uid, roleRequest);
            return Response.ok(roleRepresentation.toJson()).build();
        } catch (IllegalArgumentException iae) {
            log.error("addRoleJson: Invalid json={}, uid {}", roleJson,uid, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error("addRoleJson: IllegalStateException json={}, uid {}", roleJson,uid, ise);
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("addRoleJson: RuntimeException json={}, uid {}", roleJson,uid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    */

    /*
    @POST
    @Path("/{uid}/rolexml")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addRoleXml(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("uid") String uid, String roleXml) {
        log.trace("addRole is called with uid={}, roleXml {}", uid, roleXml);

        try {
            RoleRepresentationRequest roleRequest = RoleRepresentationRequest.fromXml(roleXml);
            RoleRepresentation roleRepresentation = userService.addUserRole(applicationTokenId, userTokenId, uid, roleRequest);
            return Response.ok(roleRepresentation.toXML()).build();
        } catch (IllegalArgumentException iae) {
            log.error("addRoleXml: Invalid xml={}, uid {}", roleXml,uid, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error("addRoleXml: IllegalStateException xml={}, uid {}", roleXml,uid, ise);
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("addRoleXml: RuntimeException xml={}, uid {}", roleXml,uid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    */


    @DELETE
    @Path("/{uid}/role/{roleid}")
    public Response deleteRole(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               @PathParam("uid") String uid, @PathParam("roleid") String roleid) {
        log.trace("deleteRole, uid={}, roleid={}", uid, roleid);

        try {
            userService.deleteUserRole(applicationTokenId, userTokenId,uid, roleid);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (RuntimeException e) {
            log.error("deleteRole-RuntimeException. roleId {}", roleid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    public Response ping() {
        return Response.ok("pong").build();
    }

    protected String buildApplicationJson(Application application) {
        String applicationCreatedJson = null;
        try {
            applicationCreatedJson = mapper.writeValueAsString(application);
        } catch (IOException e) {
            log.warn("Could not convert application to Json {}", application.toString());
        }
        return applicationCreatedJson;
    }

    protected String buildUserXml(UserAggregate userAggregate) {
        String userXml = null;
        if (userAggregate != null) {
            userXml = userAggregate.toXML();
        }
        return userXml;
    }
}
