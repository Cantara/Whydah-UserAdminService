package net.whydah.admin.applications;

import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.*;

/**
 * Created by baardl on 13.06.15.
 */
public class StubbedApplicationsRepositoryTest {
    private static final Logger log = getLogger(StubbedApplicationsRepositoryTest.class);

    private StubbedApplicationsRepository repo = null;

    @BeforeMethod
    public void setUp() throws Exception {
        repo = new StubbedApplicationsRepository();
    }

    @Test
    public void testReadFile() throws Exception {
        String stubbedApplication = repo.readFile("stubbedData/application.json");
        log.debug("Content: " + stubbedApplication);
        assertNotNull(stubbedApplication);
        assertTrue(stubbedApplication.length() > 0);
        String stubbedApplicationList = repo.readFile("stubbedData/applications.json");
        assertNotNull(stubbedApplicationList);

        assertEquals(repo.readFile("nonexisting.dll"),"");

    }

    @Test
    public void testFindById() throws Exception {
        String application = repo.findById("11");

        assertNotNull(application);
        assertTrue(application.contains("SecurityTokenService"));

    }

    @Test
    public void testAddApplication() throws Exception {
        String stubbedApplication = repo.readFile("stubbedData/application.json");
        repo.addApplication(stubbedApplication);
    }
}