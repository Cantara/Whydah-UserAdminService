package net.whydah.admin.email;

import org.constretto.ConstrettoConfiguration;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 18.08.13
 * @Deprecated Functionallity is moved to UserAdminService
 * Send reset password email to user.
 */
@Service
public class PasswordSender {
    private static final Logger log = LoggerFactory.getLogger(PasswordSender.class);
    private final ConstrettoConfiguration configuration;

    private static final String RESET_PASSWORD_SUBJECT = "Whydah password reset request";
    private static final String CHANGE_PASSWORD_PATH = "changepassword/";
    private final String ssoLoginServiceUrl;

    private final EmailBodyGenerator bodyGenerator;
    private final MailSender mailSender;

    @Autowired
    @Configure
    public PasswordSender(ConstrettoConfiguration configuration, EmailBodyGenerator bodyGenerator, MailSender mailSender, @Configuration("ssologinservice") String ssoLoginServiceUrl) {
        this.configuration = configuration;
        this.bodyGenerator = bodyGenerator;
        this.mailSender = mailSender;
        this.ssoLoginServiceUrl = ssoLoginServiceUrl;
    }

    public boolean sendResetPasswordEmail(String username, String token, String userEmail) {
        boolean messageSent = false;
        String resetUrl = ssoLoginServiceUrl + CHANGE_PASSWORD_PATH + token;
        log.info("Sending resetPassword email for user {} to {}, token={}", username, userEmail, token);
        String body = bodyGenerator.resetPassword(resetUrl, username);
        try {
            String reset_subject = configuration.evaluateToString("email.subject.PasswordResetEmail.ftl");
            mailSender.send(userEmail, reset_subject != null ? reset_subject : RESET_PASSWORD_SUBJECT, body);
            messageSent = true;
        } catch (Exception e) {
            log.error("Failed to send passwordResetMail to {}. Reason {}", userEmail, e.getMessage());
        }
        return messageSent;
    }


    public boolean sendResetPasswordEmail(String username, String token, String userEmail, String templateName) {
        boolean messageSent = false;
        try {
            String resetUrl = ssoLoginServiceUrl + CHANGE_PASSWORD_PATH + token;
            log.info("Sending resetPassword email for user {} to {}, token={}, templateName={}", username, userEmail, token, templateName);
            
            if (templateName == null || templateName.length() < 10) {
                String reset_subject = configuration.evaluateToString("email.subject.PasswordResetEmail.ftl");
                String system_name = configuration.evaluateToString("email.systemname.PasswordResetEmail.ftl");
                String body = bodyGenerator.resetPassword(resetUrl, username, system_name !=null? system_name: "Whydah system", templateName);
                log.debug(body);
                mailSender.send(userEmail, reset_subject != null ? reset_subject : RESET_PASSWORD_SUBJECT, body);
            } else {
                String template_subject = configuration.evaluateToString("email.subject." + templateName);
                String system_name = configuration.evaluateToString("email.systemname."+ templateName);
                String body = bodyGenerator.resetPassword(resetUrl, username, system_name !=null? system_name: "Whydah system", templateName);
                log.debug(body);
                mailSender.send(userEmail, template_subject, body);
            }
            messageSent = true;
        } catch (Exception e) {
            log.error("Failed to send passwordResetMail to {}. Reason {}.  Stacktrace {}", userEmail, e.getMessage(), e);
        }
        return messageSent;
    }
}
