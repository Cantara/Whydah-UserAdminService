package net.whydah.admin.user.uib;

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
import java.util.ArrayList;
import java.util.List;

public class UserAggregate {
    private static final Logger log = LoggerFactory.getLogger(UserAggregate.class);
    private UserIdentity identity = null;
    private List<UserPropertyAndRole> roles = new ArrayList<>();

    public UserAggregate(UserIdentity identity, List<UserPropertyAndRole> roles) {
        this.identity = identity;
        this.roles = roles;
    }

    public UserAggregate() {
    }

    public String toXML() {
        StringBuilder strb = new StringBuilder();
        String headAndIdentity = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<whydahuser>\n" +
                "    <identity>\n" +
                "        <username>" + identity.getUsername() + "</username>\n" +
                "        <cellPhone>" + (identity.getCellPhone() != null ? identity.getCellPhone() : "") + "</cellPhone>\n" +
                "        <email>" + identity.getEmail() + "</email>\n" +
                "        <firstname>" + identity.getFirstName() +"</firstname>\n" +
                "        <lastname>" + identity.getLastName() + "</lastname>\n" +
                "        <personRef>" + (identity.getPersonRef() != null ? identity.getPersonRef() : "") + "</personRef>\n" +
                "        <UID>" + identity.getUid() + "</UID>\n" +
                "    </identity>\n" +
                "    <applications>\n";
         strb.append(headAndIdentity);

        for (UserPropertyAndRole u : roles) {
                    strb.append(
                    "        <application>\n" +
                    "            <appId>" + u.getApplicationId() + "</appId>\n" +
                    "            <applicationName>" + u.getApplicationName() + "</applicationName>\n" +
                    "            <roleName>" + u.getApplicationRoleName() + "</roleName>\n" +
                    "            <roleValue>" + u.getApplicationRoleValue() + "</roleValue>\n" +
                    "        </application>\n"
                    );
        }
        strb.append(
                "    </applications>\n" +
                "</whydahuser>"
        );
        return strb.toString();
    }

    /**
     * Copy from UserToken.parseAndUpdatefromUserIdentity from SecurityTokenService
     */
    public static UserAggregate fromXML(String userIdentityXML) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(userIdentityXML)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            String uid = (String) xPath.evaluate("//identity/UID", doc, XPathConstants.STRING);
            String userName = (String) xPath.evaluate("//identity/username", doc, XPathConstants.STRING);
            String firstName = (String) xPath.evaluate("//identity/firstname", doc, XPathConstants.STRING);
            String lastName = (String) xPath.evaluate("//lastname", doc, XPathConstants.STRING);
            String email = (String) xPath.evaluate("//email", doc, XPathConstants.STRING);
            String personRef = (String) xPath.evaluate("//personRef", doc, XPathConstants.STRING);
            String cellPhone = (String) xPath.evaluate("//cellPhone", doc, XPathConstants.STRING);

            UserIdentity identity = new UserIdentity();
            identity.setUid(uid);
            identity.setUsername(userName);
            identity.setFirstName(firstName);
            identity.setLastName(lastName);
            identity.setEmail(email);
            identity.setPersonRef(personRef);
            identity.setCellPhone(cellPhone);

