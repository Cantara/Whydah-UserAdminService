package net.whydah.admin.extras;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.constretto.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;


public class ScheduledSendEMailTask {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    Toolkit toolkit;
    Timer timer;
    String toEmail;
    String mailMessage;
    String subject;


    private final String smtpUsername;
    private final String smtpPassword;

    private static final Logger log = LoggerFactory.getLogger(ScheduledSendEMailTask.class);

    @Autowired
    @Configure
    public ScheduledSendEMailTask(long timestamp, String toEmail, String subject, String mailMessage) {
        this.toEmail = toEmail;
        this.mailMessage = mailMessage;
        this.subject=subject;

        final ConstrettoConfiguration configuration = new ConstrettoBuilder()
                .createPropertiesStore()
                .addResource(Resource.create("classpath:useradminservice.properties"))
                .addResource(Resource.create("file:./useradminservice_override.properties"))
                .done()
                .getConfiguration();

        this.smtpUsername = configuration.evaluateToString("gmail.username");
        this.smtpPassword = configuration.evaluateToString("gmail.password");
        toolkit = Toolkit.getDefaultToolkit();
        timer = new Timer();
        //log.debug("timestamp{} - new Date().getTime(){}");
        long milliseconds = timestamp - new Date().getTime();
        log.debug("Milliseconds:{}", milliseconds);
        timer.schedule(new RemindTask(), milliseconds);
    }

    class RemindTask extends TimerTask {
        public void run() {
            log.debug("Task running." + new Date().toString());
            send(toEmail,subject,mailMessage);
            toolkit.beep();
            timer.cancel(); //Not necessary because we call System.exit
            //System.exit(0); //Stops the AWT thread (and everything else)
        }
    }


    public void send(String recipients, String subject, String body) {
        log.debug("Sending email to recipients={}, subject={}, body={}", recipients, subject, body);
        String FROM_ADDRESS = smtpUsername;

        //Gmail props
        Properties smtpProperties = new Properties();

        smtpProperties.put("mail.smtp.host", SMTP_HOST);
        smtpProperties.put("mail.smtp.socketFactory.port", SMTP_PORT);
        smtpProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        smtpProperties.put("mail.smtp.auth", "true");
        smtpProperties.put("mail.smtp.port", "465");

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