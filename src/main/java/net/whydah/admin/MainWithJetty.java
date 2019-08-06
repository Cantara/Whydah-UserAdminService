package net.whydah.admin;

import net.whydah.sso.util.SSLTool;
import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.model.Resource;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.valuereporter.client.activity.ObservedActivityDistributer;
import org.valuereporter.client.http.HttpObservationDistributer;

import java.net.URL;
import java.util.Map;
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

        final ConstrettoConfiguration configuration = new ConstrettoBuilder()
                .createPropertiesStore()
                .addResource(Resource.create("classpath:useradminservice.properties"))
                .addResource(Resource.create("file:./useradminservice_override.properties"))
                .done()
                .getConfiguration();

        printConfiguration(configuration);

        // Property-overwrite of SSL verification to support weak ssl certificates
        String sslVerification = configuration.evaluateToString("sslverification");
        if ("disabled".equalsIgnoreCase(sslVerification)) {
            SSLTool.disableCertificateValidation();
        }

        //Start Valuereporter event distributer.
        String reporterHost = configuration.evaluateToString("valuereporter.host");
        String reporterPort = configuration.evaluateToString("valuereporter.port");
        String prefix = configuration.evaluateToString("applicationname");
        int cacheSize = configuration.evaluateToInt("valuereporter.activity.batchsize");
        int forwardInterval = configuration.evaluateToInt("valuereporter.activity.postintervalms");
        new Thread(ObservedActivityDistributer.getInstance(reporterHost, reporterPort, prefix, cacheSize, forwardInterval)).start();
        new Thread(new HttpObservationDistributer(reporterHost, reporterPort, prefix)).start();
        Integer webappPort = configuration.evaluateToInt("service.port");
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

//        server = new Server(jettyPort);

        URL url = ClassLoader.getSystemResource("webapp/WEB-INF/web.xml");
        resourceBase = url.toExternalForm().replace("/WEB-INF/web.xml", "");
    }


    public void start() throws Exception {
        WebAppContext context = new WebAppContext();
        log.debug("Start Jetty using resourcebase={}", resourceBase);
        context.setDescriptor(resourceBase + "/WEB-INF/web.xml");
        context.setResourceBase(resourceBase);
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

    /*
    //TODO
    public String getBasePath() {
        String path = "http://localhost:" + jettyPort + CONTEXT_PATH;
        return path;
    }
    */
    public int getPortNumber() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    /*
    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    public String getResourceBase() {
        return resourceBase;
    }
    */

    private static void printConfiguration(ConstrettoConfiguration configuration) {
        Map<String, String> properties = configuration.asMap();
        for (String key : properties.keySet()) {
            log.info("Using Property: {}, value: {}", key, properties.get(key));
        }
    }
}
