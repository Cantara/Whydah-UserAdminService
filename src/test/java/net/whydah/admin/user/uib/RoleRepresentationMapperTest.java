package net.whydah.admin.user.uib;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by baardl on 22.06.15.
 */
public class RoleRepresentationMapperTest {

    @Test
    public void testFromJson() throws Exception {

        List<RoleRepresentation> role = RoleRepresentationMapper.fromJson(roleJson);
        assertNotNull(role);
        assertEquals(role.size(),1);

    }

    private String roleJson = "[\n" +
            "    {\n" +
            "        \"roleId\": \"8bafa279-1615-4833-869e-106daaefa797\",\n" +
            "        \"uid\": \"e365cccc-dc2b-4c79-a79c-d0ae3b79a45d\",\n" +
            "        \"applicationId\": \"201\",\n" +
            "        \"applicationRoleName\": \"testRoleName\",\n" +
            "        \"applicationRoleValue\": \"true\",\n" +
            "        \"applicationName\": \"DomainConfig\",\n" +
            "        \"organizationName\": \"testOrg\"\n" +
            "    }\n" +
            "]";
}