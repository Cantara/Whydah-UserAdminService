package net.whydah.admin.email;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a>
 */
@Component
public class MailSender {
	private static final Logger log = LoggerFactory.getLogger(MailSender.class);



	private final String smtpUsername;
	private final String smtpPassword;
	private final String smtpHost;
	private final String smtpPort;
	private final String smtpFromPersonalname;
	private final String smtpFromAddress;
	

//			email.smtp.host=smtp.gmail.com
//			email.smtp.port=465
//			email.smtp.username=whydahdev.cantara@gmail.com
//			email.smtp.password=440Cantara440Dev
//			email.smtp.from.address=whydahdev.cantara@gmail.com
//			email.smtp.from.personalname=Whydah

	@Autowired
	@Configure
	public MailSender(@Configuration("email.smtp.host") String host,
			@Configuration("email.smtp.port") String port,
			@Configuration("email.smtp.username") String smtpUsername,
			@Configuration("email.smtp.password") String smtpPassword,
			@Configuration("email.smtp.from.address") String address,
			@Configuration("email.smtp.from.personalname") String personalname
			) {
		this.smtpUsername = smtpUsername;
		this.smtpPassword = smtpPassword;
		this.smtpHost = host;
		this.smtpPort = port;
		this.smtpFromPersonalname = personalname;
		this.smtpFromAddress = address;
		// https://accounts.google.com/DisplayUnlockCaptcha
	}

	/*
    public static void main(String[] args) {
        new MailSender().send("erik@cantara.no", "Whydah MailSender test", "Email via Gmail test");
    }
	 */

	public void send(String recipients, String subject, String body) {
		log.debug("Sending email to recipients={}, subject={}, body={}", recipients, subject, body);
	
		Properties smtpProperties = new Properties();
	
		smtpProperties.put("mail.smtp.host", smtpHost);
		smtpProperties.put("mail.smtp.socketFactory.port", smtpPort);
		smtpProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		smtpProperties.put("mail.smtp.auth", "true");
		smtpProperties.put("mail.smtp.port", smtpPort);


		//Cantara smtp, will only work with @cantara-adresses
		/*
        Properties props = new Properties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "www.cantara.no");
        props.put("mail.smtp.port", "25");
        Session session = Session.getInstance(props);
		 */
		Session session = Session.getInstance(smtpProperties,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpUsername, smtpPassword);
			}
		});

		try {
			Message message = new MimeMessage(session);
			try {
				message.setFrom(new javax.mail.internet.InternetAddress(smtpFromAddress, smtpFromPersonalname));
			} catch (UnsupportedEncodingException e) {
				message.setFrom(new javax.mail.internet.InternetAddress(smtpFromAddress));
			}
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
			message.setSubject(subject);
			message.setContent(body, "text/html; charset=utf-8");
			Transport.send(message);
			log.info("Sent email to " + recipients);
		} catch (MessagingException e) {
			String smtpInfo = "Error sending email. SMTP_HOST=" + smtpHost + ", SMTP_PORT=" + smtpPort + ", smtpUsername=" + smtpUsername + ", subject=" + subject;
			if (e.getCause() instanceof AuthenticationFailedException) {
				log.warn("Failed to send mail due to missconfiguration? Reason {}", e.getCause().getMessage());
			}
			throw new RuntimeException(smtpInfo, e);
		}
	}
}
