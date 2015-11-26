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
import java.io.Serializable;
import java.io.StringReader;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
@Deprecated
public class UserIdentityRequest implements Serializable{

    private static final Logger log = LoggerFactory.getLogger(UserIdentityRepresentation.class);
    private String username;
    private String firstName;
    private String lastName;
    private String personRef;
    private String email;
    private String cellPhone;

    public UserIdentityRequest(String username, String firstName, String lastName, String personRef, String email, String cellPhone) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personRef = personRef;
        this.email = email;
        this.cellPhone = cellPhone;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPersonRef() {
        return personRef;
    }

    public String getEmail() {
        return email;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public static UserIdentityRequest fromXML(String userIdentityXML) {
        log.debug("parse userIdentityXML {} ", userIdentityXML);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        UserIdentityRequest identity = null;
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(userIdentityXML)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            String userName = (String) xPath.evaluate("//identity/username", doc, XPathConstants.STRING);
            String firstName = (String) xPath.evaluate("//identity/firstname", doc, XPathConstants.STRING);
            String lastName = (String) xPath.evaluate("//lastname", doc, XPathConstants.STRING);
            String email = (String) xPath.evaluate("//email", doc, XPathConstants.STRING);
            String personRef = (String) xPath.evaluate("//personRef", doc, XPathConstants.STRING);
            String cellPhone = (String) xPath.evaluate("//cellPhone", doc, XPathConstants.STRING);

            identity = new UserIdentityRequest(userName, firstName, lastName, personRef, email, cellPhone);
        } catch (Exception e) {
            log.debug("Error parsing userIdentityXML " + userIdentityXML, e);
        }
        return identity;
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
}
