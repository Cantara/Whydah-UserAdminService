package net.whydah.admin.security;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-07-06
 */
public class SecurityFilterTest {
    private SecurityFilter securityFilter;

    @BeforeClass
    public void setup() throws ServletException {
        securityFilter = new SecurityFilter("stsUrlNotWorking");
    }

    @Test
    public void testHealthEndpoint() {
        assertNull(securityFilter.authenticateAndAuthorizeRequest("/health"));
    }

    @Test(enabled = false)  //disable integration with STS is not enable yet in SecurityFilter.
    public void testAuthenticateApplication() {
        Integer errorCodeOrNull = securityFilter.authenticateAndAuthorizeRequest("/applicationTokenId/userTokenId/somePath");
        assertEquals(errorCodeOrNull, new Integer(HttpServletResponse.SC_UNAUTHORIZED));
    }
}
