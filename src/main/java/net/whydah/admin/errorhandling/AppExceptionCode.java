package net.whydah.admin.errorhandling;

import jakarta.ws.rs.core.Response.Status;




public class AppExceptionCode {
   
	// EXCEPTIONS
	public static final AppException STSAPP_ILLEGAL_8000 = new AppException(Status.FORBIDDEN, 8000, "Illegal Token Service.", "Illegal Token Service.", "");
	public static final AppException APP_INVALID_JSON_FORMAT_8001 = new AppException(Status.BAD_REQUEST, 8001, "Invalid json format.", "Invalid json format.", "");
	public static final AppException APP_NOTFOUND_8002 = new AppException(Status.BAD_REQUEST, 8002, "Application cannot be found.", "Application cannot be found.", "");
	public static final AppException APP_UNABLE_TO_RESET_PASSWORD_8004 = new AppException(Status.ACCEPTED, 8004, "Unable to send reset password notification to user", "", "");
	public static final AppException APP_NOCONTENT_8003 = new AppException(Status.NO_CONTENT, 8003, "No content", "", "");
	
	
	
	//MISC
	public static final AppException MISC_MISSING_PARAMS_9998 = new AppException(Status.BAD_REQUEST, 9998, "Missing required parameters", "Missing required parameters", "");
	public static final AppException MISC_BadRequestException_9997 = new AppException(Status.BAD_REQUEST, 9997, "BadRequestException", "", "");
	public static final AppException MISC_OperationFailedException_9996 = new AppException(Status.INTERNAL_SERVER_ERROR, 9996, "AuthenticationFailedException", "", "");
	public static final AppException MISC_ConflictException_9995 = new AppException(Status.INTERNAL_SERVER_ERROR, 9995, "ConflictException", "", "");
	public static final AppException MISC_RuntimeException_9994 = new AppException(Status.INTERNAL_SERVER_ERROR, 9994, "RuntimeException", "", "");
	public static final AppException MISC_FORBIDDEN_9993 = new AppException(Status.FORBIDDEN, 9993, "Forbidden", "", "");
	public static final AppException MISC_NotAuthorizedException_9992 = new AppException(Status.UNAUTHORIZED, 9992, "NotAuthorizedException", "", "");
	public static final AppException MISC_NOT_ACCEPTABLE_9991 = new AppException(Status.NOT_ACCEPTABLE, 9991, "Not Acceptable", "", "");
}
