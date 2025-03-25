package net.whydah.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backward compatibility class that delegates to SpringProperties.
 * This allows existing code to continue using ConfigValues.
 */
public class ConfigValues {

	private static final Logger log = LoggerFactory.getLogger(ConfigValues.class);

	static {
		log.info("ConfigValues is now a delegate to SpringProperties");
	}

	public static String getString(String configKey) {
		return SpringProperties.getString(configKey);
	}

	public static <K> K get(String configKey, K defaultValue) {
		return SpringProperties.get(configKey, defaultValue);
	}
}