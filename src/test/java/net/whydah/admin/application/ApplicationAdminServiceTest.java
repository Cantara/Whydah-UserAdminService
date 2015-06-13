package net.whydah.admin.application;

import net.whydah.admin.applications.StubbedApplicationsRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by baardl on 13.06.15.
 */
public class ApplicationAdminServiceTest {

    private StubbedApplicationsRepository repo = null;
    private ApplicationAdminService adminService = null;

    @BeforeMethod
    public void setUp() throws Exception {
        repo = new StubbedApplicationsRepository();
        adminService = new ApplicationAdminService(null,repo);
    }


    @Test
    public void testCreateApplication() throws Exception {
        String stubbedApplication = repo.readFile("stubbedData/application.json");
        adminService.createApplication("1","2",stubbedApplication);
        //OK if no exception is thrown.

    }

    @Test
    public void testGetApplication() throws Exception {
        String stubbedApplication = repo.readFile("stubbedData/application.json");
        adminService.createApplication("1","2",stubbedApplication);
        String addedApplication = adminService.getApplication("1","2","id1");
        assertNotNull(addedApplication);
        assertTrue(addedApplication.contains("webtest"));
    }
}