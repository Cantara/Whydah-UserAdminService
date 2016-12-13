package net.whydah.errorhandling;

import javax.ws.rs.core.Response.Status;




public class AppExceptionCode {
   
	// EXCEPTIONS
	public static AppException STSAPP_ILLEGAL_8000 = new AppException(Status.FORBIDDEN, 8000, "Illegal Token Service.", "Illegal Token Service.", "");
	public static AppException APP_INVALID_JSON_FORMAT_8001 = new AppException(Status.BAD_REQUEST, 8001, "Invalid json format.", "Invalid json format.", "");
	public static AppException APP_NOTFOUND_8002 = new AppException(Status.BAD_REQUEST, 8002, "Application cannot be found.", "Application cannot be found.", "");
	public static AppException APP_CONFLICT_8003 = new AppException(Status.CONFLICT, 8003, "IllegalStateException.", "IllegalStateException.", "");
	
	//MISC
	public static AppException MISC_MISSING_PARAMS_9998 = new AppException(Status.BAD_REQUEST, 9998, "Missing required parameters","Missing required parameters",""); 
	
	
	
	
}
