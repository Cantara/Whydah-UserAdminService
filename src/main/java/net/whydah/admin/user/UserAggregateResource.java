package net.whydah.admin.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.user.uib.UserAggregate;
import net.whydah.admin.user.uib.UserAggregateRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Endpoint for useraggregate.
 */
@Component
@Path("/{applicationtokenid}/{usertokenid}/useraggregate")
public class UserAggregateResource {
    private static final Logger log = LoggerFactory.getLogger(UserAggregateResource.class);

    private final UserService userAggregateService;
    private final ObjectMapper mapper;


    @Context
    private UriInfo uriInfo;

    @Autowired
    public UserAggregateResource(UserService userAggregateService) {
        this.userAggregateService = userAggregateService;
        this.mapper = new ObjectMapper();
    }

    @GET
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getUserAggregateByUid(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("usertokenid") String userTokenId,
                                          @PathParam("uid") String uid) {
        log.trace("getUserAggregateByUid with uid={}", uid);

        UserAggregate userAggregate;
        try {
            userAggregate = userAggregateService.getUserAggregateByUid(applicationTokenId, userTokenId, uid);
            if (userAggregate == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("no user with uid" + uid).build();
            }
        } catch (RuntimeException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        //TODO rewrite urls to point to UAS instead of UIB

        String json = UserAggregateRepresentation.fromUserAggregate(userAggregate).toJson();
        return Response.ok(json).build();
        /*
        try {
            String userResponse = mapper.writeValueAsString(userAggregate);
            return Response.ok(userResponse).build();
        } catch (IOException e) {
            log.error("", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        */
    }
}
