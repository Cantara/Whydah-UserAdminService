package net.whydah.admin.auth;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.whydah.admin.createlogon.MailService;

@Path("/{applicationtokenid}")
@Component
public class MailingResource {

	private static final Logger log = LoggerFactory.getLogger(MailingResource.class);
    private final MailService mailService;

    
	@Autowired
	@Configure
	public MailingResource(MailService mailService) {
		this.mailService = mailService;
	}
 
    @Path("/send_scheduled_email")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON})
    public Response send_scheduled_email(@PathParam("applicationtokenid") String applicationtokenid,
                                         @FormParam("timestamp") String timestamp,
                                         @FormParam("emailaddress") String emailaddress,
                                         @FormParam("subject") String subject,
                                         @FormParam("emailMessage") String emailMessage,
                                         @FormParam("templateParams") String params,
                                         @FormParam("templateName") String templateName
    		) {
        log.info("send_scheduled_email - Try to schedule mail user with emailaddress {}", emailaddress);
        if(templateName!=null && templateName.length()>4) {
        	mailService.send(Long.parseLong(timestamp), emailaddress, subject, params, templateName);
        } else {
        	mailService.send(Long.parseLong(timestamp),emailaddress,subject,emailMessage);
        }
       
        return Response.ok("email scheduled").build();

    }   
}
