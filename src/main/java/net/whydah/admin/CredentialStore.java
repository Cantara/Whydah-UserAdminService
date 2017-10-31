package net.whydah.admin;

import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.session.WhydahApplicationSession;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Repository
public class CredentialStore {
    private static WhydahApplicationSession was = null;
    private final String stsUri;
    private final String uasUri;
    private final ApplicationCredential uasApplicationCredential;


    @Autowired
    @Configure
    public CredentialStore(@Configuration("securitytokenservice") String stsUri,
                           @Configuration("myuri") String uasUri,
                           @Configuration("applicationid") String applicationid,
                           @Configuration("applicationname") String applicationname,
                           @Configuration("applicationsecret") String applicationsecret) {
        this.stsUri = stsUri;
        this.uasUri = uasUri;
        this.uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
        
    }

    public String getMyApplicationID() {
        return uasApplicationCredential.getApplicationID();
    }

    public String getUserAdminServiceTokenId() {
        if (was == null) {
            was = getWas();
        }
        return was.getActiveApplicationTokenId();

    }


    synchronized public WhydahApplicationSession getWas() {
        if (was == null) {
            was = WhydahApplicationSession.getInstance(stsUri, uasUri, uasApplicationCredential);
        }
        return was;
    }
}
