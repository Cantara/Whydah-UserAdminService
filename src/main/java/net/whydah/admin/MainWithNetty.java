package net.whydah.admin;

import net.whydah.admin.config.AppConfig;
import net.whydah.admin.config.ApplicationMode;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baardl on 07.04.14.
 */
public class MainWithNetty {
    private final static Logger log = LoggerFactory.getLogger(MainWithNetty.class);

    static int webappPort;
    static NettyJaxrsServer netty;
    private static final String APP_CONTEXT="/useradminservice";

    public static void main(String[] args) {
        String appMode = ApplicationMode.getApplicationMode();
        AppConfig appConfig = new AppConfig();
        netty = new NettyJaxrsServer();
        ResteasyDeployment deploy = new ResteasyDeployment();
        List<Object> resources = new ArrayList<Object>();

        webappPort = Integer.valueOf(appConfig.getProperty("service.port"));
        resources.add(new TestService());
        deploy.setResources(resources);
        netty.setDeployment(deploy);
        netty.setPort(webappPort);
        netty.setRootResourcePath(APP_CONTEXT);
        netty.setSecurityDomain(null);
        System.out.println("Starting netty on port:" +netty.getPort());
        System.out.println("Try:  wget http://localhost:" + webappPort + APP_CONTEXT +"/test");
        netty.start();
    }
}