            /*
            NodeList applicationNodes = (NodeList) xPath.evaluate("//application", doc, XPathConstants.NODESET);
            for(int i=0; i<applicationNodes.getLength(); i++) {
                Node appNode = applicationNodes.item(i);
                NodeList children = appNode.getChildNodes();
                HashMap<String, String> values = getAppValues(children);
                //putApplicationCompanyRoleValue(values.get("appId"), values.get("applicationName"), values.get("orgID"), values.get("organizationName"), values.get("roleName"), values.get("roleValue"));
            }
            */
            return new UserAggregate(identity, null);
        } catch (Exception e) {
            //log.error("Error parsing userIdentityXML " + userIdentityXML, e);
        }
        return null;
    }

     /*
    private static HashMap<String, String> getAppValues(NodeList children) {
        HashMap<String, String> values = new HashMap<>();
        for(int j=0; j < children.getLength(); j++) {
            Node node = children.item(j);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                values.put(node.getNodeName(), node.getTextContent());
            }
        }
        return values;
    }

    public void putApplicationCompanyRoleValue(String p_application_ID, String p_application_Name, String p_company_ID, String p_company_name, String p_role, String p_value) {
        if (applicationCompanyRoleValueMap.containsKey(p_application_ID)) {
            ApplicationData application = applicationCompanyRoleValueMap.get(p_application_ID);
            CompanyRoles company = application.getCompaniesAndRolesMap().get(p_company_ID);
            if (company != null) {  // Application and company exists, just update the rolemap
                company.getRoleMap().put(p_role, p_value);
            } else {
                company = new CompanyRoles();
                company.setCompanyNumber(p_company_ID);
                company.setCompanyName(p_company_name);
                Map<String, String> rolemap = new HashMap<String, String>();
                rolemap.put(p_role, p_value);
                company.setRoleMap(rolemap);
                application.addCompanyWithRoles(company.getCompanyNumber(), company);
                applicationCompanyRoleValueMap.put(application.getApplicationID(), application);
            }
            // Add or update existing application
        } else {
            ApplicationData application = new ApplicationData();
            application.setApplicationID(p_application_ID);
            application.setApplicationName(p_application_Name);
            CompanyRoles company = new CompanyRoles();
            company.setCompanyNumber(p_company_ID);
            company.setCompanyName(p_company_name);
            Map<String,String> rolemap = new HashMap<String, String>();
            rolemap.put(p_role, p_value);
            company.setRoleMap(rolemap);
            application.addCompanyWithRoles(company.getCompanyNumber(), company);
            applicationCompanyRoleValueMap.put(application.getApplicationID(), application);
        }
    }
    */



    public UserIdentity getIdentity() {
        return identity;
    }
    public void setIdentity(UserIdentity identity) {
        this.identity = identity;
    }


    public String getPersonName() {
        return identity.getFirstName()+ ' ' + identity.getLastName();
    }
    public String getPersonRef() {
        return identity.getPersonRef();
    }
    public String getUid() {
        return identity.getUid();
    }
    public String getUsername() {
        return identity.getUsername();
    }
    public String getFirstName() {
        return identity.getFirstName();
    }
    public String getLastName() {
        return identity.getLastName();
    }
    public String getEmail() {
        return identity.getEmail();
    }
    public String getCellPhone() {
        return identity.getCellPhone();
    }


    public List<UserPropertyAndRole> getRoles() {
        return roles;
    }


    public void addRole(UserPropertyAndRole role) {
        this.roles.add(role);
    }

    public void setPersonRef(String personRef) {
        identity.setPersonRef(personRef);
    }
    public void setUid(String uid) {
        identity.setUid(uid);
    }
    public void setUsername(String username) {
        identity.setUsername(username);
    }
    public void setFirstName(String firstName) {
        identity.setFirstName(firstName);
    }
    public void setLastName(String lastName) {
        identity.setLastName(lastName);
    }
    public void setEmail(String email) {
        identity.setEmail(email);
    }
    public void setCellPhone(String cellPhone) {
        identity.setCellPhone(cellPhone);
    }
   /* public void setPassword(String password) {
        identity.setPassword(password);
    }
    */

    public void setRoles(List<UserPropertyAndRole> roles) {
        this.roles = roles;
    }

    public String toJson() {
        String userJson = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            userJson =  mapper.writeValueAsString(this);
        } catch (IOException e) {
            log.info("Could not create json from this object {}", toString(), e);
        }
        return userJson;
    }

    @Override
    public String toString() {
        return "UserAggregate{" +
                "identity=" + identity.toString() +
                ", roles=" + roles.toString() +
                '}';
    }
}
