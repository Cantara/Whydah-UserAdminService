package net.whydah.admin.user.uib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Created by baardl on 17.04.14.
 */
public class UserAgregateTest {
    private static final Logger log = LoggerFactory.getLogger(UserAgregateTest.class);

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

    @Test
    public void buildXml() throws Exception {
        UserIdentity userIdentity = new UserIdentity("uid1", "userName1", "firstName1", "lastName1", "personRef1", "first.last@example.com", "93333000", "pwdPwd1234");
        List<UserPropertyAndRole> userPropertiesAndRoles = new ArrayList<>();
        UserPropertyAndRole userPropertyAndRole = new UserPropertyAndRole();
        userPropertyAndRole.setId("id1");
        userPropertyAndRole.setApplicationId("appid1");
        userPropertyAndRole.setApplicationName("appName");
        userPropertyAndRole.setOrganizationId("orgId");
        userPropertyAndRole.setOrganizationName("orgName");
        userPropertyAndRole.setApplicationRoleName("roleName");
        userPropertyAndRole.setApplicationRoleValue("roleValue");
        userPropertiesAndRoles.add(userPropertyAndRole);
        UserAggregate userAggregate = new UserAggregate(userIdentity, userPropertiesAndRoles);
        log.info("userAggregate: {}", userAggregate.toXML());
        assertNotNull(userAggregate.toXML());

    }

    @Test
    public void buildJson() throws Exception {
        UserIdentity userIdentity = new UserIdentity("uid1", "userName1", "firstName1", "lastName1", "personRef1", "first.last@example.com", "93333000", "pwdPwd1234");
        log.debug("userIdentity, toJson {}", userIdentity.toJson());
        String fromJson = "{\n" +
                "    \"username\": \"userName1\",\n" +
                "    \"firstName\": \"firstName1\",\n" +
                "    \"lastName\": \"lastName1\",\n" +
                "    \"personRef\": \"personRef1\",\n" +
                "    \"email\": \"first.last@example.com\",\n" +
                "    \"cellPhone\": \"93333000\",\n" +
                "    \"uid\": \"uid1\",\n" +
                "    \"password\": \"pwdPwd1234\",\n" +
                "    \"personName\": \"firstName1 lastName1\"\n" +
                "}";
        UserIdentity uiFromJson = UserIdentity.fromJson(fromJson);
        assertEquals(userIdentity.toJson(), uiFromJson.toJson());

    }
}
