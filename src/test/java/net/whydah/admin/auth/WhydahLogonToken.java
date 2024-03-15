package net.whydah.admin.auth;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Date;

/**
 * Loosely based upon code from Gunnar Skjold (Origin AS)
 * @author Gunnar Skjold
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@XmlRootElement(name = "applicationtoken")
public class WhydahLogonToken {
    private static final Logger log = LoggerFactory.getLogger(WhydahLogonToken.class);

	private WhydahTokenParams params;

	protected WhydahLogonToken() {
	}

	@XmlElement(name = "params")
	protected WhydahTokenParams getParams() {
        if (params == null) {
            setParams(new WhydahTokenParams());
        }
		return params;
	}

	protected void setParams(WhydahTokenParams params) {
		this.params = params;
	}


	public String getApplicationtokenID() {
		return getParams().getApplicationtokenID();
	}

	public String getApplicationid() {
		return getParams().getApplicationid();
	}

	public String getApplicationname() {
        return getParams().getApplicationname();
	}

	public Date getExpires() {
        Date expires = getParams().getExpires();
        if (expires == null) {
            expires = new Date(0);
        }
		return expires;
	}

	public boolean isExpired() {
		return params == null ? true : params.expires == null ? true : params.expires.before(new Date());
	}

    public static WhydahLogonToken fromXml(String logonResult) {
        log.trace("Try to build xml from {}", logonResult);

        WhydahLogonToken logonToken = new WhydahLogonToken();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(logonResult)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            String applicationtokenId = (String) xPath.evaluate("/applicationtoken/params/applicationtokenID", doc, XPathConstants.STRING);
            logonToken.getParams().setApplicationtokenID(applicationtokenId);
            String applicationid = (String) xPath.evaluate("/applicationtoken/params/applicationid", doc, XPathConstants.STRING);
            logonToken.getParams().setApplicationid(applicationid);
            String applicationName = (String) xPath.evaluate("/applicationtoken/params/applicationname", doc, XPathConstants.STRING);
            logonToken.getParams().setApplicationname(applicationName);
            String expires = (String ) xPath.evaluate("/applicationtoken/params/expires", doc, XPathConstants.STRING);
            if (expires != null && !expires.isEmpty()) {
                Long longDate = new Long(expires);
                logonToken.getParams().setExpires(new Date(longDate));
            }
            //FIXME - applicatons
            /*
            <applications>
    <application>
        <appId>21</appId>
        <applicationName>ReceiptControlAdmin</applicationName>
        <orgName>Developer</orgName>
        <roleName>ROLE_ADMIN</roleName>
        <roleValue>1</roleValue>
    </application>
</applications>
             */


        } catch (Exception e) {
            log.warn("Could not create an WhydahLogonToken from this xml {}", logonResult, e);
        }
        return logonToken;
    }

    public String toXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
                " <applicationtoken>\n" +
                "     <params>\n" +
                "         <applicationtoken>" + params.getApplicationtokenID() + "</applicationtoken>\n" +
                "         <applicationid>" + params.getApplicationid() + "</applicationid>\n" +
                "         <applicationname>" + params.getApplicationname() + "</applicationname>\n" +
                "         <expires>" + params.getExpires().getTime() + "</expires>\n" +
                "     </params> \n" +
                " </applicationtoken>\n";
    }

    private static class WhydahTokenParams {
		private String applicationtokenID, applicationid, applicationname;
		private Date expires;

		public String getApplicationtokenID() {
			return applicationtokenID;
		}

		public void setApplicationtokenID(String applicationtokenID) {
			this.applicationtokenID = applicationtokenID;
		}

		public String getApplicationid() {
			return applicationid;
		}

		public void setApplicationid(String applicationid) {
			this.applicationid = applicationid;
		}

		public String getApplicationname() {
			return applicationname;
		}

		public void setApplicationname(String applicationname) {
			this.applicationname = applicationname;
		}

		public Date getExpires() {
			return expires;
		}

		public void setExpires(Date expires) {
			this.expires = expires;
		}
	}
}
