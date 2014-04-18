package net.whydah.admin.user.uib;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Created by baardl on 17.04.14.
 */
public class UserAgregateTest {

    private String userAgregateTemplate = "{\"uid\":\"uid\",\"username\":\"usernameABC\",\"firstName\":\"firstName\",\"lastName\":\"lastName\",\"personRef\":\"personRef\",\"email\":\"email\",\"cellPhone\":\"12345678\",\"password\":\"password\",\"roles\":[{\"applicationId\":\"applicationId\",\"applicationName\":\"applicationName\",\"organizationId\":\"organizationId\",\"organizationName\":\"organizationName\",\"applicationRoleName\":\"roleName\",\"applicationRoleValue\":\"email\",\"id\":null},{\"applicationId\":\"applicationId123\",\"applicationName\":\"applicationName123\",\"organizationId\":\"organizationId123\",\"organizationName\":\"organizationName123\",\"applicationRoleName\":\"roleName123\",\"applicationRoleValue\":\"roleValue123\",\"id\":null}]}\n";

    @Test
    public void buildUserAggregate() throws Exception {
        UserAggregateRepresentation userAggregate = UserAggregateRepresentation.fromJson(userAgregateTemplate);
        assertNotNull(userAggregate);
        assertEquals("uid", userAggregate.getUid());
        List<RoleRepresentation> userRoles = userAggregate.getRoles();
        assertNotNull(userRoles);
        //TODO bli: work in progress
    }
}
