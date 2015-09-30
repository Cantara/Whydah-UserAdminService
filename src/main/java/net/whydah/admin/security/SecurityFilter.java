package net.whydah.admin.security;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * NOT ENABLED YET!
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-07-06
 */
@Component
public class SecurityFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);
    private final String tokenServiceUrl;

    @Autowired
    @Configure
    public SecurityFilter(@Configuration("securitytokenservice") String tokenServiceUrl) {
        this.tokenServiceUrl = tokenServiceUrl;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }


    Integer authenticateAndAuthorizeRequest(String pathInfo) {
        log.trace("filter path {}", pathInfo);

        //Open paths without authentication
        if (pathInfo.startsWith("/health")) {
            return null;
        }

        /*
        if (ApplicationMode.skipSecurityFilter()) {
            log.warn("Running in noSecurityFilter mode, security is omitted for users.");
            Authentication.setAuthenticatedUser(buildMockedUserToken());
            return null;
        }
        */


        //Require authenticated and authorized applicationtokenid
        String applicationTokenId = findPathElement(pathInfo, 1);
        //applicationTokenId of application calling UAS, not UAS applicationTokenId
        //boolean applicationVerified = new CommandValidateApplicationTokenId(tokenServiceUrl, applicationTokenId).execute();
        boolean applicationVerified = true;
        if (!applicationVerified) {
            log.trace("Application not authorized, applicationTokenId={}", applicationTokenId);
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        /*
        /{applicationtokenid}/auth/logon - LogonController (User Login)
        /{applicationtokenid}/auth/password/reset/username
        /{applicationtokenid}/create_logon_facebook_user (createAndLogonUser)
         */
        String second = findPathElement(pathInfo, 2);
        if (second == null) {
            return HttpServletResponse.SC_NOT_FOUND;
        }
        if (second.equals("auth") || second.equals("create_logon_facebook_user") || second.equals("signup")) {
            return null;
        }

        String userTokenId = second;
        URI tokenServiceUri = UriBuilder.fromUri(tokenServiceUrl).build();
        //boolean userVerified = new CommandValidateUsertokenId(tokenServiceUri, applicationTokenId, userTokenId).execute();
        boolean userVerified = true;
        if (!userVerified) {
            log.trace("User not Authorized=" + userTokenId);
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        //TODO verify required user role
        /*
        /{applicationtokenid}/{userTokenId}/application
        /{applicationtokenid}/{userTokenId}/adminapplications
        /{applicationtokenid}/{userTokenId}/applications

        /{applicationtokenid}/{usertokenid}/useraggregate/{uid}
        /{applicationtokenid}/{userTokenId}/user
        /{applicationtokenid}/{userTokenId}/users
         */
        //Authentication.setAuthenticatedUser(userToken);
        return null;
    }

    private String findPathElement(String pathInfo, int elementNumber) {
        if (pathInfo == null) {
            return null;
        }
        String[] pathElements = pathInfo.split("/");
        if (pathElements.length <= elementNumber) {
            return null;
        }
        return pathElements[elementNumber];
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


    @Override
    public void destroy() {
    }
}
