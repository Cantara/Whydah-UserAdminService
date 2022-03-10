package net.whydah.admin.createlogon;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.whydah.admin.ConfigValues;
import net.whydah.admin.email.EmailBodyGenerator;
import net.whydah.admin.email.IMailSender;
import net.whydah.admin.email.MailSender;
import net.whydah.admin.email.msgraph.MsGraphMailSender;

import org.constretto.ConstrettoConfiguration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MailService {


	private static final Logger log = getLogger(MailService.class);
	private final IMailSender mailSender;

	Toolkit toolkit;
	Timer timer;
	String toEmail;
	String mailMessage;
	String subject;
	
    
    private final ConstrettoConfiguration configuration;
    private final EmailBodyGenerator bodyGenerator;

	@Autowired
	@Configure
	public MailService(ConstrettoConfiguration configuration, EmailBodyGenerator bodyGenerator) {
		if(!ConfigValues.get("email.smtp.app.clientid", "").isEmpty() &&
				!ConfigValues.get("email.smtp.app.fromaddress", "").isEmpty() &&
				!ConfigValues.get("email.smtp.app.clientsecret", "").isEmpty()) {
			this.mailSender = new MsGraphMailSender(
					ConfigValues.getString("email.smtp.app.fromaddress"), 
					ConfigValues.getString("email.smtp.app.clientid"), 
					ConfigValues.get("email.smtp.app.tenantid", "common"), 
					ConfigValues.getString("email.smtp.app.clientsecret")				 
					);
		} else {
			this.mailSender = new MailSender();
		}
		
		this.configuration = configuration;
        this.bodyGenerator = bodyGenerator;
	}

	class RemindTask extends TimerTask {
		public void run() {
			log.debug("Task running." + new Date().toString());
			mailSender.send(toEmail,subject,mailMessage);
			toolkit.beep();
			timer.cancel(); //Not necessary because we call System.exit
		}
	}
	
	public void send(long timestamp, String toEmail, String subject, String templateParamsInJson, String templateName)  {


		this.toEmail = toEmail;	
		ObjectMapper mapper = new ObjectMapper();	
		try {
			if(subject==null || subject.equals("")) {
				this.subject = configuration.evaluateToString("email.subject." + templateName);
			}
			
			this.mailMessage = bodyGenerator.createBody(templateName, mapper.readValue(templateParamsInJson, Map.class));
			
			toolkit = Toolkit.getDefaultToolkit();
			timer = new Timer();

			long milliseconds = timestamp - new Date().getTime();
			log.debug("Milliseconds:{}", milliseconds);
			timer.schedule(new RemindTask(), milliseconds);
			
		} catch (Exception e) {
            log.error("Failed to send mail to {}. Reason {}", toEmail, e.getMessage());
        }
		
		


	}

	public void send(long timestamp, String toEmail, String subject, String mailMessage)  {


		this.toEmail = toEmail;
		this.mailMessage = mailMessage;
		this.subject=subject;

		toolkit = Toolkit.getDefaultToolkit();
		timer = new Timer();

		long milliseconds = timestamp - new Date().getTime();
		log.debug("Milliseconds:{}", milliseconds);
		timer.schedule(new RemindTask(), milliseconds);


	}

}
