package net.whydah.admin.security;

import net.whydah.admin.CredentialStore;
import net.whydah.admin.applications.uib.UibApplicationsConnection;
import net.whydah.sso.application.mappers.ApplicationCredentialMapper;
import net.whydah.sso.application.mappers.ApplicationMapper;
import net.whydah.sso.application.types.Application;
import net.whydah.sso.commands.appauth.CommandGetApplicationIdFromApplicationTokenId;
import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandGetUsertokenByUsertokenId;
import net.whydah.sso.commands.userauth.CommandValidateUsertokenId;
import net.whydah.sso.user.helpers.UserXpathHelper;
import net.whydah.sso.user.types.UserApplicationRoleEntry;
import net.whydah.sso.util.WhydahUtil;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * SecurityFilter is reponsible for verifying applicationTokenId and userTokenId against STS.
 * Verifying application and user roles is the responsibility of UIB.
 *
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-11-22
 */
@Component
public class SecurityFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    private final String stsUri;
    private String stsAppId;
    private URI tokenServiceUri;
    private UASCredentials uasCredentials;
    private UibApplicationsConnection uibApplicationsConnection;
    private final CredentialStore credentialStore;


    @Autowired
    @Configure
    public SecurityFilter(@Configuration("securitytokenservice") String stsUri, @Configuration("securitytokenservice.appid") String stsAppId, UASCredentials uasCredentials, UibApplicationsConnection uibApplicationsConnection, CredentialStore credentialStore) {
        this.stsUri = stsUri;
        this.stsAppId = stsAppId;
        if (this.stsAppId == null || this.stsAppId.equals("")) {
            this.stsAppId = "2211";
        }
        this.tokenServiceUri = URI.create(stsUri);
        this.uasCredentials = uasCredentials;
        this.credentialStore = credentialStore;
        this.uibApplicationsConnection = uibApplicationsConnection;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        //Integer statusCode = null;  // todo: resetting filter as we do the check motstly in code as well  // authenticateAndAuthorizeRequest(servletRequest.getPathInfo());
        Integer statusCode = authenticateAndAuthorizeRequest(servletRequest.getPathInfo());  // lets run the filter to check it in the mean time
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


        String applicationTokenId = findPathElement(pathInfo, 1).substring(1);
        //" we should probably avoid askin sts if we know it is sts asking, but we should ask sts for a valid applicationsession for all other applications"
        String appId = new CommandGetApplicationIdFromApplicationTokenId(URI.create(stsUri), applicationTokenId).execute();
        if (appId == null) {
            log.warn("SecurityFilter - unable to lookup application from applicationtokenid, returning unauthorized");
            return HttpServletResponse.SC_UNAUTHORIZED;

            // Lets get UAS through
        } else if (appId.equals(uasCredentials.getApplicationId())) {
            log.info("SecurityFilter - found UAS access, OK");
            return null;  // OK to call myself

            // And sts gets special treatement too
        } else if (appId.equals(stsAppId)) {
            Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUri, applicationTokenId).execute();
            if (!applicationTokenIsValid) {
                log.warn("SecurityFilter - invalid application session for sts request, returning unauthorized");
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
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
        String pwResetAuthPattern = "/auth/password/reset/username";
        String pwPattern = "/user/.+/(reset|change)_password";
        String userSignupPattern = "/signup/user";
        String listApplicationsPattern = "/applications";
        String hasUASAccess = "/hasUASAccess";
        String send_scheduled_email = "/send_scheduled_email";

        String userPWEnabeled = "/user/.+/password_login_enabled";
        String[] patternsWithoutUserTokenId = {applicationAuthPattern, userLogonPattern, pwResetAuthPattern, pwPattern, userAuthPattern, userSignupPattern, listApplicationsPattern, hasUASAccess, send_scheduled_email, userPWEnabeled};
        for (String pattern : patternsWithoutUserTokenId) {
            if (Pattern.compile(pattern).matcher(path).matches()) {
                log.debug("{} was matched to {}. SecurityFilter passed.", path, pattern);
                return null;
            }
        }
        try {
            String applicationJson = uibApplicationsConnection.findApplications(applicationTokenId, usertokenId, appId);
            log.warn("SecurityFilter - got application:" + applicationJson);
            Application application = ApplicationMapper.fromJson(applicationJson);

            // Does the calling application has UAS access
            if (!application.getSecurity().isWhydahUASAccess()) {
                return HttpServletResponse.SC_UNAUTHORIZED;
            }


            //paths WITH userTokenId verification
        /*
        /{applicationtokenid}/{userTokenId}/application     //ApplicationResource
        /{applicationtokenid}/{userTokenId}/applications    //ApplicationsResource
        /{applicationtokenid}/{userTokenId}/user            //UserResource
        /{applicationtokenid}/{usertokenid}/useraggregate   //UserAggregateResource
        /{applicationtokenid}/{usertokenid}/users           //UsersResource
         */

            Boolean userTokenIsValid = new CommandValidateUsertokenId(tokenServiceUri, credentialStore.getWas().getActiveApplicationTokenId(), usertokenId).execute();
            if (!userTokenIsValid) {
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
            UserApplicationRoleEntry adminUserRole = WhydahUtil.getWhydahUserAdminRole();
            String userTokenXml = new CommandGetUsertokenByUsertokenId(tokenServiceUri, credentialStore.getWas().getActiveApplicationTokenId(), ApplicationCredentialMapper.toXML(uasCredentials.getApplicationCredential()), usertokenId).execute();
            if (UserXpathHelper.hasRoleFromUserToken(userTokenXml, adminUserRole.getApplicationId(), adminUserRole.getRoleName())) {
                return null;
            }
        } catch (Exception e) {
            log.error("Unable to lookup application in UIB", e);
        }
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
    }

    @Override
    public void destroy() {
    }
}
