package net.whydah.identity.data;

/**
 * Created by IntelliJ IDEA.
 * User: totto
 * Date: Nov 4, 2010
 * Time: 8:53:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationCredential {

    private String applicationID="1234";
    private String applicationPassord="thePasswrd";

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicationPassord() {
        return applicationPassord;
    }

    public void setApplicationPassord(String applicationPassord) {
        this.applicationPassord = applicationPassord;
    }

    public String toXML(){
        if (applicationID == null){
            return templateToken;
        } else {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
            "<applicationcredential>\n" +
            "    <params>\n" +
            "        <applicationID>"+ applicationID +"</applicationID>\n" +
            "        <applicationSecret>"+ applicationPassord +"</applicationSecret>\n" +
            "    </params> \n" +
            "</applicationcredential>\n" ;
        }
    }

    private final String templateToken = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
            "<template>\n" +
            "    <applicationcredential>\n" +
            "        <params>\n" +
            "            <applicationID>"+ applicationID +"</applicationID>\n" +
            "            <applicationSecret>"+ applicationPassord +"</applicationSecret>\n" +
            "        </params> \n" +
            "    </applicationcredential>\n" +
            "</template>";

}
