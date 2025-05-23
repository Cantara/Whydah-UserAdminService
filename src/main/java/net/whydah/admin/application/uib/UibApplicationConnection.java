package net.whydah.admin.application.uib;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.admin.errorhandling.AppException;
import net.whydah.admin.security.UASCredentials;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.application.types.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Component
public class UibApplicationConnection {
	private static final Logger log = LoggerFactory.getLogger(UibApplicationConnection.class);

	private final String userIdentityBackendUri;
	private final UASCredentials uasCredentials;

	@Autowired
	public UibApplicationConnection(@Value("${useridentitybackend}") String uibUrl, UASCredentials uasCredentials) {
		this.uasCredentials = uasCredentials;
		this.userIdentityBackendUri=uibUrl;
	}

	public Response createApplication(String applicationTokenId, String userTokenId, String applicationJson) {

		WebTarget uib = getUIBWebTarget();
		WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("application");
		Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).post(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
		UibApplicationsConnection.clearCache();
		return copyResponse(responseFromUib);
	}

	public Response getApplication(String applicationTokenId, String userTokenId, String applicationId) throws AppException {
		WebTarget uib = getUIBWebTarget();
		WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("application").path(applicationId);
		Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
		return copyResponse(responseFromUib);
	}

	public Application getApplication2(String applicationTokenId, String userTokenId, String applicationId) throws AppException {
		WebTarget uib = getUIBWebTarget();
		WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("application").path(applicationId);
		Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).get();
		Application application = ApplicationMapper.fromJson(responseFromUib.readEntity(String.class));
		return application;
	}

	public Response updateApplication(String applicationTokenId, String userTokenId, String applicationId, String applicationJson) {
		WebTarget uib = getUIBWebTarget();
		WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("application").path(applicationId);
		Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).put(Entity.entity(applicationJson, MediaType.APPLICATION_JSON));
		UibApplicationsConnection.clearCache();
		return copyResponse(responseFromUib);
	}

	public Response deleteApplication(String applicationTokenId, String userTokenId, String applicationId) {

		WebTarget uib = getUIBWebTarget();
		WebTarget webResource = uib.path(applicationTokenId).path(userTokenId).path("application").path(applicationId);
		Response responseFromUib = webResource.request(MediaType.APPLICATION_JSON).header(uasCredentials.APPLICATION_CREDENTIALS_HEADER_XML, uasCredentials.getApplicationCredentialsXmlEncoded()).delete();
		UibApplicationsConnection.clearCache();
		return copyResponse(responseFromUib);
	}

	private WebTarget getUIBWebTarget() {
		Client client = ClientBuilder.newClient();
		log.info("Connection to UserIdentityBackend on {}" , userIdentityBackendUri);
		WebTarget uib = client.target(userIdentityBackendUri);
		return uib;
	}

	private Response copyResponse(Response responseFromUib) {
		Response.ResponseBuilder rb = Response.status(responseFromUib.getStatusInfo());
		if (responseFromUib.hasEntity()) {
			rb.entity(responseFromUib.getEntity());
		}
		return rb.build();
//		return responseFromUib;
	}
}