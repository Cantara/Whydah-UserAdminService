package net.whydah.admin.user.uib;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 12/04/14
 */
public class RoleRepresentation extends RoleRepresentationRequest {
    private String id;
    private String uid;

    public static RoleRepresentation fromUserPropertyAndRole(UserPropertyAndRole role) {
        RoleRepresentation representation = new RoleRepresentation();
        representation.setId(role.getId());
        representation.setApplicationId(role.getApplicationId());
        representation.setApplicationName(role.getApplicationName());
        representation.setOrganizationName(role.getOrganizationName());
        representation.setApplicationRoleName(role.getApplicationRoleName());
        representation.setApplicationRoleValue(role.getApplicationRoleValue());
        return representation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String toXML() {
        return "<application>" +
                "            <id>" + id + "</id>\n" +
                "            <uid>" + uid + "</uid>\n" +
                "            <appId>" + getApplicationId() + "</appId>\n" +
                "            <applicationName>" + getApplicationName() + "</applicationName>\n" +
                "            <orgName>" + getOrganizationName() + "</orgName>\n" +
                "            <roleName>" + getApplicationRoleName() + "</roleName>\n" +
                "            <roleValue>" + getApplicationRoleValue() + "</roleValue>\n" +
                "        </application>";
    }

    public static RoleRepresentation fromJson(String roleJson) {
        RoleRepresentation roleRepresentation = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            roleRepresentation = mapper.readValue(roleJson, RoleRepresentation.class);

        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("Error mapping json for " + roleJson, e);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Error parsing json for " + roleJson, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading json for " + roleJson, e);
        }
        return roleRepresentation;
    }

    public String toJson() {
        /* return "{\"roleId\":\""+ getId() +"\"," +
                "\"uid\":\""+ getUid() +"\"," +
                "\"applicationId\":\""+ getApplicationId() +"\"," +
                "\"applicationName\":\"" + getApplicationName() + "\","+
                "\"applicationRoleName\":\""+ getApplicationRoleName() +"\"," +
                "\"applicationRoleValue\":\""+ getApplicationRoleValue() +"\"," +
                "\"organizationName\":\""+ getOrganizationName() +"\"}";
                */
        String json =  "{";
        if (isNotEmpty(getId())) {
            json = json + "\"roleId\":\"" + getId() + "\",";
        }
        if (isNotEmpty(getUid())) {
            json = json + "\"uid\":\"" + getUid() + "\",";
        }

        json = json + "\"applicationId\":\""+ getApplicationId() +"\"," +
                "\"applicationName\":\"" + getApplicationName() + "\","+
                "\"applicationRoleName\":\""+ getApplicationRoleName() +"\"," +
                "\"applicationRoleValue\":\""+ getApplicationRoleValue() +"\"," +
                "\"organizationName\":\""+ getOrganizationName() +"\"}";

        return json;

    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }

}
