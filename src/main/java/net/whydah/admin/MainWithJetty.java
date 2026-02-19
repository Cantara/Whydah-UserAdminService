package net.whydah.admin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.valuereporter.client.activity.ObservedActivityDistributer;
import org.valuereporter.client.http.HttpObservationDistributer;

import com.exoreaction.notification.SlackNotificationFacade;

import net.whydah.sso.config.ApplicationMode;
import net.whydah.sso.util.SSLTool;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class MainWithJetty {

    public static final String CONTEXT_PATH = "/useradminservice";

    /*
     * Static initialiser runs before anything else in this class.
     *
     * Order matters:
     *   1. Reset JUL completely (removes all existing handlers).
     *   2. Install SLF4JBridgeHandler so JUL records route to Logback.
     *   3. Set JUL root level to ALL so nothing is silently dropped
     *      before reaching the bridge (Logback's own levels then apply).
     *   4. Only NOW obtain the SLF4J Logger - Logback is fully wired.
     *
     * Note: logback.xml uses LevelChangePropagator which keeps JUL levels
     * in sync automatically after startup, so step 3 is just for the
     * brief window before Logback loads its config.
     */
    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
    }

    private static final Logger log = LoggerFactory.getLogger(MainWithJetty.class);

    public static final String IMPLEMENTATION_VERSION =
            MainWithJetty.class.getPackage().getImplementationVersion();

    private Server server;
    private String resourceBase;
    private static int jettyPort;

    private static final int MAX_THREADS  = 100;
    private static final int MIN_THREADS  = 10;
    private static final int IDLE_TIMEOUT = 120;

    public static String getHealthUrl() {
        return "http://localhost:" + jettyPort + CONTEXT_PATH + "/health";
    }

    // ------------------------------------------------------------------
    // main
    // ------------------------------------------------------------------
    public static void main(String[] arguments) throws Exception {

        log.info("=======================================================");
        log.info("Starting UserAdminService version {}", IMPLEMENTATION_VERSION);
        log.info("=======================================================");

        // Config-location resolution
        String configPath = System.getProperty("config.location", "./");
        System.setProperty("config.location", configPath);

        File externalProps = new File(configPath + "useradminservice_override.properties");
        if (externalProps.exists()) {
            log.info("External properties found at: {}", externalProps.getAbsolutePath());
        } else {
            log.info("No external properties at: {}", externalProps.getAbsolutePath());
        }

        // Load defaults from classpath for fallback comparison
        Properties fallbackProps = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream("useradminservice.properties")) {
            if (in != null) {
                fallbackProps.load(in);
                log.debug("Loaded fallback properties from classpath");
            } else {
                log.warn("useradminservice.properties not found on classpath");
            }
        } catch (Exception e) {
            log.warn("Error loading fallback properties: {}", e.getMessage());
        }

        // Spring context - loads applicationContext.xml which wires property sources
        log.info("Initialising Spring context...");
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        Environment env = context.getEnvironment();
        log.info("Spring context ready.");

        // Dump key properties at startup for operational visibility
        log.info("--- Resolved configuration ---");
        for (String key : new String[]{
                "service.port", "myuri", "useridentitybackend",
                "securitytokenservice", "applicationid", "applicationname",
                "sslverification", "valuereporter.host", "valuereporter.port"}) {
            log.info("  {} = {}", key, env.getProperty(key));
        }

        // SSL
        if ("disabled".equalsIgnoreCase(env.getProperty("sslverification"))) {
            SSLTool.disableCertificateValidation();
            log.warn("SSL certificate validation is DISABLED");
        }

        // ValueReporter
        String reporterHost = env.getProperty(
                "valuereporter.host",
                fallbackProps.getProperty("valuereporter.host", "localhost"));
        String reporterPort = env.getProperty(
                "valuereporter.port",
                fallbackProps.getProperty("valuereporter.port", "4901"));
        String appName = env.getProperty(
                "applicationname",
                fallbackProps.getProperty("applicationname", "UserAdminService"));

        int cacheSize       = Integer.parseInt(env.getProperty("valuereporter.activity.batchsize",    "50"));
        int forwardInterval = Integer.parseInt(env.getProperty("valuereporter.activity.postintervalms","10000"));

        new Thread(
                ObservedActivityDistributer.getInstance(reporterHost, reporterPort, appName, cacheSize, forwardInterval),
                "valuereporter-activity"
        ).start();
        new Thread(
                new HttpObservationDistributer(reporterHost, reporterPort, appName),
                "valuereporter-http"
        ).start();

        // Start Jetty
        int webappPort = Integer.parseInt(env.getProperty("service.port", "9992"));
        MainWithJetty main = new MainWithJetty(webappPort);
        main.start();
        main.join();
    }

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public MainWithJetty(int port) {
        jettyPort = port;

        QueuedThreadPool threadPool = new QueuedThreadPool(MAX_THREADS, MIN_THREADS, IDLE_TIMEOUT);
        server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{ connector });

        URL url = ClassLoader.getSystemResource("webapp/WEB-INF/web.xml");
        resourceBase = url.toExternalForm().replace("/WEB-INF/web.xml", "");
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    public void start() throws Exception {
        WebAppContext context = new WebAppContext();
        log.debug("Starting Jetty with resourceBase={}", resourceBase);
        context.setDescriptor(resourceBase + "/WEB-INF/web.xml");
        context.setBaseResourceAsString(resourceBase);
        context.setContextPath(CONTEXT_PATH);
        context.setParentLoaderPriority(true);
        server.setHandler(context);

        server.start();

        int localPort = getPortNumber();
        log.info("Jetty started on port {}, context path {}", localPort, CONTEXT_PATH);
        log.info("Health endpoint: {}", getHealthUrl());

        String appMode = ApplicationMode.DEV.equalsIgnoreCase(
                System.getProperty(ApplicationMode.IAM_MODE_KEY, "")) ? "DEV" : "PROD";

        SlackNotificationFacade.initialize("UAS", appMode);
        SlackNotificationFacade.notifyStartupSuccess(localPort, CONTEXT_PATH, IMPLEMENTATION_VERSION);
    }

    public void stop() throws Exception {
        server.stop();
        SlackNotificationFacade.shutdown();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public int getPortNumber() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }
}