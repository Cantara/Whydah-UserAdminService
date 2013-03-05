package net.whydah.iam.service.data;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: totto
 * Date: Nov 4, 2010
 * Time: 10:43:39 AM
 */
public class ApplicationTokenTest extends TestCase {

    public void testCreateApplicationCredential() {
        ApplicationCredential cred = new ApplicationCredential();
        cred.setApplicationID("Styrerommet");
        cred.setApplicationPassord("dummy");
        ApplicationToken imp = new ApplicationToken(cred.toXML());
        //System.out.println(imp.toXML());
        assertTrue("The generated application token is wrong.", imp.getApplicationID().equals(cred.getApplicationID()));
        assertTrue(imp.getApplicationTokenId().length() > 12);
    }

    public void testCreateApplicationCredential2() {
        ApplicationCredential cred = new ApplicationCredential();
        cred.setApplicationID("Giftit");
        cred.setApplicationPassord("dummy");
        ApplicationToken imp = new ApplicationToken(cred.toXML());
        //System.out.println(imp.toXML());
        assertTrue("The generated application token is wrong.", imp.getApplicationID().equals(cred.getApplicationID()));
        assertTrue(imp.getApplicationTokenId().length() > 12);
    }
}

