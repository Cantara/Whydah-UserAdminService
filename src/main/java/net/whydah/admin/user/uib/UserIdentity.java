package net.whydah.admin.user.uib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.Serializable;

/**
 * A class representing the identity of a User - backed by LDAP scheme.
 * See getLdapAttributes in LDAPHelper for mapping to LDAP attributes.
 *
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserIdentity extends UserIdentityRepresentation implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(UserIdentity.class);
    private static final long serialVersionUID = 1;

    private String uid;


    public UserIdentity() {
    }

    public UserIdentity(String uid, String username, String firstName, String lastName, String personRef,
                        String email, String cellPhone, String password) {
        this.uid = uid;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personRef = personRef;
        this.email = email;
        this.cellPhone = cellPhone; //TODO Validate valid cellPhone
        this.password = password;
    }

    public boolean validate() {
        if (uid == null || uid.length() < 2) {
            log.error("UID {} not valid", uid);
            return false;
        }
        if (username == null || username.length() < 3) {
            log.error("username {} not valid", username);
            return false;
        }
        if (firstName == null || firstName.length() < 2) {
            log.error("firstName {} not valid", firstName);
            return false;
        }
        if (lastName == null || lastName.length() < 2) {
            log.error("lastName {} not valid", lastName);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserIdentity{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", personRef='" + personRef + '\'' +
                ", email='" + email + '\'' +
                ", cellPhone='" + cellPhone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserIdentity that = (UserIdentity) o;

        if (uid != null ? !uid.equals(that.uid) : that.uid != null) {
            return false;
        }
        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        if (cellPhone != null ? !cellPhone.equals(that.cellPhone) : that.cellPhone != null) {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) {
            return false;
        }
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (cellPhone != null ? cellPhone.hashCode() : 0);
        return result;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static UserIdentity fromJson(String userJson) {
        UserIdentity userIdentity = null;
        try {

            ObjectMapper mapper = new ObjectMapper();
            userIdentity = mapper.readValue(userJson, UserIdentity.class);

            String email = userIdentity.getEmail();
            if (email.contains("+")){
                userIdentity.setEmail(replacePlusWithEmpty(email));
            }

            InternetAddress internetAddress = new InternetAddress();
            internetAddress.setAddress(email);
            try {
                internetAddress.validate();
                userIdentity.setEmail(email);
            } catch (AddressException e) {
                //log.error(String.format("E-mail: %s is of wrong format.", email));
                //return Response.status(Response.Status.BAD_REQUEST).build();
                throw new IllegalArgumentException(String.format("E-mail: %s is of wrong format.", email));
            }


        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userIdentity;
    }

    private static String replacePlusWithEmpty(String email){
        String[] words = email.split("[+]");
        if (words.length == 1) {
            return email;
        }
        email  = "";
        for (String word : words) {
            email += word;
        }
        return email;
    }

    public String toXML() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<whydahuser>\n" +
                "    <identity>\n" +
                "        <username>" + getUsername() + "</username>\n" +
                "        <cellPhone>" + (getCellPhone() != null ? getCellPhone() : "") + "</cellPhone>\n" +
                "        <email>" + getEmail() + "</email>\n" +
                "        <firstname>" + getFirstName() + "</firstname>\n" +
                "        <lastname>" + getLastName() + "</lastname>\n" +
                "        <personRef>" + (getPersonRef() != null ? getPersonRef() : "") + "</personRef>\n" +
                "        <UID>" + getUid() + "</UID>\n" +
                "    </identity>\n" +
                "</whydahuser>";
    }


    public String toJson() {
        String identity =
                "\"uid\":\""+ uid +"\"" +
                ",\"username\":\""+ username +"\"" +
                ",\"firstName\":\"" +firstName +"\"" +
                ",\"lastName\":\""+lastName+"\"" +
                ",\"personRef\":\""+personRef+
                "\",\"email\":\""+email+"\"" +
                ",\"cellPhone\":\""+cellPhone+"\"";
        return "{" + identity + "}";
    }
}
