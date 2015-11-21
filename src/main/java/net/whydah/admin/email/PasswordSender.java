package net.whydah.admin.email;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Deprecated Functionallity is moved to UserAdminService
 * Send reset password email to user.
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 18.08.13
 */
@Service
public class PasswordSender {
    private static final Logger log = LoggerFactory.getLogger(PasswordSender.class);
    private static final String RESET_PASSWORD_SUBJECT = "Whydah password reset";
    private static final String CHANGE_PASSWORD_PATH = "changepassword/";
    private final String ssoLoginServiceUrl;

    private final EmailBodyGenerator bodyGenerator;
    private final MailSender mailSender;

    @Autowired
    @Configure
    public PasswordSender(EmailBodyGenerator bodyGenerator, MailSender mailSender, @Configuration("ssologinservice") String ssoLoginServiceUrl) {
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
            mailSender.send(userEmail, RESET_PASSWORD_SUBJECT, body);
            messageSent = true;
        } catch (Exception e) {
            log.info("Failed to send passwordResetMail to {}. Reason {}", userEmail, e.getMessage());
        }
        return messageSent;
    }
}