package net.whydah.admin.application;

import com.google.common.base.Joiner;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Application.
 *
 * Erik's notes:
 *
 * applications
 *      application, applicationID, name
 *          relations   (organisation)
 *              relation1, id, name
 *                  properties / hashmap (mappet tidligere til roller)
 *              relation2, id, name
 *                  properties / hashmap
 *
 */
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private String id;
    private String name;
    private String defaultRole;
    private String defaultOrgid;
    private List<String> availableOrgIds = new ArrayList<>();

    private Application() {
    }

    public Application(String id, String name) {
        this(id, name, null, null);
    }
    public Application(String id, String name, String defaultRole, String defaultOrgid) {
        this.id = id;
        this.name = name;
        this.defaultRole = defaultRole;
        this.defaultOrgid = defaultOrgid;
    }

    public Application(String id, String name, String defaultRole, String defaultOrgid, List<String> availableOrgIds) {
        this.id = id;
        this.name = name;
        this.defaultRole = defaultRole;
        this.defaultOrgid = defaultOrgid;
        setAvailableOrgIds(availableOrgIds);
    }

    /**
     * {
     "id": "id1",
     "name": "test",
     "defaultRole": "default1role",
     "defaultOrgid": "defaultorgid",
     "availableOrgIds": [
     "developer@customer",
     "consultant@customer"
     ]
     }
     * @param applicationJson
     * @return
     */
    public static Application fromJson(String applicationJson) {
        try {
            Application application;

            ObjectMapper mapper = new ObjectMapper();
            application = mapper.readValue(applicationJson, Application.class);

            return application;
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("Error mapping json for " + applicationJson, e);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Error parsing json for " + applicationJson, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading json for " + applicationJson, e);
        }
    }

    public String toJson() {
        String applicationJson = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            applicationJson =  mapper.writeValueAsString(this);
        } catch (IOException e) {
            log.info("Could not create json from this object {}", toString(), e);
        }
        return applicationJson;
    }

    public String toXML() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
                " <application>\n" +
                "   <applicationid>" + id + "</applicationid>\n" +
                "   <applicationname>" + name + "</applicationname>\n" +
                "   <defaultrole>" + defaultRole + "</defaultrole>\n" +
                "   <defaultorgid>" + defaultOrgid + "</defaultorgid>\n" +
                "  " + buildAvailableOrgIsXml() + "\n" +
                " </application>\n";
    }

    private String buildAvailableOrgIsXml() {
        StringBuilder availableXml = new StringBuilder("<availableOrgIs>\n");

        for (String availableOrgId : availableOrgIds) {
            availableXml.append("<orgId>" + availableOrgId + "</orgId>\n");
        }
        availableXml.append("</availableOrgIs>");
        return availableXml.toString();
    }

    public static Application fromXml(String applicationXml) {
        log.debug("Build application from xml {}", applicationXml);
        Application application = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(applicationXml)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            String id = (String) xPath.evaluate("/application/applicationid", doc, XPathConstants.STRING);
            String name = (String) xPath.evaluate("/application/applicationname", doc, XPathConstants.STRING);
            String defaultrole = (String) xPath.evaluate("/application/defaultrole", doc, XPathConstants.STRING);
            String defaultorgid = (String) xPath.evaluate("/application/defaultorgid", doc, XPathConstants.STRING);
            NodeList availableOrgIds = (NodeList) xPath.evaluate("/application/availableOrgIs/orgId", doc, XPathConstants.NODESET);

            application = new Application(id,name,defaultrole, defaultorgid);
            if (availableOrgIds != null && availableOrgIds.getLength() > 0) {
                for (int i = 0; i < availableOrgIds.getLength(); i++) {
                    Node node = availableOrgIds.item(i);
                    XPathExpression pathExpr = xPath.compile(".");
                    String orgId = (String) pathExpr.evaluate(node, XPathConstants.STRING);
                    log.debug("orgId {}", orgId);
                    application.addAvailableOrgId(orgId);
                }
            }
        } catch (Exception e) {
            log.warn("Could not create an Application from this xml {}", applicationXml, e);
        }
        return application;

    }



    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDefaultRole() {
        return defaultRole;
    }
    public String getDefaultOrgid() {
        return defaultOrgid;
    }

    public List<String> getAvailableOrgIds() {
        return availableOrgIds;
    }

    public void setAvailableOrgIds(List<String> availableOrgIds) {
        if (availableOrgIds != null) {
            this.availableOrgIds = availableOrgIds;
        }
    }

    public void addAvailableOrgId(String availableOrgId) {
        if (availableOrgIds == null) {
            availableOrgIds = new ArrayList();
        }
        if (availableOrgId != null) {
            this.availableOrgIds.add(availableOrgId);
        }
    }

    public void removeAvailableOrgId(String availableOrgId) {
        if (availableOrgIds != null && availableOrgId != null) {
            availableOrgIds.remove(availableOrgId);
        }
    }

    public void setDefaultOrgid(String defaultOrgid) {
        this.defaultOrgid = defaultOrgid;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    @Override
    public String toString() {
        String availableIdsString = "";
         if (this.availableOrgIds != null) {
            availableIdsString = Joiner.on(", ").join(this.availableOrgIds);
         }
        return "Application{" +
                "appId='" + id + '\'' +
                ", name='" + name + '\'' +
                ", defaultrole='" + defaultRole + '\'' +
                ", defaultOrgid='" + defaultOrgid + '\'' +
                ", availableOrgIds = '" + availableIdsString  + '\'' +
                '}';
    }

    public void setId(String id) {
        this.id = id;
    }


}
