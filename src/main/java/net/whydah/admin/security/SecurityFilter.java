package net.whydah.admin.security;

import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandValidateUsertokenId;
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
import java.net.URISyntaxException;
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

    @Autowired
    @Configure
    public SecurityFilter(@Configuration("securitytokenservice") String stsUri) {
        this.stsUri = stsUri;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        Integer statusCode = null; //FIXME BLI original code: authenticateAndAuthorizeRequest(servletRequest.getPathInfo());
        if (statusCode == null) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(statusCode);
        }
    }


    /**
     *
     * @param pathInfo  the path to apply the filter to
     * @return HttpServletResponse.SC_UNAUTHORIZED if authentication fails, otherwise null
     */
    Integer authenticateAndAuthorizeRequest(String pathInfo) {
        log.debug("filter path {}", pathInfo);

        //match /
        if (pathInfo == null || pathInfo.equals("/")) {
            return HttpServletResponse.SC_NOT_FOUND;
        }


        String applicationTokenId = findPathElement(pathInfo, 1).substring(1);
        Boolean applicationTokenIsValid = new CommandValidateApplicationTokenId(stsUri, applicationTokenId).execute();
        if (!applicationTokenIsValid) {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }


        //paths without userTokenId
        String path = pathInfo.substring(1); //strip leading /
        //strip applicationTokenId from pathInfo
        path = path.substring(path.indexOf("/"));
        /*
        /{applicationTokenId}/user/{uid}/reset_password     //PasswordResource2
        /{applicationTokenId}/user/{uid}/change_password    //PasswordResource2
        /{applicationTokenId}/authenticate/user/*           //UserAuthenticationEndpoint
        /{applicationTokenId}/signup/user                   //UserSignupEndpoint
        */
        String applicationAuthPattern = "/application/auth";
        String userLogonPattern = "/auth/logon/user";           //LogonController, same as authenticate/user in UIB.
        String userAuthPattern = "/authenticate/user(|/.*)";    //This is the pattern used in UIB
        String pwPattern = "/user/.+/(reset|change)_password";
        String userSignupPattern = "/signup/user";
        String [] patternsWithoutUserTokenId = {applicationAuthPattern,userLogonPattern, pwPattern, userAuthPattern, userSignupPattern};
        for (String pattern : patternsWithoutUserTokenId) {
            if (Pattern.compile(pattern).matcher(path).matches()) {
                log.debug("{} was matched to {}. SecurityFilter passed.", path, pattern);
                return null;
            }
        }


        //paths WITH userTokenId verification
        /*
        /{applicationtokenid}/{userTokenId}/application     //ApplicationResource
        /{applicationtokenid}/{userTokenId}/applications    //ApplicationsResource
        /{applicationtokenid}/{userTokenId}/user            //UserResource
        /{applicationtokenid}/{usertokenid}/useraggregate   //UserAggregateResource
        /{applicationtokenid}/{usertokenid}/users           //UsersResource
         */
        String usertokenId = findPathElement(pathInfo, 2).substring(1);

        URI tokenServiceUri;
        try {
            tokenServiceUri = new URI(stsUri);
        } catch (URISyntaxException e) {
            log.error("{} is not a valid URI.", stsUri, e);
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        Boolean userTokenIsValid = new CommandValidateUsertokenId(tokenServiceUri, applicationTokenId, usertokenId).execute();
        if (!userTokenIsValid) {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }
        return null;
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
