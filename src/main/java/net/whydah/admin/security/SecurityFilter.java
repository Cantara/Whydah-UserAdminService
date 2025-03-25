package net.whydah.admin.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.whydah.admin.CredentialStore;
import net.whydah.sso.application.mappers.ApplicationCredentialMapper;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandGetUserTokenByUserTokenId;
import net.whydah.sso.commands.userauth.CommandValidateUserTokenId;
import net.whydah.sso.user.helpers.UserXpathHelper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.util.WhydahUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * SecurityFilter is responsible for verifying applicationTokenId and userTokenId against STS.
 * Verifying application and user roles is the responsibility of UIB.
 */
public class SecurityFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    private String stsAppId;
    private URI tokenServiceUri;
    private final CredentialStore credentialStore;

    public SecurityFilter(String stsUri, String stsAppId, UASCredentials uasCredentials, CredentialStore credentialStore) {
        this.stsAppId = stsAppId;
        if (this.stsAppId == null || this.stsAppId.equals("")) {
            this.stsAppId = "2211";
        }
        this.tokenServiceUri = URI.create(stsUri);
        this.credentialStore = credentialStore;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        Integer statusCode = authenticateAndAuthorizeRequest(servletRequest.getPathInfo());
        if (statusCode == null) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(statusCode);
        }
    }

    /**
     * @param pathInfo the path to apply the filter to
     * @return HttpServletResponse.SC_UNAUTHORIZED if authentication fails, otherwise null
     */
    Integer authenticateAndAuthorizeRequest(String pathInfo) {
        log.info("filter path {}", pathInfo);

        //match /
        if (pathInfo == null || pathInfo.equals("/")) {
            log.warn("SecurityFilter - path, returning unauthorized");
            return HttpServletResponse.SC_NOT_FOUND;
        }

        //match /health
        if (pathInfo.equals("/health")) {
            log.debug("SecurityFilter - health path, returning authorized");
            return null;  // OK
        }

        // OK, we do not try to resolve stuff if we do not have a whydah session
        if (credentialStore.getWas() == null) {
            log.info("Unable to access whydah session, returning HTTP 503 (SERVICE_UNAVAILABLE)");
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }

        if (!credentialStore.hasValidApplicationSession()) {
            log.info("Invalid UAS whydah session, returning HTTP 503 (SERVICE_UNAVAILABLE)");
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }

        String callerApplicationTokenId = findPathElement(pathInfo, 1).substring(1);

        //" we should probably avoid askin sts if we know it is sts asking, but we should ask sts for a valid applicationsession for all other applications"
        String appId = credentialStore.getApplicationID(callerApplicationTokenId);
        if (appId == null) {
            log.warn("SecurityFilter - unable to lookup application from applicationtokenid {}, returning unauthorized for path {}", callerApplicationTokenId, pathInfo);
            return HttpServletResponse.SC_UNAUTHORIZED;

            // Lets get UAS through
        } else if (appId.equals(credentialStore.getMyApplicationID())) {
            log.info("SecurityFilter - found UAS access, OK");
            return null;  // OK to call myself

            // And sts gets special treatment too
        } else if (appId.equals(stsAppId)) {
            log.debug("Check STS token {}", callerApplicationTokenId);
            //HUY: sometimes this callback failed. Try allowing longer timeout
            //also add some logging for detecting if the callback command is really failed
            Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(tokenServiceUri, callerApplicationTokenId, 10000) {
                protected Boolean getFallback() {
                    log.error("Fall back called");
                    return super.getFallback();
                }

                protected Boolean dealWithFailedResponse(String responseBody, int statusCode) {
                    log.error("Failed response body {} code {}", responseBody, statusCode);
                    return super.dealWithFailedResponse(responseBody, statusCode);
                }
            }.execute();
            if (!applicationTokenIsValid) {
                log.warn("SecurityFilter - invalid application session for sts request, returning unauthorized");
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
        } else if (!credentialStore.isValidApplicationSession(tokenServiceUri, callerApplicationTokenId)) {
            log.info("Invalid caller whydah session, invalid applicationtokenid, returning unauthorized");
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        String usertokenId = findPathElement(pathInfo, 2).substring(1);

        //paths without userTokenId
        String path = pathInfo.substring(1); //strip leading /
        //strip applicationTokenId from pathInfo
        path = path.substring(path.indexOf("/"));

        /*
        /{applicationTokenId}/auth/password/reset/{usernaem}     //PasswordResource
         /{applicationTokenId}/user/{uid}/reset_password     //PasswordResource
        /{applicationTokenId}/user/{uid}/change_password    //PasswordResource
        /{applicationTokenId}/authenticate/user/*           //UserAuthenticationEndpoint
        /{applicationTokenId}/signup/user                   //UserSignupEndpoint
        */
        String applicationAuthPattern = "/application/auth";
        String userLogonPattern = "/auth/logon/user";           //LogonController, same as authenticate/user in UIB.
        String userAuthPattern = "/authenticate/user(|/.*)";    //This is the pattern used in UIB
        String pwResetAuthPattern = "/auth/password/reset/username/(.*?)";
        String pwPattern = "/user/.+/(reset|change)_password";
        String userSignupPattern = "/signup";
        String userThirdParySignupPattern = "/createandlogon";
        String listApplicationsPattern = "/applications";
        String findApplicationsPattern = "/applications/find/(.*?)";
        String findApplicationsPattern_ = "/applications/find";
        String findApplicationsPattern2 = "/find/applications/(.*?)";
        String findApplicationsPattern2_ = "/find/applications";
        String hasUASAccess = "/hasUASAccess";
        String send_scheduled_email = "/send_scheduled_email";
        String userPWEnabeled = "/user/.+/password_login_enabled";
        String userThirdPartyLoginEnabled = "/user/.+/.+/thirdparty_login_enabled";
        String[] patternsWithoutUserTokenId = {applicationAuthPattern, userLogonPattern, userThirdParySignupPattern, pwResetAuthPattern, pwPattern, userAuthPattern, userSignupPattern, listApplicationsPattern, hasUASAccess, send_scheduled_email, userPWEnabeled, userThirdPartyLoginEnabled, findApplicationsPattern, findApplicationsPattern2, findApplicationsPattern_, findApplicationsPattern2_};
        for (String pattern : patternsWithoutUserTokenId) {
            if (Pattern.compile(pattern).matcher(path).matches()) {
                log.debug("{} was matched to {}. SecurityFilter passed.", path, pattern);
                return null;
            }
        }
        try {
            //paths WITH userTokenId verification
            /*
            /{applicationtokenid}/{userTokenId}/application     //ApplicationResource
            /{applicationtokenid}/{userTokenId}/applications    //ApplicationsResource
            /{applicationtokenid}/{userTokenId}/user            //UserResource
            /{applicationtokenid}/{usertokenid}/useraggregate   //UserAggregateResource
            /{applicationtokenid}/{usertokenid}/users           //UsersResource
             */

            Boolean userTokenIsValid = new CommandValidateUserTokenId(tokenServiceUri, credentialStore.getWas().getActiveApplicationTokenId(), usertokenId).execute();
            if (!userTokenIsValid) {
                log.warn("SecurityFilter - got application without valid userToken " + HttpServletResponse.SC_UNAUTHORIZED);
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
            UserApplicationRoleEntry adminUserRole = WhydahUtil.getWhydahUserAdminRole();
            String userTokenXml = new CommandGetUserTokenByUserTokenId(tokenServiceUri, credentialStore.getWas().getActiveApplicationTokenId(), ApplicationCredentialMapper.toXML(credentialStore.getWas().getMyApplicationCredential()), usertokenId).execute();
            if (UserXpathHelper.hasRoleFromUserToken(userTokenXml, adminUserRole.getApplicationId(), adminUserRole.getRoleName())) {
                log.debug("{} was matched adminUserRole {}. SecurityFilter passed.", path, adminUserRole);
                return null;
            }
        } catch (Exception e) {
            log.error("Unable to lookup application in UIB", e);
        }
        log.warn("SecurityFilter - fallback... unhandled ACL");
        return HttpServletResponse.SC_UNAUTHORIZED;
    }

    private String findPathElement(String pathInfo, int elementNumber) {
        String pathElement = null;
        if (pathInfo != null) {
            String[] pathElements = pathInfo.split("/");
            if (pathElements.length > elementNumber) {
                pathElement = "/" + pathElements[elementNumber];
            }
        }
        return pathElement;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to initialize
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }
}