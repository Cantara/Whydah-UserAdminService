package net.whydah.admin.users;

import net.whydah.admin.application.Application;
import net.whydah.admin.user.uib.UserIdentityRepresentation;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Path("/{applicationtokenid}/{userTokenId}/users")
@Controller
public class UsersResource {
    private static final Logger log = LoggerFactory.getLogger(UsersResource.class);
    //UIB interface   /{applicationtokenid}/{usertokenid}/users/find/{q}

    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findUsers(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
                            @PathParam("q") String query) {

        List<UserIdentityRepresentation> users = buildStubUsers();
        try {
            String usersResponse = mapper.writeValueAsString(users);
            if (usersResponse != null) {
                return Response.ok(usersResponse).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        }  catch (JsonMappingException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        } catch (IOException e) {
            log.error("Could not map created users to Json. Users: {}", users.toString(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.error("Unkonwn error.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }



    }

    private List<UserIdentityRepresentation> buildStubUsers() {
        List<UserIdentityRepresentation> users = new ArrayList<>();

        users.add(new UserIdentityRepresentation("WhydahUser2You", "User 2 You", "Whydah", "stubPersonRef2", "WhydahUser2You@example.com", "004592134452"));
        users.add(new UserIdentityRepresentation("WhydahUser3You", "User 3 You", "Whydah", "stubPersonRef3", "WhydahUser2You@example.com", "004592134453"));
        users.add(new UserIdentityRepresentation("WhydahUser4You", "User 4 You", "Whydah", "stubPersonRef4", "WhydahUser2You@example.com", "004592134454"));
        for (int i = 1; i < 6; i++) {
            users.add(stubUser("UserExtra" + i, "FirstName " + i));
        }

        return null;
    }

    private UserIdentityRepresentation stubUser(String username, String firstName) {
        return new UserIdentityRepresentation(username, firstName, "stubLastname", "stubPersonRef", "stub@example.com", "004792134455");
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
}
