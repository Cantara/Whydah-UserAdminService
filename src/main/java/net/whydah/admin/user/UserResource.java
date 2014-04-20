package net.whydah.admin.user;

import net.whydah.admin.application.Application;
import net.whydah.admin.user.uib.UserAggregate;
import net.whydah.admin.user.uib.UserIdentity;
import net.whydah.admin.user.uib.UserPropertyAndRole;
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
     * Create a new user from xml
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
            log.error(ise.getMessage());
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
