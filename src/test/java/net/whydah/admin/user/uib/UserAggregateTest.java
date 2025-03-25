package net.whydah.admin.user.uib;

import net.whydah.sso.user.mappers.UserAggregateMapper;
import net.whydah.sso.user.mappers.UserIdentityMapper;
import net.whydah.sso.user.types.UserAggregate;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.user.types.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Created by baardl on 17.04.14.
 */
public class UserAggregateTest {
    private static final Logger log = LoggerFactory.getLogger(UserAggregateTest.class);

    private final String uid = UUID.randomUUID().toString();
    private String userAgregateTemplate = "{\"uid\":\"" + uid + "\",\"username\":\"usernameABC\",\"firstName\":\"firstName\",\"lastName\":\"lastName\",\"personRef\":\"personRef\",\"email\":\"email@cantara.no\",\"cellPhone\":\"12345678\",\"password\":\"password\",\"roles\":[{\"applicationId\":\"applicationId\",\"applicationName\":\"applicationName\",\"organizationName\":\"organizationName\",\"applicationRoleName\":\"roleName\",\"applicationRoleValue\":\"email\",\"id\":\"null\"},{\"applicationId\":\"applicationId123\",\"applicationName\":\"applicationName123\",\"organizationName\":\"organizationName123\",\"applicationRoleName\":\"roleName123\",\"applicationRoleValue\":\"roleValue123\",\"id\":\"null\"}]}\n";

    @Test
    public void buildUserAggregate() throws Exception {
        UserAggregate userAggregate = UserAggregateMapper.fromUserAggregateNoIdentityJson(userAgregateTemplate);
        assertNotNull(userAggregate);
        assertEquals(uid, userAggregate.getUid());
        assertEquals("personRef", userAggregate.getPersonRef());
        assertEquals("12345678", userAggregate.getCellPhone());
        List<UserApplicationRoleEntry> userRoles = userAggregate.getRoleList();
        assertNotNull(userRoles);
        //TODO bli: work in progress
    }


    @Test
    public void buildJson() throws Exception {
        String fromJson = """
                {
                    "username": "userName1",
                    "firstName": "firstName1",
                    "lastName": "lastName1",
                    "personRef": "personRef1",
                    "email": "first.last@example.com",
                    "cellPhone": "93333000",
                    "uid": "uid1",
                    "password": "pwdPwd1234",
                    "personName": "firstName1 lastName1"
                }\
                """;
        UserIdentity uiFromJson = UserIdentityMapper.fromUserIdentityJson(fromJson);

    }
}
