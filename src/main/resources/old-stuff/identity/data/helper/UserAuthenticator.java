package net.whydah.identity.data.helper;

import net.whydah.identity.data.UserToken;

public interface UserAuthenticator {
    public UserToken logonUser(String appTokenXml, String userCredentialXml);

    UserToken createAndLogonUser(String appTokenXml, String userCredentialXml, String fbUserXml);
}
