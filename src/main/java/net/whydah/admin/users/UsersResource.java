package net.whydah.admin.users;

import edu.emory.mathcs.backport.java.util.Arrays;
import net.whydah.admin.AuthenticationFailedException;
import net.whydah.admin.errorhandling.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/users")
@Controller
public class UsersResource {
    private static final Logger log = LoggerFactory.getLogger(UsersResource.class);
    //UIB interface   /{applicationtokenid}/{usertokenid}/users/find/{q}

    private final UsersService usersService;

    @Autowired
    public UsersResource(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * UserAdmin Find, return UserAggregateDeprecated
     * @throws AppException 
     */
    @GET
    @Path("/find/{q}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                              @PathParam("q") String query) throws AppException {

        String usersJson = null;
        try {
            usersJson = usersService.findUsers(applicationTokenId, userTokenId, query);
            if (usersJson != null) {
                return Response.ok(usersJson).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (AuthenticationFailedException afe) {
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Directory search, return only UserIdentityDeprecated
     * @throws AppException 
     */
    @GET
    @Path("/search/{q}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                @PathParam("q") String query) throws AppException {

        String usersJson = null;
        try {
            usersJson = usersService.searchUsers(applicationTokenId, userTokenId, query);
            if (usersJson != null) {
                log.debug("usersService.searchUsers returned:" + usersJson);
                return Response.ok(usersJson).build();
            } else {
                log.debug("usersService.searchUsers returned:" + Response.Status.NO_CONTENT);
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
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

    @GET
    @Path("/query/{page}/{q}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
    		@PathParam("page") String page,  @PathParam("q") String query) throws AppException {

        String usersJson = null;
        try {
            usersJson = usersService.queryUsers(applicationTokenId, userTokenId, page, query);
            if (usersJson != null) {
                return Response.ok(usersJson).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }


    }
    
    @GET
    @Path("/export/{page}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
    		@PathParam("page") String page) throws AppException {

        log.debug("exportUsers start");
        String usersJson = null;
        try {
            usersJson = " " + usersService.exportUsers(applicationTokenId, userTokenId, page) + " ";
            if (usersJson != null) {
                log.debug("exportUsers result: " + usersJson);
                return Response.ok(usersJson.trim()).build();
            } else {
                log.debug("exportUsers empty result: " + usersJson);
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (RuntimeException e) {
            log.error("exportUsers Unknown error." + Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }


    }
    

    @POST
    @Path("/checkduplicates")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDuplicateUsersFromJson(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                             String json) throws AppException {

        String responseData = null;
        try {
        	responseData = usersService.getDuplicateUsers(applicationTokenId, userTokenId, json);
            if (responseData != null) {
                return Response.ok(responseData).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (AuthenticationFailedException afe) {
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }
    
    @GET
    @Path("/checkexist/{username}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response checkExists(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
    		 @PathParam("username") String username) throws AppException {

        String usersJson = null;
        try {
            usersJson = usersService.checkExist(applicationTokenId, userTokenId, username);
            if (usersJson != null) {
                return Response.ok(usersJson).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }


    }

}
