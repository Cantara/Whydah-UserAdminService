package net.whydah.admin.application;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhyDahRoleCheckUtil;
import net.whydah.admin.application.uib.UibApplicationConnection;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.admin.security.UASCredentials;
import net.whydah.admin.user.uib.UibUserConnection;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.application.types.Application;
import net.whydah.sso.application.types.ApplicationSecurity;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by HUY
 */
@Service
public class ApplicationService {
	private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
	private final UibApplicationConnection uibApplicationConnection;
	private WhyDahRoleCheckUtil adminChecker;
	public WhyDahRoleCheckUtil getAdminChecker(){
		return adminChecker;
	}

	@Autowired
	@Configure
	public ApplicationService(UibApplicationConnection uibApplicationConnection, WhyDahRoleCheckUtil adminChecker) {
		this.uibApplicationConnection = uibApplicationConnection;
		this.adminChecker = adminChecker;
	}

	public Response createApplication(String applicationTokenId, String userTokenId, String applicationJson) throws AppException{
		log.trace("create is called with applicationJson={}", applicationJson);
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			String validatedAppJson = validateApplicationJson(applicationTokenId, applicationJson);
			if(validatedAppJson==null){
				throw AppExceptionCode.APP_INVALID_JSON_FORMAT_8001;
			}
			Response responseFromUib = uibApplicationConnection.createApplication(applicationTokenId, userTokenId, validatedAppJson);
			if (responseFromUib.getStatus() == 400) {
				throw AppExceptionCode.APP_INVALID_JSON_FORMAT_8001;
			} else if (responseFromUib.getStatus() == 500) {
				throw new WebApplicationException("Unexpected error from UIB", 500);
			}
			return responseFromUib;
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public Response getApplication(String applicationTokenId, String userTokenId, String applicationId) throws AppException {
		log.trace("getApplication is called with applicationId={}", applicationId);
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			Response responseFromUib = uibApplicationConnection.getApplication(applicationTokenId, userTokenId, applicationId);
			if (responseFromUib.getStatus() == 200) {

				String jsonResult = responseFromUib.readEntity(String.class);
				log.trace("Received jsonResult {}", jsonResult);
				return Response.ok(jsonResult).build();

			} else {
				if (responseFromUib.getStatus() == 404) {
					//application not found
					throw AppExceptionCode.APP_NOTFOUND_8002;
				} else {
					//server error
					throw new WebApplicationException("Unexpected error from UIB", 500);
				}
			}
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}

	}

	public Response updateApplication(String applicationTokenId, String userTokenId, String applicationId, String applicationJson) throws AppException {
		log.trace("updateApplication applicationId={}, applicationJson={}", applicationId, applicationJson);
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			
			String validatedAppJson = validateApplicationJson(applicationTokenId, applicationJson);
			Response responseFromUib = uibApplicationConnection.updateApplication(applicationTokenId, userTokenId, applicationId, validatedAppJson);
			if (responseFromUib.getStatus() == 204) {
				return responseFromUib;
			} else {
				if (responseFromUib.getStatus() == 404) {
					//application not found
					throw AppExceptionCode.APP_NOTFOUND_8002;
				} else if (responseFromUib.getStatus() == 400) {
					//application not found
					throw AppExceptionCode.APP_INVALID_JSON_FORMAT_8001;
				} else {
					//server error
					throw new WebApplicationException("Unexpected error from UIB", 500);

				}
			}

		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}

	}

	public Response deleteApplication(String applicationTokenId, String userTokenId, String applicationId) throws AppException {
		log.trace("deleteApplication is called with applicationId={}", applicationId);
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {
			
			Response responseFromUib = uibApplicationConnection.deleteApplication(applicationTokenId, userTokenId, applicationId);
			if (responseFromUib.getStatus() == 204) {
				return responseFromUib;
			} else {
				if (responseFromUib.getStatus() == 404) {
					//application not found
					throw AppExceptionCode.APP_NOTFOUND_8002;
				} else {
					//server error
					throw new WebApplicationException("Unexpected error from UIB", 500);
				}
			}
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public String validateApplicationJson(String applicationTokenId, String appJson){
		if(adminChecker.isInternalWhyDahAdminApp(applicationTokenId)){
			return appJson;
		}
		Application app = ApplicationMapper.fromJson(appJson);
		if(app.getSecurity().isWhydahAdmin()){
			log.warn("Attempt to set whydahAdmin flag to true from 3rd party app. Set back to false");
			//TODO: uncomment this line below after we edit the json file in UASWA
			//app.getSecurity().setWhydahAdmin(false);//Do not allow 3rd party having this flag
		}
		return ApplicationMapper.toJson(app);
	}




}
