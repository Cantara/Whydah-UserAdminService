package net.whydah.identity.data.helper;

import net.whydah.identity.data.ApplicationToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder på tokens for autentiserte applikasjoner
 */
public class AuthenticatedApplicationRepository {
    private static final Map<String, ApplicationToken> apptokens = new HashMap<String, ApplicationToken>();

    public static boolean verifyApplicationToken(ApplicationToken token) {
        return token.equals(apptokens.get(token.getApplicationTokenId()));
    }

    public static boolean verifyApplicationTokenId(String tokenid) {
        return apptokens.get(tokenid) != null;
    }

    public static boolean verifyApplicationToken(String s) {
        try {
            String appid = s.substring(s.indexOf("<applicationtoken>") + "<applicationtoken>".length(), s.indexOf("</applicationtoken>"));
            return apptokens.get(appid) != null;
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static void addApplicationToken(ApplicationToken token) {
        apptokens.put(token.getApplicationTokenId(), token);
    }
}
