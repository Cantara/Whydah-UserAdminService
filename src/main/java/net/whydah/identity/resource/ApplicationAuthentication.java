package net.whydah.identity.resource;

import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;
import net.whydah.identity.config.AppConfig;
import net.whydah.identity.data.ApplicationCredential;
import net.whydah.identity.data.ApplicationToken;
import net.whydah.identity.data.UserCredential;
import net.whydah.identity.data.helper.AuthenticatedApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Path("/")
public class ApplicationAuthentication {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationAuthentication.class);

    @Inject
    private AppConfig appConfig;

//    @Context
//    private  UriInfo uriInfo;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response info() {
        if("enabled".equals(appConfig.getProperty("testpage"))) {
            logger.debug("Showing test page");
            HashMap<String, Object> model = new HashMap<String, Object>();
            UserCredential testUserCredential = new UserCredential("bentelongva@hotmail.com", "061073");
            model.put("applicationcredential", new ApplicationCredential().toXML());
            model.put("testUserCredential", testUserCredential.toXML());
            return Response.ok().entity(new Viewable("/testpage.html.ftl", model)).build();
        } else {
            logger.debug("Showing prod page");
            return Response.ok().entity(new Viewable("/html/prodwelcome.html")).build();
        }
    }


    @Path("/applicationtokentemplate")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getApplicationTokenTemplate() {
        ApplicationToken template = new ApplicationToken();
        return Response.ok().entity(template.toXML()).build();
    }


    @Path("/applicationcredentialtemplate")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getApplicationCredentialsTemplate() {
        ApplicationCredential template = new ApplicationCredential();
        return Response.ok().entity(template.toXML()).build();
    }

    @Path("/usercredentialtemplate")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserCredentialsTemplate() {
        UserCredential template = new UserCredential("", "");
        return Response.ok().entity(template.toXML()).build();
    }

    @Path("/logon")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response logonApplication(@FormParam("applicationcredential") String appCredentialXml) {
        logger.debug("applogon fikk: {}", appCredentialXml);
        ApplicationToken token = new ApplicationToken(appCredentialXml);
        token.setBaseuri(appConfig.getProperty("mybaseuri"));
        AuthenticatedApplicationRepository.addApplicationToken(token);
        logger.debug("applogon returnerer {}", token.toXML());
        return Response.ok().entity(token.toXML()).build();
    }

    @Path("{applicationtokenid}/validate")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response validateApplicationtokenid(@PathParam("applicationtokenid") String applicationtokenid) {
        logger.debug("verify apptokenid {}", applicationtokenid);
        if(AuthenticatedApplicationRepository.verifyApplicationTokenId(applicationtokenid)) {
            logger.debug("Apptokenid valid");
            return Response.ok().build();
        } else {
            logger.debug("Apptokenid not valid");
            return Response.status(Response.Status.CONFLICT).build();
        }
    }
}
