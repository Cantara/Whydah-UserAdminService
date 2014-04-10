package net.whydah.admin.application;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class ApplicationTest {
    Application application = null;

    @BeforeMethod
    public void setUp() throws Exception {
        List<String> availableOrgIds = new ArrayList<>();
        availableOrgIds.add("aoi1");
        availableOrgIds.add("aoi2");
        application = new Application("id1","mockApp", "defRoleid", "deforgid", availableOrgIds);

    }
    @Test
    public void testXML() throws Exception {
        String applicationXml = application.toXML();
        Application verifyApplication = Application.fromXml(applicationXml);
        assertNotNull(verifyApplication);
        assertEquals("id1", verifyApplication.getId());
        assertEquals("mockApp", verifyApplication.getName());
        assertEquals("deforgid", verifyApplication.getDefaultOrgid());
        assertEquals("defRoleid", verifyApplication.getDefaultRole());
        assertNotNull( verifyApplication.getAvailableOrgIds(), "availableOrgIds should not be null");
        assertEquals("aoi1", verifyApplication.getAvailableOrgIds().get(0));
        assertEquals("aoi2", verifyApplication.getAvailableOrgIds().get(1));
    }


}
