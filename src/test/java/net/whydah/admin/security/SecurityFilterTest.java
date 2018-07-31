package net.whydah.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import java.util.regex.Pattern;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class SecurityFilterTest {
    private SecurityFilter securityFilter;
    private static final Logger log = LoggerFactory.getLogger(SecurityFilterTest.class);


    @BeforeClass
    public void setup() throws ServletException {
        securityFilter = new SecurityFilter("test.com", null, null, null);
    }

    @Test
    public void testOpenEndpoints() {
        assertNull(securityFilter.authenticateAndAuthorizeRequest("/health"));
    }


    @Test
    public void testPathsWithoutAuth() {
        assertNull(securityFilter.authenticateAndAuthorizeRequest("/health"));

    }


    @Test
    public void testPathsWithoutUserTokenIdOK() {
        assertTrue(getFilterResult("/find/applications/9999") == null);

    }

    private Integer getFilterResult(String path) {
        String applicationAuthPattern = "/application/auth";
        String userLogonPattern = "/auth/logon/user";           //LogonController, same as authenticate/user in UIB.
        String userAuthPattern = "/authenticate/user(|/.*)";    //This is the pattern used in UIB
        String pwResetAuthPattern = "/auth/password/reset/username/(.*?)";
        String pwPattern = "/user/.+/(reset|change)_password";
        String userSignupPattern = "/signup/user";
        String listApplicationsPattern = "/applications";
        String findApplicationsPattern = "/applications/find/(.*?)";
        String findApplicationsPattern2 = "/find/applications/(.*?)";
        String hasUASAccess = "/hasUASAccess";
        String send_scheduled_email = "/send_scheduled_email";

        String userPWEnabeled = "/user/.+/password_login_enabled";
        String[] patternsWithoutUserTokenId = {applicationAuthPattern, userLogonPattern, pwResetAuthPattern, pwPattern, userAuthPattern, userSignupPattern, listApplicationsPattern, hasUASAccess, send_scheduled_email, userPWEnabeled, findApplicationsPattern, findApplicationsPattern2};
        for (String pattern : patternsWithoutUserTokenId) {
            if (Pattern.compile(pattern).matcher(path).matches()) {
                log.debug("{} was matched to {}. SecurityFilter passed.", path, pattern);
                return null;
            }
        }
        return 1;

    }
}
