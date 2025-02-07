package net.whydah.admin.user;

import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.whydah.admin.util.HttpConnectionHelper;
import net.whydah.admin.util.HttpConnectionHelper.Response;

@Component
public class USSReporter {

	private static final Logger log = LoggerFactory.getLogger(USSReporter.class);

	private String USS_URL = null;
	private String USS_ACCESSTOKEN = null;

	@Autowired
	@Configure
	public USSReporter(@Configuration("uss.url") String ussURL,
			@Configuration("uss.accesstoken") String ussAccessToken) {
		this.USS_URL = ussURL;
		this.USS_ACCESSTOKEN = ussAccessToken;
	}

	public void sendDeleteEventToUSS(String uid) {
		try {
			
			Response res = HttpConnectionHelper.delete(USS_URL.replaceFirst("/$", "") + "/api/" + USS_ACCESSTOKEN + "/delete/" + uid);
			log.debug("response {} from USS", res.getContent());

		} catch (Exception ex) {
			log.error("unexpected error", ex);
		}
	}

}
