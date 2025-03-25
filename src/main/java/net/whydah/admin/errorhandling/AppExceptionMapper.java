package net.whydah.admin.errorhandling;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {

	public Response toResponse(AppException ex) {
		
		return Response.status(ex.getStatus())
				.entity(ExceptionConfig.handleSecurity(new ErrorMessage(ex)).toString())
				.type(MediaType.APPLICATION_JSON).
				build();
	}

}
