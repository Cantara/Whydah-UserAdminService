package net.whydah.identity.data.helper;

import net.whydah.identity.data.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveUserTokenRepository {
    private final static Logger logger = LoggerFactory.getLogger(ActiveUserTokenRepository.class);

    private static final Map<String, UserToken> tokens = new ConcurrentHashMap<String, UserToken>(1024);

    /**
     * Get UserToken from UserTokenRepository. If token is not found or is not valid/timed out, null is returned.
     * @param tokenId userTokenId
     * @return UserToken if found and valid, null if not.
     */
    public static UserToken getUserToken(String tokenId) {
        logger.debug("getUserToken with userTokenid=" + tokenId);
        if (tokenId == null) {
        	return null;
        }
        UserToken resToken = tokens.get(tokenId);
        if (resToken != null && verifyUserToken(resToken)) {
            logger.info("Valid userToken found: " + resToken);
            logger.debug("userToken=" + resToken);
            return resToken;
        }
        logger.debug("No usertoken found for tokenId=" + tokenId);
        return null;
    }

    /**
     * Check if token exists in UserTokenRepository, and is valid and not timed out.
     * @param token UserToken
     * @return true if token is valid.
     */
    public static boolean verifyUserToken(UserToken token) {
        if(token.getTokenid() == null) {
            logger.info("UserToken not valid, missing tokenId");
            return false;
        }
        UserToken resToken = tokens.get(token.getTokenid());
        if (resToken == null) {
            logger.info("UserToken not found in repo.");
            return false;
        }
        logger.debug("UserToken from repo: {}", resToken);
        if (!token.equals(resToken)) {
            logger.info("UserToken not valid: not the same as in repo.");
            return false;
        }
        if (!resToken.isValid()) {
            tokens.remove(token.getTokenid());
            return false;
        }

        return true;
    }

    public static void addUserToken(UserToken token) {
        if(token.getTokenid() == null) {
            logger.error("Error: token has net tokenid");
            return;
        }
        if(tokens.containsKey(token.getTokenid())) {
            logger.error("Error: trying to update an already existing UserToken in repo..");
            return;
        }
        UserToken copy = token.copy();
        tokens.put(copy.getTokenid(), copy);
        logger.info("Added token with id {}", copy.getTokenid());
    }

    public static void removeUserToken(String userTokenId) {
        tokens.remove(userTokenId);
    }
}
