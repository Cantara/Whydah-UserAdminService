package net.whydah.admin.email;

import net.whydah.admin.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MailSender implements IMailSender {
	private static final Logger log = LoggerFactory.getLogger(MailSender.class);

	private final String smtpUsername;
	private final String smtpPassword;
	private final String smtpHost;
	private final String smtpPort;
	private final String smtpFromPersonalname;
	private final String smtpFromAddress;

	public MailSender() {
		this.smtpUsername = ConfigValues.getString("email.smtp.username");
		this.smtpPassword = ConfigValues.getString("email.smtp.password");
		this.smtpHost = ConfigValues.getString("email.smtp.host");
		this.smtpPort = ConfigValues.getString("email.smtp.port");
		this.smtpFromPersonalname = ConfigValues.getString("email.smtp.from.personalname");
		this.smtpFromAddress = ConfigValues.getString("email.smtp.from.address");

		log.info("email.smtp.host:" + smtpHost);
		log.info("email.smtp.port:" + smtpPort);
		log.info("email.smtp.username:" + smtpUsername);
		log.info("email.smtp.password:" + smtpPassword);
		log.info("email.smtp.from.address:" + smtpFromAddress);
		log.info("email.smtp.from.personalname:" + smtpFromPersonalname);
	}

	public void send(String recipients, String subject, String body) {
		log.debug("Sending email to recipients={}, subject={}, body={}", recipients, subject, body);
		log.info("If you are looking at the previous log statement and no mail get sent: try https://accounts.google.com/DisplayUnlockCaptcha");

		Properties smtpProperties = new Properties();

		smtpProperties.put("mail.smtp.host", smtpHost);
		smtpProperties.put("mail.smtp.socketFactory.port", smtpPort);
		smtpProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		smtpProperties.put("mail.smtp.auth", "true");
		smtpProperties.put("mail.smtp.port", smtpPort);
		smtpProperties.put("mail.debug", "true");

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