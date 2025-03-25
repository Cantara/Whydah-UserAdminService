package net.whydah.admin.security;

import net.whydah.sso.application.mappers.ApplicationCredentialMapper;
import net.whydah.sso.application.types.ApplicationCredential;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 22.11.15.
 */
@Component
public class UASCredentials {
    private static final Logger log = getLogger(UASCredentials.class);

    public static final String APPLICATION_CREDENTIALS_HEADER_XML = "uas-app-credentials";
    private final String applicationId;
    private final String applicationName;
    private String applicationSecret;


    public UASCredentials(String applicationId, String applicationName) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
    }

    @Autowired
    public UASCredentials(@Value("${applicationid}") String applicationId, @Value("${applicationname}") String applicationName, @Value("${applicationsecret}") String applicationSecret) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.applicationSecret = applicationSecret;

    }

    public String getApplicationId() {
        return applicationId;
    }

    public ApplicationCredential getApplicationCredential() {
        ApplicationCredential uasApplicationCredential = new ApplicationCredential(applicationId, applicationName, applicationSecret);
        return uasApplicationCredential;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public String getApplicationCredentialsXml() {
        String applicationCredentialsXml =  ApplicationCredentialMapper.toXML(getApplicationCredential());
        return applicationCredentialsXml;
    } 

    public String getApplicationCredentialsXmlEncoded() {
        String encoded = "";
        String unencoded = getApplicationCredentialsXml();
        if (unencoded != null) {
            try {
                encoded = URLEncoder.encode(unencoded,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn("Failed to encode {}. reason {}. This will prevent UAS talking to UIB!");
            }
        }
        return encoded;
    }

    public static String encode(String uasAppCredentialXml) {
        String encoded = "";
        if (uasAppCredentialXml != null) {
            try {
                encoded = URLEncoder.encode(uasAppCredentialXml,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn("Failed to encode {}. reason {}. This will prevent UAS talking to UIB!");
            }
        }
        return encoded;
    }
}
