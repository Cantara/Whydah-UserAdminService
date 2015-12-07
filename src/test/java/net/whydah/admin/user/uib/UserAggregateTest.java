package net.whydah.admin.user.uib;

import net.whydah.sso.basehelpers.JsonPathHelper;
import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
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
public class UserAggregateTest {
    private static final Logger log = LoggerFactory.getLogger(UserAggregateTest.class);

    private String userAgregateTemplate = "{\"uid\":\"uid\",\"username\":\"usernameABC\",\"firstName\":\"firstName\",\"lastName\":\"lastName\",\"personRef\":\"personRef\",\"email\":\"email\",\"cellPhone\":\"12345678\",\"password\":\"password\",\"roles\":[{\"applicationId\":\"applicationId\",\"applicationName\":\"applicationName\",\"organizationName\":\"organizationName\",\"applicationRoleName\":\"roleName\",\"applicationRoleValue\":\"email\",\"id\":\"null\"},{\"applicationId\":\"applicationId123\",\"applicationName\":\"applicationName123\",\"organizationName\":\"organizationName123\",\"applicationRoleName\":\"roleName123\",\"applicationRoleValue\":\"roleValue123\",\"id\":\"null\"}]}\n";

    @Test
    public void buildUserAggregate() throws Exception {
        String e = JsonPathHelper.getStringFromJsonpathExpression(userAgregateTemplate,"$.uid");
        UserAggregate userAggregate = UserAggregateMapper.parseUserAggregateNoIdentityJson(userAgregateTemplate);
        assertNotNull(userAggregate);
        assertEquals("uid", userAggregate.getUid());
        assertEquals("personRef", userAggregate.getPersonRef());
        assertEquals("12345678", userAggregate.getCellPhone());
        List<UserApplicationRoleEntry> userRoles = userAggregate.getRoleList();
        assertNotNull(userRoles);
        //TODO bli: work in progress
    }

    @Test
    public void buildXml() throws Exception {
        UserIdentity userIdentity = new UserIdentity();
        List<UserApplicationRoleEntry> userPropertiesAndRoles = new ArrayList<>();
        UserApplicationRoleEntry userPropertyAndRole = new UserApplicationRoleEntry();
        userPropertyAndRole.setId("id1");
        userPropertyAndRole.setApplicationId("appid1");
        userPropertyAndRole.setApplicationName("appName");
        userPropertyAndRole.setOrgName("orgName");
        userPropertyAndRole.setRoleName("roleName");
        userPropertyAndRole.setRoleName("roleValue");
        userPropertiesAndRoles.add(userPropertyAndRole);
        UserAggregate userAggregate = UserAggregateMapper.fromJson(userAgregateTemplate);
        userAggregate.setRoleList(userPropertiesAndRoles);;
        log.info("userAggregate: {}", userAggregate.toXML());
        assertNotNull(userAggregate.toXML());

    }

    @Test
    public void buildJson() throws Exception {
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
        UserIdentity uiFromJson = UserIdentityMapper.fromUserIdentityJson(fromJson);

    }
}
