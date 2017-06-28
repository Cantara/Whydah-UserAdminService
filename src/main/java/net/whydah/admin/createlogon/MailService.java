package net.whydah.admin.createlogon;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Toolkit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.whydah.admin.email.MailSender;

import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailService {


	private static final Logger log = getLogger(MailService.class);
	private final MailSender mailSender;

	Toolkit toolkit;
	Timer timer;
	String toEmail;
	String mailMessage;
	String subject;

	@Autowired
	@Configure
	public MailService(MailSender sender) {
		this.mailSender = sender;
	}

	class RemindTask extends TimerTask {
		public void run() {
			log.debug("Task running." + new Date().toString());
			mailSender.send(toEmail,subject,mailMessage);
			toolkit.beep();
			timer.cancel(); //Not necessary because we call System.exit
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
