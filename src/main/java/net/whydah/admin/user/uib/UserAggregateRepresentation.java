package net.whydah.admin.user.uib;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 12/04/14
 */
public class UserAggregateRepresentation {
    private static final Logger log = LoggerFactory.getLogger(UserAggregateRepresentation.class);
    private String uid;
    private String username;
    private String personName;
    private String firstName;
    private String lastName;
    private String personRef;
    private String email;
    private String cellPhone;

    private List<RoleRepresentation> roles;
    private UserAggregate userAggregate;

    private UserAggregateRepresentation() {
    }

    public static UserAggregateRepresentation fromUserAggregate(UserAggregate userAggregate) {
        UserAggregateRepresentation dto = new UserAggregateRepresentation();

        UserIdentity id = userAggregate.getIdentity();
        dto.setUid(id.getUid());
        dto.setUsername(id.getUsername());
        dto.setFirstName(id.getFirstName());
        dto.setLastName(id.getLastName());
        dto.setPersonRef(id.getPersonRef());
        dto.setEmail(id.getEmail());
        dto.setCellPhone(id.getCellPhone());

        List<UserPropertyAndRole> userPropertyAndRoles = userAggregate.getRoles();
        List<RoleRepresentation> roleRepresentations = new ArrayList<>(userPropertyAndRoles.size());
        for (UserPropertyAndRole role : userPropertyAndRoles) {
            roleRepresentations.add(RoleRepresentation.fromUserPropertyAndRole(role));
        }
        dto.setRoles(roleRepresentations);
        return dto;
    }

    /*
    public static UserAggregateRepresentation fromJson(String userAggregateJson){
        UserAggregateRepresentation userAggregate = null;
        ObjectMapper objectMapper = new ObjectMapper();
        Writer strWriter = new StringWriter();
        try {
            userAggregate =  objectMapper.readValue(userAggregateJson, UserAggregateRepresentation.class);
        } catch (IOException e) {
            log.info("Could not create json string from {}. Error Msg {}", userAggregateJson, e.getMessage());
            throw new MisconfigurationExeption("Could not create json from json input: " + userAggregateJson,e);
        }
        return userAggregate;
    }
    */

    public static UserAggregateRepresentation fromJson(String userAggregateJson){
        if (userAggregateJson == null) {
            log.debug("userAggregateJson was empty, so returning null.");
            return null;
        }

        String uid = findJsonpathValue(userAggregateJson, "$.uid");

        String appid;
        String orgName;
        String rolename;
        String roleValue;
        List<String> rolesAsStrings = findJsonpathList(userAggregateJson, "$.roles[*]");
        List<RoleRepresentation> roles = new ArrayList<>(rolesAsStrings.size());
        for (int n = 0; n < rolesAsStrings.size(); n++){
            try {
                appid = findJsonpathValue(userAggregateJson, "$.roles[" + n + "].applicationId");
                orgName = findJsonpathValue(userAggregateJson, "$.roles[" + n + "].applicationName");
                rolename = findJsonpathValue(userAggregateJson, "$.roles[" + n + "].applicationRoleName");
                roleValue = findJsonpathValue(userAggregateJson, "$.roles[" + n + "].applicationRoleValue");
                RoleRepresentation role = new RoleRepresentation();
                role.setUid(uid);
                role.setApplicationId(appid);
                role.setOrganizationName(orgName);
                role.setApplicationRoleName(rolename);
                role.setApplicationRoleValue(roleValue);
                roles.add(role);
            } catch (PathNotFoundException pnpe) {
                log.error("", pnpe);
                return null;
            }
        }

        UserAggregateRepresentation dto = new UserAggregateRepresentation();
        dto.setUid(uid);
        dto.setUsername(findJsonpathValue(userAggregateJson, "$.username"));
        dto.setFirstName(findJsonpathValue(userAggregateJson, "$.firstName"));
        dto.setLastName(findJsonpathValue(userAggregateJson, "$.lastName"));
        dto.setPersonRef(findJsonpathValue(userAggregateJson, "$.personRef"));
        dto.setEmail(findJsonpathValue(userAggregateJson, "$.email"));
        dto.setCellPhone(findJsonpathValue(userAggregateJson, "$.cellPhone"));

        dto.setRoles(roles);
        return dto;
    }

    public String toJson() {
        StringBuilder strb = new StringBuilder();
        strb.append("{");
        String identity =
                "\"uid\":\""+ uid +"\"" +
                ",\"username\":\""+ username +"\"" +
                ",\"firstName\":\"" +firstName +"\"" +
                ",\"lastName\":\""+lastName+"\"" +
                ",\"personRef\":\""+personRef+
                "\",\"email\":\""+email+"\"" +
                ",\"cellPhone\":\""+cellPhone+"\"";
        strb.append(identity);
        strb.append(",\"roles\": [");
        for (Iterator<RoleRepresentation> iterator = roles.iterator(); iterator.hasNext(); ) {
            RoleRepresentation role = iterator.next();
            strb.append(role.toJson());
            if (iterator.hasNext()) {
                strb.append(", ");
            }
        }
        strb.append("]");
        strb.append("}");
        return strb.toString();
    }

    private static List<String> findJsonpathList(String jsonString,  String expression) throws PathNotFoundException {
        List<String> result=null;
        try {
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);
            result= JsonPath.read(document, expression);

        } catch (Exception e) {
            log.warn("Failed to parse JSON. Expression {}, JSON {}, ", expression, jsonString, e);
        }
        return result;
    }

    private static String findJsonpathValue(String jsonString,  String expression) throws PathNotFoundException {
        String value = "";
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);
        String result= JsonPath.read(document, expression);
        value=result.toString();

        return value;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPersonRef(String personRef) {
        this.personRef = personRef;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }
    public void setRoles(List<RoleRepresentation> roles) {
        this.roles = roles;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getUid() {
        return uid;
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
    public List<RoleRepresentation> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    public UserAggregate getUserAggregate() {
        UserIdentity userIdentity = new UserIdentity(getUid(), getUsername(), getFirstName(), getLastName(), getPersonRef(), getEmail(), getCellPhone(), null);
        List<UserPropertyAndRole> userPropertiesAndRoles = new ArrayList<>();
        List<RoleRepresentation> roles = getRoles();
        for (RoleRepresentation role : roles) {
            UserPropertyAndRole userPropertyAndRole = new UserPropertyAndRole();
            userPropertyAndRole.setId(role.getId());
            userPropertyAndRole.setOrganizationName(role.getOrganizationName());
            userPropertyAndRole.setApplicationId(role.getApplicationId());
            userPropertyAndRole.setApplicationName(role.getApplicationName());

            userPropertyAndRole.setApplicationRoleName(role.getApplicationRoleName());
            userPropertyAndRole.setApplicationRoleValue(role.getApplicationRoleValue());
            userPropertiesAndRoles.add(userPropertyAndRole);
        }
        userAggregate = new UserAggregate(userIdentity, userPropertiesAndRoles);
        return userAggregate;
    }

}
