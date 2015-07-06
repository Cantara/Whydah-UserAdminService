package net.whydah.admin.security;

import net.whydah.sso.commands.appauth.CommandValidateApplicationTokenId;
import net.whydah.sso.commands.userauth.CommandValidateUsertokenId;
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
    public SecurityFilter(String tokenServiceUrl) {
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

        /*
        //match /password/{applicationtokenid}
        if (pathElement1.startsWith("/password")) {  //TODO change path
            String applicationTokenId = findPathElement(pathInfo, 2);
            //boolean applicationVerified = applicationTokenService.verifyApplication(applicationTokenId);
            boolean applicationVerified = true;
            if (applicationVerified) {
                log.trace("application verified {}. Moving to next in chain.", applicationTokenId);
                return null;
            } else {
                log.trace("Application not Authorized=" + applicationTokenId);
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
        }
        */
        //applicationTokenId of application calling UAS, not UAS applicationTokenId
        boolean applicationVerified = new CommandValidateApplicationTokenId(tokenServiceUrl, applicationTokenId).execute();
        if (!applicationVerified) {
            log.trace("Application not Authorized=" + applicationTokenId);
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        //match /{applicationTokenId}/authenticate/user
        /*
        String pathElement2 = findPathElement(pathInfo, 2);
        if (pathElement2.equals("/authenticate")) {
            log.debug("{} was matched to /{applicationTokenId}/authenticate/user", pathInfo);
            return null;
        }
        */

        //Authenticate and authorize userTokenId
        /* Paths:
        /{applicationtokenid}/{userTokenId}/application
        /{applicationtokenid}/{userTokenId}/applications
        /{applicationtokenid}/{userTokenId}/user
        /{applicationtokenid}/{usertokenid}/useraggregate
        /{applicationtokenid}/{usertokenid}/users

        /{applicationtokenid}/{userTokenId}/verifyApplicationAuth
         */
        String userTokenId = findPathElement(pathInfo, 2);  //TODO Check the other paths which do not have userTokenId as second element
        URI tokenServiceUri = UriBuilder.fromUri(tokenServiceUrl).build();
        boolean userVerified = new CommandValidateUsertokenId(tokenServiceUri, applicationTokenId, userTokenId).execute();
        if (!userVerified) {
            log.trace("User not Authorized=" + userTokenId);
            return HttpServletResponse.SC_UNAUTHORIZED;
        }
        //TODO verify required user role

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
