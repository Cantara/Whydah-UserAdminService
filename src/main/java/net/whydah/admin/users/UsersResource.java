package net.whydah.admin.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
     * UserAdmin Find, return UserAggregate
     */
    @GET
    @Path("/find/{q}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                              @PathParam("q") String query) {

        String usersJson = null;
        try {
            usersJson = usersService.findUsers(applicationTokenId, userTokenId, query);
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

    /**
     * Directory search, return only UserIdentity
     */
    @GET
    @Path("/search/{q}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                                @PathParam("q") String query) {

        String usersJson = null;
        try {
            usersJson = usersService.findUsers(applicationTokenId, userTokenId, query);
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
    @Path("/ping/pong")
    @Produces(MediaType.TEXT_HTML)
    public Response ping() {
        return Response.ok("pong").build();
    }


}
