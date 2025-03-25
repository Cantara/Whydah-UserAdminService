package net.whydah.admin.application;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.WhydahRoleCheckUtil;
import net.whydah.admin.application.uib.UibApplicationConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.errorhandling.AppExceptionCode;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.application.types.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static net.whydah.sso.util.LoggerUtil.first50;

/**
 * Created by HUY
 */
@Service
public class ApplicationService {
	private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
	private final UibApplicationConnection uibApplicationConnection;
	private WhydahRoleCheckUtil adminChecker;

	public WhydahRoleCheckUtil getAdminChecker() {
		return adminChecker;
	}

	@Autowired
	public ApplicationService(UibApplicationConnection uibApplicationConnection, WhydahRoleCheckUtil adminChecker) {
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
			return copyResponse(responseFromUib);
		} else {
			throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}
	}

	public Response getApplication(String applicationTokenId, String userTokenId, String applicationId) throws AppException {
		log.debug("getApplication is called with applicationId={}", applicationId);
		try {
			if (adminChecker.authorise(applicationTokenId, userTokenId)) {
				Response responseFromUib = uibApplicationConnection.getApplication(applicationTokenId, userTokenId, applicationId);
				log.debug("responseFromUib.status:{}", responseFromUib.getStatus());
				if (responseFromUib.getStatus() == 200) {

					String jsonResult = responseFromUib.readEntity(String.class);
					log.debug("Received jsonResult {}", jsonResult);
					return Response.ok(jsonResult).build();

				} else {
					log.debug("Received unexpected statusCode form UIB:{}", responseFromUib.getStatus());
					if (responseFromUib.getStatus() == 404) {
						//application not found
						throw AppExceptionCode.APP_NOTFOUND_8002;
					} else {
						//server error
						throw new WebApplicationException("Unexpected error from UIB", 500);
					}
				}
			} else {
				log.debug("adminChecker.authorise(applicationTokenId, userTokenId): false");
				throw AppExceptionCode.MISC_NotAuthorizedException_9992;
			}
		} catch (Exception e) {
			log.error("unable to handle response from UIB: {}", e);
			Application application = uibApplicationConnection.getApplication2(applicationTokenId, userTokenId, applicationId);
			String json = ApplicationMapper.toJson(application);
			log.debug("applicationJson {}", first50(json));
			return Response.ok(json).build();

//            throw AppExceptionCode.MISC_NotAuthorizedException_9992;
		}

	}

	public Response updateApplication(String applicationTokenId, String userTokenId, String applicationId, String applicationJson) throws AppException {
		log.trace("updateApplication applicationId={}, applicationJson={}", applicationId, applicationJson);
		if (adminChecker.authorise(applicationTokenId, userTokenId)) {

			String validatedAppJson = validateApplicationJson(applicationTokenId, applicationJson);
			Response responseFromUib = uibApplicationConnection.updateApplication(applicationTokenId, userTokenId, applicationId, validatedAppJson);
			if (responseFromUib.getStatus() == 204) {
				return copyResponse(responseFromUib);
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
				return copyResponse(responseFromUib);
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
		if (adminChecker.isUAWA(applicationTokenId)) {
			return appJson;
		}
		Application app = ApplicationMapper.fromJson(appJson);
		app.getSecurity().setWhydahAdmin(false);
		return ApplicationMapper.toJson(app);
	}

	private Response copyResponse(Response responseFromUib) {
		Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
		if (responseFromUib.hasEntity()) {
			rb.entity(responseFromUib.getEntity());
		}
		return rb.build();
	}
}