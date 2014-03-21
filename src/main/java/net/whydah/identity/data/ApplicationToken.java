package net.whydah.identity.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: totto
 * Date: Nov 4, 2010
 * <p/>
 * Time: 8:54:00 AM
 */

public class ApplicationToken {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationToken.class);

    private String applicationTokenId = "";
    private String applicationSecret = "test";
    private String applicationName = "Whydah";
    private String expires = "";
    private String baseuri = "http://example.com//";

    public String getBaseuri() {
        return baseuri;
    }

    public void setBaseuri(String baseuri) {
        this.baseuri = baseuri;
    }

    private boolean template = true;

    public ApplicationToken() {
        applicationTokenId = UUID.randomUUID().toString();
        expires = String.valueOf((System.currentTimeMillis() + 10000));
    }

    public ApplicationToken(String xml) {
        applicationTokenId = getApplicationToken(getApplicationName(xml));  // "ERST677hjS"
        applicationSecret = getApplicationSecret(xml);  // "ERST677hjS"
        applicationName = getApplicationName(xml);
        expires = String.valueOf((System.currentTimeMillis() + 10000));
        template = false;
    }

    public String toXML() {
        if (template) {
            return templateToken;
        } else {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
                    " <token>\n" +
                    "     <params>\n" +
                    "         <applicationtoken>" + applicationTokenId + "</applicationtoken>\n" +
                    "         <applicationid>" + "23" + "</applicationid>\n" +
                    "         <applicationname>" + applicationName + "</applicationname>\n" +
                    "         <expires>" + expires + "</expires>\n" +
                    "     </params> \n" +
                    "     <Url type=\"application/xml\" method=\"POST\" " +
                    "                template=\"" + baseuri + "/iam/" + applicationTokenId + "/getusertokenbytokenid\"/> \n" +
                    " </token>\n";
        }
    }

    private final String templateToken = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +

            "    <token>\n" +
            "        <params>\n" +
            "            <applicationtoken>" + applicationTokenId + "</applicationtoken>\n" +
            "            <applicationid>" + "23" + "</applicationid>\n" +
            "            <applicationname>" + applicationName + "</applicationname>\n" +
            "            <expires>" + expires + "</expires>\n" +
            "        </params> \n" +
            "           <Url type=\"application/xml\"" +
            "                template=\"http://example.com/iam/{applicationtoken}/getusertokenbytokenid\"/>" +
            "    </token>\n";

    public String getApplicationTokenId() {
        return applicationTokenId;
    }


    public String getExpires() {
        return expires;
    }


    public String getApplicationID() {
        return applicationName;
    }

    private String getApplicationName(String applicationCredentialXML) {
        logger.debug("Her: {}", applicationCredentialXML);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(applicationCredentialXML)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "/applicationcredential/*/applicationID[1]";
            XPathExpression xPathExpression = xPath.compile(expression);
            String appId = xPathExpression.evaluate(doc);
            logger.debug("XML parse: id = {}", appId);
            return appId;
        } catch (Exception e) {
            logger.error("Could not get applicationID from XML: " + applicationCredentialXML, e);
        }
        return "</applicationID>";
    }

    private String getApplicationSecret(String applicationCredentialXML) {
        logger.debug("Her: {}", applicationCredentialXML);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(applicationCredentialXML)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "/applicationcredential/*/applicationSecret[1]";
            XPathExpression xPathExpression = xPath.compile(expression);
            String appId = xPathExpression.evaluate(doc);
            logger.debug("XML parse: id = {}", appId);
            return appId;
        } catch (Exception e) {
            logger.error("Could not get applicationID from XML: " + applicationCredentialXML, e);
        }
        return "</applicationSecret>";
    }

    private String getApplicationToken(String appID) {
        return getMD5hash(appID + expires);
    }

    public String getMD5hash(String t) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(t.getBytes());
            byte[] h = digest.digest();
            return getHexString(h);
        } catch (Exception e) {
            logger.error("Could not get MD5 hash for string " + t, e);
        }
        return "";
    }

    public String getHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte aB : b) {
            result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
