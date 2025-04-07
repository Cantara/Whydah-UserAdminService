package net.whydah.admin;

import net.whydah.sso.util.SSLTool;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.valuereporter.client.activity.ObservedActivityDistributer;
import org.valuereporter.client.http.HttpObservationDistributer;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class MainWithJetty {
    public static final String CONTEXT_PATH = "/useradminservice";
    private static final Logger log = LoggerFactory.getLogger(MainWithJetty.class);

    private Server server;
    private String resourceBase;
    private static int jettyPort;
    int maxThreads = 100;
    int minThreads = 10;
    int idleTimeout = 120;


    public static String getHEALTHURL() {
        return "http://localhost:" + jettyPort + CONTEXT_PATH + "/health";
    }


    public static void main(String[] arguments) throws Exception {
        // http://stackoverflow.com/questions/9117030/jul-to-slf4j-bridge
        // Jersey uses java.util.logging - bridge to slf4
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);

        // Initialize Spring context to load properties
        // Look for external properties first, then fall back to classpath
        System.out.println("Looking for properties in current directory and classpath...");

        // Set system property to help Spring find the properties
        String configPath = System.getProperty("config.location");
        if (configPath == null) {
            // Default to the current directory if not specified
            configPath = "./";
            System.setProperty("config.location", configPath);
        }

        // Try to load external property file first for debugging
        File externalProps = new File(configPath + "useradminservice_override.properties");
        if (externalProps.exists()) {
            System.out.println("Found external properties at: " + externalProps.getAbsolutePath());
            log.info("Found external properties at: " + externalProps.getAbsolutePath());
        } else {
            System.out.println("No external properties found at: " + externalProps.getAbsolutePath());
            log.info("No external properties found at: " + externalProps.getAbsolutePath());
        }
        
        
        
        
        // Manually load properties first
        Properties manualProps = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream("useradminservice.properties")) {
            if (in != null) {
                manualProps.load(in);
                System.out.println("Manually loaded default properties");
            } else {
                System.out.println("Could not find default properties");
            }
        } catch (Exception e) {
            System.out.println("Error loading default properties: " + e.getMessage());
        }
        
        // Initialize Spring context
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        
        // Now try to get property through various methods for debugging
        Environment env = context.getEnvironment();
        String host1 = env.getProperty("valuereporter.host");
        String host2 = manualProps.getProperty("valuereporter.host");
        String host3 = System.getProperty("valuereporter.host");
        
        System.out.println("Reporter host from Spring Environment: " + host1);
        System.out.println("Reporter host from manual props: " + host2);
        System.out.println("Reporter host from system props: " + host3);
        
        // Use manual properties if Spring fails
        String reporterHost = host1 != null ? host1 : host2;
        String reporterPort = env.getProperty("valuereporter.port", manualProps.getProperty("valuereporter.port"));
        String prefix = env.getProperty("applicationname", manualProps.getProperty("applicationname"));
       
        System.out.println("Direct inspection of Spring Environment:");
        log.info("Direct inspection of Spring Environment:");
        String[] keysToLog = {
                "service.port", "myuri", "useridentitybackend",
                "securitytokenservice", "applicationid", "applicationname",
                "sslverification"
        };
        for (String key : keysToLog) {
            String value = env.getProperty(key);
            System.out.println("  " + key + " = " + value);
            log.info("  " + key + " = " + value);
        }
        // Property-overwrite of SSL verification to support weak ssl certificates
        String sslVerification = env.getProperty("sslverification");
        if ("disabled".equalsIgnoreCase(sslVerification)) {
            SSLTool.disableCertificateValidation();
        }

        int cacheSize = Integer.valueOf(env.getProperty("valuereporter.activity.batchsize","50"));
        int forwardInterval = Integer.valueOf(env.getProperty("valuereporter.activity.postintervalms", "10000"));
        new Thread(ObservedActivityDistributer.getInstance(reporterHost, reporterPort, prefix, cacheSize, forwardInterval)).start();
        new Thread(new HttpObservationDistributer(reporterHost, reporterPort, prefix)).start();
        Integer webappPort = Integer.valueOf(env.getProperty("service.port", "9992"));
        MainWithJetty main = new MainWithJetty(webappPort);
        main.start();
        main.join();
    }

    /**
     * http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty#Setting_a_ServletContext
     */
    public MainWithJetty(int jettyPort) {
        this.jettyPort = jettyPort;
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(jettyPort);
        server.setConnectors(new Connector[]{connector});

        URL url = ClassLoader.getSystemResource("webapp/WEB-INF/web.xml");
        resourceBase = url.toExternalForm().replace("/WEB-INF/web.xml", "");
    }


    public void start() throws Exception {
        WebAppContext context = new WebAppContext();
        log.debug("Start Jetty using resourcebase={}", resourceBase);
        context.setDescriptor(resourceBase + "/WEB-INF/web.xml");
        context.setBaseResourceAsString(resourceBase);
//    setResourceBase(resourceBase);
        context.setContextPath(CONTEXT_PATH);
        context.setParentLoaderPriority(true);
        server.setHandler(context);

        server.start();
        int localPort = getPortNumber();
        log.info("Jetty server started on port {}, context path {}", localPort, CONTEXT_PATH);
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public int getPortNumber() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }
}