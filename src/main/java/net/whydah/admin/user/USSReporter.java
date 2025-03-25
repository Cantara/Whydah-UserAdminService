package net.whydah.admin.user;

import net.whydah.admin.util.HttpConnectionHelper;
import net.whydah.admin.util.HttpConnectionHelper.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class USSReporter {

	private static final Logger log = LoggerFactory.getLogger(USSReporter.class);

	private String USS_URL = null;
	private String USS_ACCESSTOKEN = null;

	@Autowired
	public USSReporter(@Value("${uss.url}") String ussURL,
					   @Value("${uss.accesstoken}") String ussAccessToken) {
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