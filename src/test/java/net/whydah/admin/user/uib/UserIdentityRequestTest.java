package net.whydah.admin.user.uib;

import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserIdentity;
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
        UserIdentity userIdentityRequest = new UserIdentity("username", "firstn", "lastn", "pR", "me@cantara.no", "90001234");
        assertNotNull(UserIdentityMapper.toJson(userIdentityRequest));

    }
}
