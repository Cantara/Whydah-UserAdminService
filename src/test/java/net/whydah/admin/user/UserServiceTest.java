package net.whydah.admin.user;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhyDahRoleCheckUtil;
import net.whydah.admin.user.uib.UibUserConnection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by baardl on 26.06.15.
 */
public class UserServiceTest {

    private UibUserConnection uibUserConnection;
    private CredentialStore credentialStore;
    private UserService userService;
    private WhyDahRoleCheckUtil adminChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        uibUserConnection = mock(UibUserConnection.class);
        credentialStore = mock(CredentialStore.class);
        adminChecker = mock(WhyDahRoleCheckUtil.class);
        userService = new UserService(uibUserConnection, credentialStore, adminChecker);
    }


    private static String rolesJson = "[{\"roleId\":\"296fcbd3-21f0-42c9-81eb-35c0fa41bd81\",\"uid\":\"useradmin\",\"applicationId\":\"19\",\"applicationRoleName\":\"WhydahUserAdmin\",\"applicationRoleValue\":\"99\",\"organizationName\":\"Whydah\",\"applicationName\":\"UserAdminWebApp\"}]";
}