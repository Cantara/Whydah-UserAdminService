package net.whydah.admin;

import jakarta.annotation.PostConstruct;
import net.whydah.sso.config.ApplicationMode;
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
    private static ApplicationContext applicationContext;

    @Autowired
    public SpringProperties(Environment env, ApplicationContext appContext) {
        environment = env;
        applicationContext = appContext;
    }

    @PostConstruct
    public void init() {
        // Log important properties for debugging
        if (log.isDebugEnabled()) {
            log.debug("Application initialized with properties:");
            // Log specific important properties
            String[] keysToLog = {
                    "service.port", "myuri", "useridentitybackend",
                    "securitytokenservice", "applicationid", "applicationname",
                    "sslverification", ApplicationMode.IAM_MODE_KEY
            };

            for (String key : keysToLog) {
                String value = environment.getProperty(key);
                if (value != null) {
                    if (key.contains("secret") || key.contains("password")) {
                        log.debug("  {} = ********", key);
                    } else {
                        log.debug("  {} = {}", key, value);
                    }
                } else {
                    log.warn("  {} = <not set>", key);
                }
            }
        }
    }

    /**
     * Get a string configuration value.
     *
     * @param configKey the configuration key
     * @return the string value or null if not found
     */
    public static String getString(String configKey) {
        // First check system properties (useful for testing)
        String sysProp = System.getProperty(configKey);
        if (sysProp != null) {
            return sysProp;
        }

        if (environment == null) {
            log.warn("Environment not initialized yet when trying to get property: {}", configKey);
            return null;
        }
        return environment.getProperty(configKey);
    }

    /**
     * Get a configuration value with a default.
     *
     * @param configKey    the configuration key
     * @param defaultValue the default value to return if key not found
     * @param <K>          the type of the value
     * @return the value or default if not found
     */
    public static <K> K get(String configKey, K defaultValue) {
        // First check system properties (useful for testing)
        String sysProp = System.getProperty(configKey);
        if (sysProp != null) {
            try {
                return convertStringToType(sysProp, defaultValue);
            } catch (Exception e) {
                log.warn("Failed to convert system property {} with value '{}' to type {}",
                        configKey, sysProp, defaultValue.getClass().getSimpleName(), e);
            }
        }

        if (environment == null) {
            log.warn("Environment not initialized yet when trying to get property: {}", configKey);
            return defaultValue;
        }

        String value = environment.getProperty(configKey);
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
    private static <K> K convertStringToType(String value, K defaultValue) {
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
            // For String and other types, just return the value
            return (K) value;
        }
    }
}