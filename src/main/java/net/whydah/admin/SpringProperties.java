package net.whydah.admin;

import net.whydah.admin.config.ApplicationConfig.ApplicationPropertiesHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringProperties {
    private static final Logger log = LoggerFactory.getLogger(SpringProperties.class);
    private static Environment environment;
    
    @Autowired
    public SpringProperties(Environment env) {
        environment = env;
        log.info("SpringProperties initialized with Environment");
    }
    
    public String getString(String configKey) {
        // First check static holder
        String value = ApplicationPropertiesHolder.getProperty(configKey);
        if (value != null) {
            return value;
        }
        
        // Then check system properties
        value = System.getProperty(configKey);
        if (value != null) {
            return value;
        }
        
        // Finally try environment if available
        if (environment != null) {
            return environment.getProperty(configKey);
        }
        
        log.warn("Property not found: {}", configKey);
        return null;
    }
    
    public <K> K get(String configKey, K defaultValue) {
        String value = getString(configKey);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return convertStringToType(value, defaultValue);
        } catch (Exception e) {
            log.warn("Could not convert property '{}' with value '{}' to type {}, using default",
                    configKey, value, defaultValue.getClass().getSimpleName());
            return defaultValue;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <K> K convertStringToType(String value, K defaultValue) {
        if (defaultValue instanceof Integer) {
            return (K) Integer.valueOf(value);
        } else if (defaultValue instanceof Long) {
            return (K) Long.valueOf(value);
        } else if (defaultValue instanceof Boolean) {
            return (K) Boolean.valueOf(value);
        } else if (defaultValue instanceof Double) {
            return (K) Double.valueOf(value);
        } else if (defaultValue instanceof Float) {
            return (K) Float.valueOf(value);
        } else {
            return (K) value;
        }
    }
}