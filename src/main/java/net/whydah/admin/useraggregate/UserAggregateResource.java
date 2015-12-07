package net.whydah.admin.useraggregate;

import net.whydah.admin.user.UserService;
import net.whydah.admin.user.uib.*;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import net.whydah.sso.user.types.UserAggregate;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.ArrayList;

@Path("/{applicationtokenid}/{userTokenId}/useraggregate")
@Controller
public class UserAggregateResource {
    private static final Logger log = LoggerFactory.getLogger(UserAggregateResource.class);
    UserService userService;

    @Context
    private Request request;


    @Autowired
    public UserAggregateResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user from xml.
     * <p>
     * <p>
     * Password is left out deliberately. A password belong to user credential as in user login. We will support multiple ways for logging in,
     * where uid/passord is one. Another login is via FB and Windows AD tokens.
     *
     * @param userAggregateJson xml representing a User
     * @return Application
     */
    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createUserAggregate(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                               String userAggregateJson, @Context Request request) {
        log.trace("createUser is called with userAggregateJson={}", userAggregateJson);

        UserAggregate userAggregate = UserAggregateMapper.fromJson(userAggregateJson);
        UserIdentity createdUser = userService.createUser(applicationTokenId, userTokenId, userAggregateJson);
        //
        // TODO - add roles to user here
        //
        if (createdUser != null) {
            UserAggregate createdUserAggregate = UserAggregateMapper.fromJson(createdUser.toJson());

            return Response.ok(UserAggregateMapper.toJson(createdUserAggregate)).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    private boolean isXmlContent(String userXml) {
        boolean isXml = false;
        if (userXml != null) {
            isXml = userXml.trim().startsWith("<");
        }
        return isXml;
    }


    @GET
    @Path("/{uid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserAggregate(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                    @PathParam("uid") String uid, @Context Request req) {
        log.trace("getUserAggregate is called with uid={}. ", uid);
        String userResponse;
        UserIdentity userIdentity = null;

        try {
            userIdentity = userService.getUserIdentity(applicationTokenId, userTokenId, uid);
            UserAggregate userAggregate = UserAggregateMapper.fromJson(userIdentity.toJson());
            //
            // TODO - fetch and add roles here
            //
            return Response.ok(UserAggregateMapper.toJson(userAggregate)).build();
        } catch (IllegalArgumentException iae) {
            log.error("getUserIdentity: Invalid xml={}", uid, iae);
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
    @Deprecated //Not used by ansible scrips anymore as of 2015-07-06
    public Response ping() {
        return Response.ok("pong").build();
    }


    protected String buildUserXml(UserAggregate userAggregate) {
        String userXml = null;
        if (userAggregate != null) {
            userXml = userAggregate.toXML();
        }
        return userXml;
    }
}
