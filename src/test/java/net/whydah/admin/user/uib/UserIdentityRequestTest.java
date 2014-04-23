package net.whydah.admin.user.uib;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class UserIdentityRequestTest {
    @Test
    public void testFromXML() throws Exception {

    }

    @Test
    public void testToJson() throws Exception {
        UserIdentityRequest userIdentityRequest = new UserIdentityRequest("username", "fn", "ln", "pR", "@","1234");
        assertNotNull(userIdentityRequest.toJson());

    }
}
