package net.whydah.admin.user.uib;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by baardl on 22.06.15.
 */
public class RoleRepresentationMapper {

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
}
