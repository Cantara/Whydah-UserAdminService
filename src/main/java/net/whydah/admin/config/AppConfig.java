package net.whydah.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper methods for reading configurration.
 */
@Component
public class AppConfig {
    public final static String IAM_CONFIG_KEY = "IAM_CONFIG";
    private final static Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final Properties properties;

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public AppConfig() {
        try {
            properties = readProperties(ApplicationMode.getApplicationMode());

            // Property-overwrite of SSL verification to support weak ssl certificates
            if ("disabled".equalsIgnoreCase(properties.getProperty("sslverification"))) {
                SSLTool.disableCertificateValidation();

            }
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private static Properties readProperties(String appMode) throws IOException {
        Properties properties = loadFromClasspath(appMode);

        String configfilename = System.getProperty(IAM_CONFIG_KEY);
        if(configfilename != null) {
            loadFromFile(properties, configfilename);
        }
        return properties;
    }

    private static Properties loadFromClasspath(String appMode) throws IOException {
        Properties properties = new Properties();
        String propertyfile = String.format("useradminservice.%s.properties", appMode);
        logger.info("Loading properties from classpath: {}", propertyfile);
        InputStream is = AppConfig.class.getClassLoader().getResourceAsStream(propertyfile);
        if(is == null) {
            logger.error("Error reading {} from classpath.", propertyfile);
            System.exit(3);
        }
        properties.load(is);
        return properties;
    }

    private static void loadFromFile(Properties properties, String configfilename) throws IOException {
        File file = new File(configfilename);
        //System.out.println(file.getAbsolutePath());
        logger.info("Overriding defaults from property file {}", file.getAbsolutePath());
        if(file.exists()) {
            properties.load(new FileInputStream(file));
        } else {
            logger.error("Config file {} specified by System property {} not found.", configfilename, IAM_CONFIG_KEY);
            System.exit(3);
        }
    }
}
