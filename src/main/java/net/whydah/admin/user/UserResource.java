package net.whydah.admin.user;

import net.whydah.admin.ConflictExeption;
import net.whydah.admin.application.Application;
import net.whydah.admin.user.uib.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/user")
@Component
public class UserResource {
    private static final Logger log = LoggerFactory.getLogger(UserResource.class);
    UserService userService;
    ObjectMapper mapper = new ObjectMapper();


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
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createUser(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId, String userXml) {
        log.trace("createUser is called with userXml={}", userXml);
        UserIdentity createdUser = null;
        try {
            createdUser = userService.createUserFromXml(applicationTokenId, userTokenId, userXml);

        } catch (IllegalArgumentException iae) {
            log.error("createUser: Invalid xml={}", userXml, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.info(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (ConflictExeption ce) {
            log.info(ce.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (createdUser != null) {
            UserAggregate userAggregate = new UserAggregate(createdUser, new ArrayList<UserPropertyAndRole>());
            String createdUserXml = userAggregate.toXML();
            return Response.ok(createdUserXml).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
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
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getUser(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                   @PathParam("userId") String userId) {
        log.trace("userId is called with userId={}", userId);
        try {
            Application application = userService.getUser(applicationTokenId, userTokenId, userId);
            String applicationCreatedXml = buildApplicationXml(application);
            return Response.ok(applicationCreatedXml).build();
        } catch (IllegalArgumentException iae) {
            log.error("getUser: Invalid xml={}", userId, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error(ise.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{userId}/role")
    @Produces(MediaType.APPLICATION_XML)
    public Response addRole(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("userId") String userId, String roleXml) {
        log.trace("addRole is called with userId={}, roleXml {}", userId, roleXml);

        try {
            RoleRepresentationRequest roleRequest = RoleRepresentationRequest.fromXml(roleXml);
            RoleRepresentation roleRepresentation = userService.addUserRole(applicationTokenId, userTokenId, userId, roleRequest);
            return Response.ok(roleRepresentation.toXML()).build();
        } catch (IllegalArgumentException iae) {
            log.error("addRole: Invalid xml={}, userId {}", roleXml,userId, iae);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException ise) {
            log.error("addRole: IllegalStateException xml={}, userId {}", roleXml,userId, ise);
            return Response.status(Response.Status.CONFLICT).build();
        } catch (RuntimeException e) {
            log.error("addRole: RuntimeException xml={}, userId {}", roleXml,userId, e);
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
    protected String buildApplicationXml(Application application) {
        String applicationCreatedXml = null;
        if (application != null) {
            applicationCreatedXml = application.toXML();
        }
        return applicationCreatedXml;
    }
}
