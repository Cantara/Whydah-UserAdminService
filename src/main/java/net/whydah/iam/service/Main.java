package net.whydah.iam.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import net.whydah.iam.service.config.AppConfig;
import net.whydah.iam.service.config.ApplicationMode;
import net.whydah.iam.service.config.SecurityTokenServiceModule;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private HttpServer httpServer;
    private int webappPort;

    protected void startServer() throws IOException {
        String appMode = ApplicationMode.getApplicationMode();
        AppConfig appConfig = new AppConfig();
        Injector injector = Guice.createInjector(new SecurityTokenServiceModule(appConfig, appMode));

        logger.info("Starting grizzly...");

        ServletHandler adapter = new ServletHandler();
        adapter.setContextPath("/tokenservice");
        adapter.addInitParameter("com.sun.jersey.config.property.packages", "net.whydah.iam.service");
        adapter.setProperty(ServletHandler.LOAD_ON_STARTUP, "1");

        GuiceFilter filter = new GuiceFilter();
        adapter.addFilter(filter, "guiceFilter", null);

        GuiceContainer container = new GuiceContainer(injector);
        adapter.setServletInstance(container);

        webappPort = Integer.valueOf(appConfig.getProperty("service.port"));
        httpServer = new HttpServer();
        ServerConfiguration serverconfig = httpServer.getServerConfiguration();
        serverconfig.addHttpHandler(adapter, "/");
        NetworkListener listener = new NetworkListener("grizzly", NetworkListener.DEFAULT_NETWORK_HOST, webappPort);
        httpServer.addListener(listener);
        httpServer.start();
        logger.info("SecurityTokenService started on port {}", webappPort);
    }

    public int getPort() {
        return webappPort;
    }

    public void stop() {
        if(httpServer != null) {
            httpServer.stop();
        }
    }


    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.startServer();
        try {
            // wait forever...
            Thread.currentThread().join();
        } catch (InterruptedException ie) {
        }
        main.stop();
    }
}
