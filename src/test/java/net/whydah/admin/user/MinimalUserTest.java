package net.whydah.admin.user;

import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserIdentity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by baardl on 30.09.15.
 */
public class MinimalUserTest {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @Test
    public void createFromJson() throws Exception {
        String userJson = """
                
                {"username":"helloMe", "firstName":"hello", "lastName":"meLastname", "personRef":"", "email":"hello.me@example.com", "cellPhone":"+47 90221133"}\
                """;
        UserIdentity minimalUser = UserIdentityMapper.fromUserIdentityWithNoIdentityJson(userJson);
        assertNotNull(minimalUser);
        assertEquals(minimalUser.getUsername(),"helloMe");
        assertEquals(minimalUser.getFirstName(),"hello");
        assertEquals(minimalUser.getLastName(), "meLastname");
        assertEquals(minimalUser.getPersonRef(),"");
        assertEquals(minimalUser.getEmail(),"hello.me@example.com");
        assertEquals(minimalUser.getCellPhone(),"+47 90221133");
    }
}