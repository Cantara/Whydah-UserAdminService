package net.whydah.admin.user.uib;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 08/04/14
 */
@Deprecated // Use UserAggregateMapper
public class RoleRepresentationRequest {
    private static final Logger log = LoggerFactory.getLogger(RoleRepresentationRequest.class);
    private String applicationId;
    private String applicationName;

    private String organizationName;

    private String applicationRoleName;
    private String applicationRoleValue;


    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    public void setApplicationRoleName(String applicationRoleName) {
        this.applicationRoleName = applicationRoleName;
    }
    public void setApplicationRoleValue(String applicationRoleValue) {
        this.applicationRoleValue = applicationRoleValue;
    }


    public String getApplicationId() {
        return applicationId;
    }
    public String getApplicationName() {
        return applicationName;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public String getApplicationRoleName() {
        return applicationRoleName;
    }
    public String getApplicationRoleValue() {
        return applicationRoleValue;
    }

    public String toJson() {
        String userJson = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            userJson =  mapper.writeValueAsString(this);
            log.debug("toJson: {}", userJson);
        } catch (IOException e) {
            log.info("Could not create json from this object {}", toString(), e);
        }
        return userJson;
    }

    public static RoleRepresentationRequest fromJson(String roleJson) {
        RoleRepresentationRequest roleRepresentation = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            roleRepresentation = mapper.readValue(roleJson, RoleRepresentationRequest.class);

        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("Error mapping json for " + roleJson, e);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Error parsing json for " + roleJson, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading json for " + roleJson, e);
        }
        return roleRepresentation;
    }

//    public String toJson() {
//        /* return "{\"roleId\":\""+ getId() +"\"," +
//                "\"uid\":\""+ getUid() +"\"," +
//                "\"applicationId\":\""+ getApplicationId() +"\"," +
//                "\"applicationName\":\"" + getApplicationName() + "\","+
//                "\"applicationRoleName\":\""+ getApplicationRoleName() +"\"," +
//                "\"applicationRoleValue\":\""+ getApplicationRoleValue() +"\"," +
//                "\"organizationName\":\""+ getOrganizationName() +"\"}";
//                */
//        String json =  "{\"applicationId\":\""+ getApplicationId() +"\"," +
//                "\"applicationName\":\"" + getApplicationName() + "\","+
//                "\"applicationRoleName\":\""+ getApplicationRoleName() +"\"," +
//                "\"applicationRoleValue\":\""+ getApplicationRoleValue() +"\"," +
//                "\"organizationName\":\""+ getOrganizationName() +"\"}";
//
//        return json;
//
//    }

    public static RoleRepresentationRequest fromXml(String roleXml) {
        log.debug("Build UserPropertyAndRole from xml {}", roleXml);
        RoleRepresentationRequest userPropertyAndRole = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(roleXml)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            String appId = (String) xPath.evaluate("/application/appId", doc, XPathConstants.STRING);
            String appName = (String) xPath.evaluate("/application/applicationName", doc, XPathConstants.STRING);
            String orgID = (String) xPath.evaluate("/application/orgID", doc, XPathConstants.STRING);
            String orgName = (String) xPath.evaluate("/application/orgName", doc, XPathConstants.STRING);
            String roleName = (String) xPath.evaluate("/application/roleName", doc, XPathConstants.STRING);
            String roleValue = (String) xPath.evaluate("/application/roleValue", doc, XPathConstants.STRING);

            userPropertyAndRole = new RoleRepresentationRequest();
            userPropertyAndRole.setApplicationId(appId);
            userPropertyAndRole.setApplicationName(appName);
            userPropertyAndRole.setOrganizationName(orgName);
            userPropertyAndRole.setApplicationRoleName(roleName);
            userPropertyAndRole.setApplicationRoleValue(roleValue);

        } catch (Exception e) {
            log.warn("Could not create an UserPropertyAndRole from this xml {}", roleXml, e);
        }
        return userPropertyAndRole;

    }
}
