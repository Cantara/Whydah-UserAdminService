package net.whydah.admin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
public class TestService {

    public static String getEncodedPathInfo(String path, String contextPath)
    {
        if(contextPath != null && !"".equals(contextPath) && path.startsWith(contextPath))
            path = path.substring(contextPath.length());
        return path;
    }

    @GET
    public String echo() {
        return "pong";
    }

}

