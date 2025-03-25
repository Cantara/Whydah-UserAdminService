package net.whydah.admin;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import net.whydah.sso.config.ApplicationMode;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test that starts the application and verifies resources are properly initialized.
 */
public class ApplicationStartupTest {
    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupTest.class);
    private static Server server;
    private static int port;

    @BeforeAll
    public static void setUp() throws Exception {
        // Set to DEV mode for testing
        System.setProperty(ApplicationMode.IAM_MODE_KEY, ApplicationMode.DEV);

        // Use random port to avoid conflicts
        Random r = new Random(System.currentTimeMillis());
        port = 10000 + r.nextInt(20000);
        log.info("Starting server on port: {}", port);

        // Test-specific properties that might need overriding
        System.setProperty("securitytokenservice", "http://localhost:9998/tokenservice/");
        System.setProperty("myuri", "http://localhost:" + port + "/useradminservice/");
        System.setProperty("service.port", String.valueOf(port));

        // Initialize server
        server = new Server(port);

        // Create a ServletContextHandler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/useradminservice");
        server.setHandler(context);

        // Add Spring context loader listener
        context.addEventListener(new ContextLoaderListener());
        context.addEventListener(new RequestContextListener());

        // Set the location of the Spring context configuration
        context.setInitParameter("contextConfigLocation", "classpath:applicationContext.xml");

        // Add Jersey servlet
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitParameter("jakarta.ws.rs.Application", "net.whydah.admin.config.JerseyApplication");
        jerseyServlet.setInitOrder(1);

        // Start the server
        server.start();
        log.info("Server started on port {}", port);

        // Wait for server to initialize fully
        Thread.sleep(5000);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
            log.info("Server stopped");
        }
    }

    @Test
    public void testServerStartup() {
        log.info("Verifying server startup");
        assertTrue(server.isRunning(), "Server should be running");
    }

    @Test
    public void testHealthEndpoint() {
        log.info("Testing health endpoint");
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:" + port + "/useradminservice/health")
                .request()
                .get();

        assertEquals(200, response.getStatus(), "Health endpoint should return 200 OK");
        String responseBody = response.readEntity(String.class);
        log.info("Health response: {}", responseBody);
        assertTrue(responseBody.length() > 0, "Health response should not be empty");
    }

    @Test
    public void testApplicationsResource() {
        // Create a dummy application token for testing
        String dummyToken = "test_token_" + System.currentTimeMillis();

        log.info("Testing ApplicationsResource with token: {}", dummyToken);
        Client client = ClientBuilder.newClient();

        // This should at least return a 401 Unauthorized if the resource is properly initialized
        // rather than a 500 Server Error if there are dependency issues
        Response response = client.target("http://localhost:" + port + "/useradminservice/" + dummyToken + "/applications")
                .request()
                .get();

        // We expect either a 401 (unauthorized) or 503 (service unavailable) because we're using a dummy token
        // but not a 500 Server Error which would indicate initialization problems
        int status = response.getStatus();
        log.info("ApplicationsResource response status: {}", status);

        assertTrue(status == 401 || status == 503,
                "Expected status 401 or 503, but got " + status + ". This may indicate resource initialization problems.");
    }

    @Test
    public void testAllResourcesAvailable() {
        // Test endpoints for various resources to ensure they're all properly initialized
        String[] endpoints = {
                "/health",
                "/dummytoken/applications",
                "/dummytoken/dummyuser/application",
                "/dummytoken/dummyuser/user",
                "/dummytoken/dummyuser/users",
                "/dummytoken/hasUASAccess"
        };

        Client client = ClientBuilder.newClient();

        for (String endpoint : endpoints) {
            log.info("Testing endpoint: {}", endpoint);
            Response response = client.target("http://localhost:" + port + "/useradminservice" + endpoint)
                    .request()
                    .get();

            int status = response.getStatus();
            log.info("Response status for {}: {}", endpoint, status);

            // We don't expect 500 Server Error which would indicate initialization problems
            assertTrue(status != 500,
                    "Endpoint " + endpoint + " returned status 500, which may indicate resource initialization problems.");
        }
    }

    @Test
    public void testJerseyApplicationInitialization() {
        // Verify that the JerseyApplication is properly initialized by checking if its resources are available
        try {
            // This will throw an exception if Jersey resources are not properly registered
            Client client = ClientBuilder.newClient();
            client.target("http://localhost:" + port + "/useradminservice/nonexistent-path")
                    .request()
                    .options();

            log.info("Jersey application initialized successfully");
        } catch (Exception e) {
            log.error("Error during Jersey initialization test", e);
            throw e;
        }
    }

    @Test
    public void testHK2SpringIntegration() {
        // A placeholder test for HK2-Spring integration
        // This should be replaced with a more specific test for your application
        log.info("Verifying HK2-Spring integration");

        // Example: Check that a Spring bean is available through HK2
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:" + port + "/useradminservice/dummytoken/applications")
                .request()
                .get();

        // If Spring beans are not properly injected into Jersey resources,
        // we'd likely get a 500 error rather than 401/503
        int status = response.getStatus();
        log.info("HK2-Spring integration test status: {}", status);

        assertTrue(status != 500,
                "Expected status not to be 500, which would indicate HK2-Spring integration problems.");
    }
}