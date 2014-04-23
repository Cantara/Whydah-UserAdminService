package net.whydah.admin.user.uib;

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
        representation.setOrganizationId(role.getOrganizationId());
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
                "            <orgID>" + getOrganizationId() + "</orgID>\n" +
                "            <orgName>" + getOrganizationName() + "</orgName>\n" +
                "            <roleName>" + getApplicationRoleName() + "</roleName>\n" +
                "            <roleValue>" + getApplicationRoleValue() + "</roleValue>\n" +
                "        </application>";
    }

}
