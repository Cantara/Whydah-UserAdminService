package net.whydah.identity.data;

/**
 * Created by IntelliJ IDEA.
 * User: totto
 * Date: Nov 4, 2010
 * Time: 8:53:26 AM
 */
public class UserCredential {

    public UserCredential(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    private final String userName;
    private final String password;

    public String toXML(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
        "<usercredential>\n" +
        "    <params>\n" +
        "        <username>"+userName+"</username>\n" +
        "        <password>"+password+"</password>\n" +
        "    </params> \n" +
        "</usercredential>\n" ;
    }


}
