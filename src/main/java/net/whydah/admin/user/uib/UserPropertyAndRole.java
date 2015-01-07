package net.whydah.admin.user.uib;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * A Java representation for a user's properties and roles as it is stored in a RDBMS.
 *
 * A user (uid)
 * has the role (roleName, roleValue)
 * in this application (applicationId, applicationName)
 * from the relation (organizationId, organizationName).
 *
 * An example to illustrate why the context/relation/organization concept is needed:
 *
 * As an employee (relation: organizationId, organizationName),
 * a user (uid)
 * has the "reader" role in
 * application CompanyCMS.
 *
 * As technical writer in the Company (relation: organizationId, organizationName),
 * a user (uid)
 * has the "writer" role in
 * application CompanyCMS.
 *
 * @author totto
 * @since 1/11/11
 */
public class UserPropertyAndRole {

    private static final Logger log = LoggerFactory.getLogger(UserPropertyAndRole.class);
    private String id;

    private String uid;

    private String applicationId;
    private transient String applicationName;

    private transient String organizationName;

    private String applicationRoleName;
    private String applicationRoleValue;

    public UserPropertyAndRole() {
    }

    public UserPropertyAndRole(String id, String uid, String applicationId, String applicationName, String organizationName, String applicationRoleName, String applicationRoleValue) {
        this.id = id;
        this.uid = uid;
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.organizationName = organizationName;
        this.applicationRoleName = applicationRoleName;
        this.applicationRoleValue = applicationRoleValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserPropertyAndRole that = (UserPropertyAndRole) o;

        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null) {
            return false;
        }
        if (organizationName != null ? !organizationName.equals(that.organizationName) : that.organizationName != null) {
            return false;
        }
        if (applicationRoleName != null ? !applicationRoleName.equals(that.applicationRoleName) : that.applicationRoleName != null) {
            return false;
        }
        if (applicationRoleValue != null ? !applicationRoleValue.equals(that.applicationRoleValue) : that.applicationRoleValue != null) {
            return false;
        }
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        result = 31 * result + (organizationName != null ? organizationName.hashCode() : 0);
        result = 31 * result + (applicationRoleName != null ? applicationRoleName.hashCode() : 0);
        result = 31 * result + (applicationRoleValue != null ? applicationRoleValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserPropertyAndRole{" +
                "uid='" + uid + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", roleName='" + applicationRoleName + '\'' +
                ", roleValue='" + applicationRoleValue + '\'' +
                '}';
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    /*public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    public void setApplicationRoleName(String applicationRoleName) {
        this.applicationRoleName = applicationRoleName;
    }
    public void setApplicationRoleValue(String applicationRoleValue) {
        this.applicationRoleValue = applicationRoleValue;
    }

    public String getId() {
        return id;
    }
    public String getUid() {
        return uid;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public String getApplicationName() {
        return (applicationName == null ? "" : applicationName);
    }

    public String getOrganizationName() {
        return (organizationName == null ? "" : organizationName);
    }
    public String getApplicationRoleName() {
        return applicationRoleName;
    }
    public String getApplicationRoleValue() {
        return applicationRoleValue;
    }

    public static UserPropertyAndRole fromXml(String roleXml) {
        log.debug("Build UserPropertyAndRole from xml {}", roleXml);
        UserPropertyAndRole userPropertyAndRole = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(roleXml)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            String id = (String) xPath.evaluate("/application/appId", doc, XPathConstants.STRING);
            String name = (String) xPath.evaluate("/application/applicationName", doc, XPathConstants.STRING);
            String orgID = (String) xPath.evaluate("/application/orgID", doc, XPathConstants.STRING);
            String roleName = (String) xPath.evaluate("/application/roleName", doc, XPathConstants.STRING);
            String roleValue = (String) xPath.evaluate("/application/roleValue", doc, XPathConstants.STRING);

            userPropertyAndRole = new UserPropertyAndRole();
            userPropertyAndRole.setId(id);
            userPropertyAndRole.setApplicationName(name);
            userPropertyAndRole.setApplicationRoleName(roleName);
            userPropertyAndRole.setApplicationRoleValue(roleValue);

        } catch (Exception e) {
            log.warn("Could not create an UserPropertyAndRole from this xml {}", roleXml, e);
        }
        return userPropertyAndRole;

    }


    public String toJson() {
        String propertyOrRoleJson = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            propertyOrRoleJson =  mapper.writeValueAsString(this);
        } catch (IOException e) {
            log.info("Could not create json from this object {}", toString(), e);
        }
        return propertyOrRoleJson;
    }
}
