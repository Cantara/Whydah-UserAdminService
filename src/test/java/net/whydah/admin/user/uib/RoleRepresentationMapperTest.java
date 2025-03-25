package net.whydah.admin.user.uib;

import net.whydah.sso.user.helpers.UserRoleJsonPathHelper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * Created by baardl on 22.06.15.
 */
public class RoleRepresentationMapperTest {

    @Test
    public void testFromJson() throws Exception {

        UserApplicationRoleEntry role = UserRoleJsonPathHelper.getUserRoleFromJson(roleJson);
        assertNotNull(role);

    }

    private String roleJson = """
            [
                {
                    "roleId": "8bafa279-1615-4833-869e-106daaefa797",
                    "uid": "e365cccc-dc2b-4c79-a79c-d0ae3b79a45d",
                    "applicationId": "201",
                    "applicationRoleName": "testRoleName",
                    "applicationRoleValue": "true",
                    "applicationName": "DomainConfig",
                    "organizationName": "testOrg"
                }
            ]""";
}