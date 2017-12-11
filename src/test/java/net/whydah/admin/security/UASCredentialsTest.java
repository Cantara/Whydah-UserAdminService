package net.whydah.admin.security;

import net.whydah.sso.application.types.ApplicationCredential;
import org.testng.annotations.Test;

public class UASCredentialsTest {


    @Test
    public void testXMLEncoding() {
        ApplicationCredential applicationCredential = new ApplicationCredential("id", "name", "secretsecret", "https://whydahdev.cantara.no", "0");
        UASCredentials uasCredentials = new UASCredentials("2212", "testapp", "myapplicationsecret");
        String myXMLEncodedString = uasCredentials.getApplicationCredentialsXmlEncoded();
        System.out.println(myXMLEncodedString);
    }
}