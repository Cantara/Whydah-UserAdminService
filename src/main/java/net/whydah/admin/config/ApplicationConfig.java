package net.whydah.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@PropertySources({
    @PropertySource("classpath:useradminservice.properties"),
    @PropertySource(value = "file:./useradminservice_override.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:./config/useradminservice_override.properties", ignoreResourceNotFound = true)
})
public class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
    
    @Autowired
    private Environment env;
    
    @Bean
    public Properties applicationProperties() {
        Properties props = new Properties();
        
        // Load all relevant properties from Environment into the Properties object
        for (String key : new String[] {
                "service.port", "myuri", "useridentitybackend",
                "securitytokenservice", "applicationid", "applicationname",
                "sslverification", "valuereporter.host", "valuereporter.port"
        }) {
            String value = env.getProperty(key);
            if (value != null) {
                props.setProperty(key, value);
                log.info("Loaded property: {} = {}", key, key.contains("secret") ? "********" : value);
            } else {
                log.warn("Property not found: {}", key);
            }
        }
        
        return props;
    }
    
    @Bean
    public ApplicationPropertiesHolder propertiesHolder() {
        return new ApplicationPropertiesHolder(applicationProperties());
    }
    
    // Helper class to provide static access to properties
    public static class ApplicationPropertiesHolder {
        private static Properties properties;
        
        public ApplicationPropertiesHolder(Properties props) {
            properties = props;
            log.info("ApplicationPropertiesHolder initialized with {} properties", props.size());
        }
        
        public static String getProperty(String key) {
            return properties != null ? properties.getProperty(key) : null;
        }
        
        public static Properties getProperties() {
            return properties;
        }
    }
}