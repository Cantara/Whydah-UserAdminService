package net.whydah.errorhandling;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.model.Resource;

public class Configuration {
	
	public static final ConstrettoConfiguration configuration = new ConstrettoBuilder()
            .createPropertiesStore()
            .addResource(Resource.create("classpath:useradminservice.properties"))
            .addResource(Resource.create("file:./useradminservice_override.properties"))
            .done()
            .getConfiguration();
	
	private Configuration() {}
	
	public static String getString(String key) {
		return configuration.evaluateToString(key);
	}
	
	public static Integer getInt(String key) {
		return configuration.evaluateToInt(key);
	}

	public static Integer getInt(String key, int defaultValue) {
		return configuration.evaluateTo(key, defaultValue);
	}

	public static boolean getBoolean(String key) {
		return configuration.evaluateToBoolean(key);
	}
	
	
}