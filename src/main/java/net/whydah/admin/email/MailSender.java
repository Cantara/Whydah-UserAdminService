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
import java.util.Properties;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a>
 */
@Component
public class MailSender {
    private static final Logger log = LoggerFactory.getLogger(MailSender.class);
    public static  String FROM_ADDRESS = "notworking@whydah.net";


    private static final boolean SMTP_AUTH = true;
    private static final boolean SMTP_STARTTTLS_ENABLE = true;
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
//    private static final String SMTP_PORT = "587";


    private final String smtpUsername;
    private final String smtpPassword;

    @Autowired
    @Configure
    public MailSender(@Configuration("gmail.username") String smtpUsername,
                      @Configuration("gmail.password") String smtpPassword) {
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        // https://accounts.google.com/DisplayUnlockCaptcha
    }

    /*
    public static void main(String[] args) {
        new MailSender().send("erik@cantara.no", "Whydah MailSender test", "Email via Gmail test");
    }
    */

    public void send(String recipients, String subject, String body) {
        log.debug("Sending email to recipients={}, subject={}, body={}", recipients, subject, body);
        FROM_ADDRESS=smtpUsername;

        //Gmail props
        Properties smtpProperties = new Properties();
        //smtpProperties.put("mail.smtp.auth", SMTP_AUTH);
        //smtpProperties.put("mail.smtp.starttls.enable", SMTP_STARTTTLS_ENABLE);
        //smtpProperties.put("mail.smtp.host", SMTP_HOST);
        //smtpProperties.put("mail.smtp.port", SMTP_PORT);

        smtpProperties.put("mail.smtp.host", SMTP_HOST);
        smtpProperties.put("mail.smtp.socketFactory.port", SMTP_PORT);
        smtpProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        smtpProperties.put("mail.smtp.auth", "true");
        smtpProperties.put("mail.smtp.port", "465");


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
            message.setFrom(new InternetAddress(FROM_ADDRESS));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            message.setSubject(subject);

            message.setContent(body, "text/html; charset=utf-8");
//            message.setText(body);
            Transport.send(message);
            log.info("Sent email to " + recipients);
        } catch (MessagingException e) {
            String smtpInfo = "Error sending email. SMTP_HOST=" + SMTP_HOST + ", SMTP_PORT=" + SMTP_PORT + ", smtpUsername=" + smtpUsername + ", subject=" + subject;
            if (e.getCause() instanceof AuthenticationFailedException) {
                log.warn("Failed to send mail due to missconfiguration? Reason {}", e.getCause().getMessage());
            }
          throw new RuntimeException(smtpInfo, e);
        }
    }
}
