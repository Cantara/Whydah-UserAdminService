package net.whydah.admin.createlogon;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Toolkit;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.whydah.admin.SpringProperties;
import net.whydah.admin.email.EmailBodyGenerator;
import net.whydah.admin.email.IMailSender;
import net.whydah.admin.email.MailSender;
import net.whydah.admin.email.msgraph.MsGraphMailSender;

@Service
public class MailService {

	private static final Logger log = getLogger(MailService.class);
	private final IMailSender mailSender;

	private final EmailBodyGenerator bodyGenerator;
	private final AtomicLong nextTaskId = new AtomicLong(1);
	private final Map<Long, RemindTask> taskById = new ConcurrentHashMap<>();
	private final Timer timer = new Timer("mail-service-timer");
	private final String defaultSubject;
	
	SpringProperties properties;

	@Autowired
	public MailService(EmailBodyGenerator bodyGenerator, SpringProperties properties) {
		this.properties = properties;
		
		if(!properties.get("email.smtp.app.clientid", "").isEmpty() &&
				!properties.get("email.smtp.app.fromaddress", "").isEmpty() &&
				!properties.get("email.smtp.app.clientsecret", "").isEmpty()) {
			log.debug("Choosing IMailSender: " + MsGraphMailSender.class.getName());
			this.mailSender = new MsGraphMailSender(
					properties.getString("email.smtp.app.fromaddress"),
					properties.getString("email.smtp.app.clientid"),
					properties.get("email.smtp.app.tenantid", "common"),
					properties.getString("email.smtp.app.clientsecret")
			);
		} else {
			log.debug("Choosing IMailSender: " + MailSender.class.getName());
			this.mailSender = new MailSender(properties.get("email.smtp.username",""),
					properties.get("email.smtp.password",""),
					properties.get("email.smtp.host",""),
					properties.get("email.smtp.port",""),
					properties.get("email.smtp.from.personalname",""),
					properties.get("email.smtp.from.address",""));
		}

		this.bodyGenerator = bodyGenerator;
		this.defaultSubject = properties.get("email.subject:Whydah","");
	}

	public IMailSender getEmailSender() {
		return mailSender;
	}

	class RemindTask extends TimerTask {
		final Long taskId;
		final String toEmail;
		final String mailMessage;
		final String subject;

		public RemindTask(String toEmail, String mailMessage, String subject) {
			this.taskId = nextTaskId.getAndIncrement();
			this.toEmail = toEmail;
			this.mailMessage = mailMessage;
			this.subject = subject;
		}

		public void schedule(long delay) {
			taskById.put(taskId, this);
			timer.schedule(this, delay);
		}

		public void run() {
			try {
				log.debug("Task running." + new Date().toString());
				mailSender.send(toEmail, subject, mailMessage);
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					toolkit.beep(); // TODO is this really something we want in the production code?
				}
			} finally {
				taskById.remove(taskId);
			}
		}
	}

	public void send(long timestamp, String toEmail, String subject, String templateParamsInJson, String templateName)  {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String effectiveSubject = subject;
			if(effectiveSubject==null || effectiveSubject.equals("")) {
				effectiveSubject = properties.getString("email.subject." + templateName);
				if (effectiveSubject == null) {
					effectiveSubject = defaultSubject;
				}
			}

			String mailMessage = bodyGenerator.createBody(templateName, mapper.readValue(templateParamsInJson, Map.class));

			long milliseconds = timestamp - new Date().getTime();
			log.debug("Milliseconds:{}", milliseconds);

			RemindTask task = new RemindTask(toEmail, mailMessage, effectiveSubject);
			task.schedule(milliseconds);

		} catch (Exception e) {
			log.error("Failed to send mail to {}. Reason {}", toEmail, e.getMessage());
		}
	}

	public void send(long timestamp, String toEmail, String subject, String mailMessage)  {
		long milliseconds = timestamp - new Date().getTime();
		log.debug("Milliseconds:{}", milliseconds);
		RemindTask task = new RemindTask(toEmail, mailMessage, subject);
		task.schedule(milliseconds);
	}
}