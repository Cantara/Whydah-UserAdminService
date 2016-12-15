package net.whydah.admin.useraggregate;

import net.whydah.admin.user.UserService;
import net.whydah.admin.user.uib.*;
import net.whydah.errorhandling.AppException;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import net.whydah.sso.user.types.UserAggregate;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Path("/{applicationtokenid}/{userTokenId}/useraggregate")
@Controller
public class UserAggregateResource {
	private static final Logger log = LoggerFactory.getLogger(UserAggregateResource.class);
	UserService userService;




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
	 * @throws AppException 
	 */
	@POST
	@Path("/")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response createUserAggregate(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
			String userAggregateJson, @Context Request request) throws AppException {
		log.trace("createUser is called with userAggregateJson={}", userAggregateJson);

		UserAggregate userAggregate = UserAggregateMapper.fromJson(userAggregateJson);
		UserIdentity createdUser = userService.createUser(applicationTokenId, userTokenId, userAggregateJson);
		if (createdUser != null) {
			UserAggregate createdUserAggregate = UserAggregateMapper.fromJson(UserIdentityMapper.toJson(createdUser));
			List<UserApplicationRoleEntry> roleList = userAggregate.getRoleList();
			for (UserApplicationRoleEntry role : roleList) {
				userService.addUserRole(applicationTokenId, userTokenId, createdUser.getUid(), role);
			}
			createdUserAggregate.setRoleList(roleList);

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
			@PathParam("uid") String uid, @Context Request req) throws AppException {
		log.trace("getUserAggregate is called with uid={}. ", uid);
		UserAggregate userAggregate = null;
		userAggregate =userService.getUserAggregateByUid(applicationTokenId, userTokenId, uid);
		return Response.ok(UserAggregateMapper.toJson(userAggregate)).build();

	}


	@GET
	@Path("/ping/pong")
	@Produces(MediaType.TEXT_HTML)
	@Deprecated //Not used by ansible scrips anymore as of 2015-07-06
	public Response ping() {
		return Response.ok("pong").build();
	}


}
