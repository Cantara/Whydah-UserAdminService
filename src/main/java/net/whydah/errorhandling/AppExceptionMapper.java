package net.whydah.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {

	public Response toResponse(AppException ex) {
		
		return Response.status(ex.getStatus())
				.entity(ExceptionConfig.handleSecurity(new ErrorMessage(ex)))
				.type(MediaType.APPLICATION_JSON).
				build();
	}

}
