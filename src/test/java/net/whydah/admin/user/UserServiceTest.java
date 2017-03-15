package net.whydah.admin.user;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.WhydahRoleCheckUtil;
import net.whydah.admin.user.uib.UibUserConnection;
import org.testng.annotations.BeforeMethod;

import static org.mockito.Mockito.mock;

/**
 * Created by baardl on 26.06.15.
 */
public class UserServiceTest {

    private UibUserConnection uibUserConnection;
    private CredentialStore credentialStore;
    private UserService userService;
    private WhydahRoleCheckUtil adminChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        uibUserConnection = mock(UibUserConnection.class);
        credentialStore = mock(CredentialStore.class);
        adminChecker = mock(WhydahRoleCheckUtil.class);
        userService = new UserService(uibUserConnection, credentialStore, adminChecker);
    }


    private static String rolesJson = "[{\"roleId\":\"296fcbd3-21f0-42c9-81eb-35c0fa41bd81\",\"uid\":\"useradmin\",\"applicationId\":\"19\",\"applicationRoleName\":\"WhydahUserAdmin\",\"applicationRoleValue\":\"99\",\"organizationName\":\"Whydah\",\"applicationName\":\"UserAdminWebApp\"}]";
}