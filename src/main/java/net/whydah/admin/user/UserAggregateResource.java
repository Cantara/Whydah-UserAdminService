package net.whydah.admin.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.whydah.admin.errorhandling.AppException;
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
@Path("/{applicationtokenid}/{usertokenid}/useraggregate2")
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
                                          @PathParam("uid") String uid) throws AppException {
        log.trace("getUserAggregateByUid with uid={}", uid);
       
        String userAggregateJson = userAggregateService.getUserAggregateByUidAsJson(applicationTokenId, userTokenId, uid);
        if (userAggregateJson == null) {
        	return Response.status(Response.Status.NOT_FOUND).entity("no user with uid" + uid).build();
        }
        return Response.ok(userAggregateJson).build();



    }
}
