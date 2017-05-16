package net.whydah.admin.useraggregate;

import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.user.UserService;
import net.whydah.admin.user.uib.*;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.mappers.UserRoleMapper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.whydah.sso.user.types.UserAggregate;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	@PUT
	@Path("/")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response updateUserAggregate(@PathParam("applicationtokenid") String applicationTokenId, @PathParam("userTokenId") String userTokenId,
			String userAggregateJson, @Context Request request) throws AppException, JsonProcessingException, IOException {
		log.trace("updateUserAggregate is called with userAggregateJson={}", userAggregateJson);

		//TODO: this line contains a bug
		//UserAggregate workingUserAggregate = UserAggregateMapper.fromJson(userAggregateJson);
		ObjectMapper om = new ObjectMapper();
		JsonNode sNode = om.readTree(userAggregateJson);
		//Have to do manually for now
		String uid = sNode.get("uid").textValue();
		String personRef =  sNode.get("personRef").textValue();
		String username = sNode.get("username").textValue();
		String firstName = sNode.get("firstName").textValue();
		String lastName = sNode.get("lastName").textValue();
		String email = sNode.get("email").textValue();
		String cellPhone = sNode.get("cellPhone").textValue();

		UserAggregate workingUserAggregate = new UserAggregate(uid, username, firstName, lastName, personRef, email, cellPhone);

		if(sNode.has("roles")){
			List<UserApplicationRoleEntry> roles = UserRoleMapper.fromJsonAsList(sNode.get("roles").toString());
			workingUserAggregate.setRoleList(roles);
		}

		Response updateResponse = userService.updateUserIdentity(applicationTokenId, userTokenId, workingUserAggregate.getUid(), userAggregateJson);
		if (updateResponse != null && updateResponse.getStatus()==200) {
			String oldRoles = userService.getRolesAsJson(applicationTokenId, userTokenId, workingUserAggregate.getUid());
			if(oldRoles!=null){
				for(UserApplicationRoleEntry entry : UserRoleMapper.fromJsonAsList(oldRoles)){
					userService.deleteUserRole(applicationTokenId, userTokenId, workingUserAggregate.getUid(), entry.getId());
				}
			}
			for (UserApplicationRoleEntry role : workingUserAggregate.getRoleList()) {
				userService.addUserRole(applicationTokenId, userTokenId, workingUserAggregate.getUid(), role);		
			}
			
//			String oldRoles = userService.getRolesAsJson(applicationTokenId, userTokenId, workingUserAggregate.getUid());
//			Map<String, UserApplicationRoleEntry> oldRoleMap = new HashMap<String, UserApplicationRoleEntry>();
//			Map<String, UserApplicationRoleEntry> newRoleMap = new HashMap<String, UserApplicationRoleEntry>();
//			
//			if(oldRoles!=null){
//				for(UserApplicationRoleEntry entry : UserRoleMapper.fromJsonAsList(oldRoles)){
//					oldRoleMap.put(entry.getId(), entry);
//				}
//			}
//			//create new roles
//			List<UserApplicationRoleEntry> roleList = workingUserAggregate.getRoleList();
//			for (UserApplicationRoleEntry role : roleList) {
//				
//				newRoleMap.put(role.getId(), role);
//				
//				if(oldRoleMap.containsKey(role.getId())){
//					userService.updateUserRole(applicationTokenId, userTokenId, workingUserAggregate.getUid(), role);
//				} else {
//					userService.addUserRole(applicationTokenId, userTokenId, workingUserAggregate.getUid(), role);
//				}
//				
//			}
//			
//			//remove roles which are not existing in the new list
//			for(String roleId : new ArrayList<String>(oldRoleMap.keySet())){
//				if(!newRoleMap.containsKey(roleId)){
//					userService.deleteUserRole(applicationTokenId, userTokenId, workingUserAggregate.getUid(), roleId);
//				}
//			}

			return Response.ok(UserAggregateMapper.toJson(workingUserAggregate)).build();
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
