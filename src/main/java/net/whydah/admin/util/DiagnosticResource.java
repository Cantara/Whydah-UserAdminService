package net.whydah.admin.util;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Path("/diag")
@Component
public class DiagnosticResource {
    private static final Logger log = LoggerFactory.getLogger(DiagnosticResource.class);

    @Autowired
    private ApplicationContext springContext;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String diagnose() {
        StringBuilder report = new StringBuilder("Spring-Jersey Integration Diagnostic\n");
        report.append("=========================================\n\n");

        // Check Spring context
        report.append("Spring context available: ").append(springContext != null).append("\n");

        if (springContext != null) {
            // List some beans
            report.append("ApplicationsService bean available: ")
                    .append(springContext.containsBean("applicationsService")).append("\n");
            report.append("UsersService bean available: ")
                    .append(springContext.containsBean("usersService")).append("\n");
        }

        // Check Jersey-Spring integration
        try {
            Class.forName("org.glassfish.jersey.ext.spring6.SpringComponentProvider");
            report.append("Jersey-Spring6 integration found in classpath").append("\n");
        } catch (ClassNotFoundException e) {
            report.append("Jersey-Spring6 integration NOT found in classpath!").append("\n");
        }

        return report.toString();
    }
}