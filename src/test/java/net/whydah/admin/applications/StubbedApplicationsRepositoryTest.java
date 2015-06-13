package net.whydah.admin.applications;

import org.slf4j.Logger;
import org.testng.annotations.Test;

import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.*;

/**
 * Created by baardl on 13.06.15.
 */
public class StubbedApplicationsRepositoryTest {
    private static final Logger log = getLogger(StubbedApplicationsRepositoryTest.class);

    @Test
    public void testReadFile() throws Exception {
        StubbedApplicationsRepository repo = new StubbedApplicationsRepository();
        String stubbedApplication = repo.readFile("stubbedData/application.json");
        log.debug("Content: " + stubbedApplication);
        assertNotNull(stubbedApplication);
        assertTrue(stubbedApplication.length() > 0);
        String stubbedApplicationList = repo.readFile("stubbedData/applications.json");
        assertNotNull(stubbedApplicationList);

        assertEquals(repo.readFile("nonexisting.dll"),"");

    }
}