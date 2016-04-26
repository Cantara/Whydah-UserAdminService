package net.whydah.admin;

import net.whydah.sso.application.types.ApplicationCredential;
import net.whydah.sso.session.WhydahApplicationSession;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Properties;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Repository
public class CredentialStore {
    private String userAdminServiceTokenId;
    private static WhydahApplicationSession was = null;
    private final String uibUri;
    private final String stsUri;
    private final ApplicationCredential uasApplicationCredential;


    @Autowired
    @Configure
    public CredentialStore(@Configuration("useridentitybackend") String uibUri,
                                             @Configuration("securitytokenservice") String stsUri,
                                             @Configuration("applicationid") String applicationid,
                                             @Configuration("applicationname") String applicationname,
                                             @Configuration("applicationsecret") String applicationsecret) {
        this.uibUri = uibUri;
        this.stsUri = stsUri;
        this.uasApplicationCredential = new ApplicationCredential(applicationid, applicationname, applicationsecret);
    }


    public String getUserAdminServiceTokenId() {
        if (was == null) {
            was = new WhydahApplicationSession(stsUri, uasApplicationCredential.getApplicationID(), uasApplicationCredential.getApplicationName(), uasApplicationCredential.getApplicationSecret());
        }
        return was.getActiveApplicationTokenId();

        //return userAdminServiceTokenId;
    }

    public void setUserAdminServiceTokenId(String userAdminServiceTokenId) {
        this.userAdminServiceTokenId = userAdminServiceTokenId;
        if (was == null) {
            was = new WhydahApplicationSession(stsUri, uasApplicationCredential.getApplicationID(), uasApplicationCredential.getApplicationName(), uasApplicationCredential.getApplicationSecret());
        }
    }
}
