package net.whydah.admin.user.uib;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.List;

/**
 * Created by baardl on 22.06.15.
 */
public class RoleRepresentationMapper {

    public static List<RoleRepresentation> fromJson(String roleJson) {
        List<RoleRepresentation> roles = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            roles= mapper.readValue(roleJson, new TypeReference<List<RoleRepresentation>>() {
            });

        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("Error mapping json for " + roleJson, e);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Error parsing json for " + roleJson, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading json for " + roleJson, e);
        }
        return roles;
    }
}
