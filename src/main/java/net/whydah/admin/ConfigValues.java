package net.whydah.admin;

import java.util.Map;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigValues {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigValues.class);
	private static final ConstrettoConfiguration configuration;
	
	static {
		 configuration = new ConstrettoBuilder()
	                .createPropertiesStore()
	                .addResource(Resource.create("classpath:useradminservice.properties"))
	                .addResource(Resource.create("file:./useradminservice_override.properties"))
	                .done()
	                .getConfiguration();
		 
		 printConfiguration(configuration);

	}
	
	private static void printConfiguration(ConstrettoConfiguration configuration) {
		Map<String, String> properties = configuration.asMap();
		for (String key : properties.keySet()) {
			log.info("Using Property: {}, value: {}", key, properties.get(key));
		}
	}
	
	public static String getString(String configKey) {
		return configuration.evaluateToString(configKey);
	}
	
	public static <K> K get(String configKey, K defaultValue) {
		if(configuration.hasValue(configKey)) {
			return configuration.evaluateTo(configKey, defaultValue);
		} else {
			return defaultValue;
		}
	}

}
