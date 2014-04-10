package net.whydah.identity.data;

import org.junit.Test;

public class UserTokenTest {
   // private FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor();

    @Test
    public void testCreateUserToken() {
        /*
        UserToken utoken = new UserToken();
        utoken.setFirstName("Ola");
        utoken.setLastName("Nordmann");
        utoken.setTimestamp("123123123");
        utoken.setPersonRef("73637276722376");
        utoken.setTokenid(UUID.randomUUID().toString());
        String xml = freemarkerProcessor.toXml(utoken);
        UserToken copyToken = UserToken.createFromUserTokenXML(xml);
        String copyxml = freemarkerProcessor.toXml(copyToken);
        assertTrue("The generated user token is wrong.", xml.equalsIgnoreCase(copyxml));
        */
    }

    @Test
    public void testActiveUserTokenRepository(){
        /*
        UserToken utoken = new UserToken();
        utoken.setFirstName("Ola");
        utoken.setLastName("Nordmann");
        utoken.setTimestamp(String.valueOf(System.currentTimeMillis() + 1000));
        utoken.setTokenid(UUID.randomUUID().toString());
        utoken.setPersonRef("78125637812638");
        ActiveUserTokenRepository.addUserToken(utoken);
        assertTrue("Verification of valid token failed", ActiveUserTokenRepository.verifyUserToken(utoken));
        utoken.setFirstName("Pelle");
        String token = freemarkerProcessor.toXml(utoken);
        assertTrue("Token not updated", token.indexOf("Pelle") > 0);
        assertFalse("Verification of in-valid token successful", ActiveUserTokenRepository.verifyUserToken(utoken));
        */
    }

    @Test
    public void testTimedOutActiveUserTokenRepository(){
        /*
        UserToken utoken = new UserToken();
        utoken.setTokenid(UUID.randomUUID().toString());
        utoken.setFirstName("Ola");
        utoken.setLastName("Nordmann");
        utoken.setPersonRef("78125637812638");
        utoken.setTimestamp(String.valueOf(System.currentTimeMillis() - 1000));
        utoken.setLifespan("0");
        ActiveUserTokenRepository.addUserToken(utoken);
        assertFalse("Verification of timed-out token successful", ActiveUserTokenRepository.verifyUserToken(utoken));
        */
    }

    @Test
    public void testCreateUserTokenWithRoles() {
        /*
        UserToken utoken = new UserToken();
        utoken.setFirstName("Olav");
        utoken.setLastName("Nordmann");
        utoken.setTokenid(UUID.randomUUID().toString());
        utoken.putApplicationCompanyRoleValue("2349785543", "Styrerommet", "2349785543", "Titutten boretslag", "styreleder", "Diktator");
        utoken.putApplicationCompanyRoleValue("2349785543", "Styrerommet", "2349785543", "Titutten boretslag", "vaktmester", "ansatt");
        utoken.putApplicationCompanyRoleValue("2349785543", "Styrerommet", "0078", "Marmorberget Borettslag", "styremedlem", "");
        utoken.putApplicationCompanyRoleValue("appa", "App A", "1078", "Mormorberget Borettslag", "styremedlem", "Valla");
        String tokenxml = freemarkerProcessor.toXml(utoken);

        UserToken copyToken = UserToken.createFromUserTokenXML(tokenxml);
        String copyxml = freemarkerProcessor.toXml(copyToken);
        assertEquals(tokenxml, copyxml);
        */
    }

    @Test
    public void createFromUserIdentityXML() {
        String identityXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<whydahuser>\n" +
                "    <identity>\n" +
                "        <username>bentelongva@hotmail.com</username>\n" +
                "        <cellPhone>90967400</cellPhone>\n" +
                "        <email>bentelongva@hotmail.com</email>\n" +
                "        <firstname>BENTE</firstname>\n" +
                "        <lastname>LONGVA</lastname>\n" +
                "        <personRef>436276390081408</personRef>\n" +
                "        <UID>bentelongva@hotmail.com</UID>\n" +
                "    </identity>\n" +
                "    <applications>\n" +
                "        <application>\n" +
                "            <appId>Invoice</appId>\n" +
                "            <applicationName>Contempus Invoice</applicationName>\n" +
                "            <orgID>1</orgID>\n" +
                "            <organizationName>Etterstad Brl</organizationName>\n" +
                "            <roleName>VM</roleName>\n" +
                "            <roleValue>2010 - 2011</roleValue>\n" +
                "        </application>\n" +
                "        <application>\n" +
                "            <appId>Invoice</appId>\n" +
                "            <applicationName>Contempus Invoice</applicationName>\n" +
                "            <orgID>1</orgID>\n" +
                "            <organizationName>Etterstad Brl</organizationName>\n" +
                "            <roleName>NT</roleName>\n" +
                "            <roleValue>2010 - 2011</roleValue>\n" +
                "        </application>\n" +
                "        <application>\n" +
                "            <appId>theapp</appId>\n" +
                "            <applicationName>Selveste Appen</applicationName>\n" +
                "            <orgID>1</orgID>\n" +
                "            <organizationName>Etterstad Brl</organizationName>\n" +
                "            <roleName>AS</roleName>\n" +
                "            <roleValue>2010 - 2015</roleValue>\n" +
                "        </application>\n" +
                "    </applications>\n" +
                "</whydahuser>";
        String appXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n " +
                    " <token>\n" +
                    "     <params>\n" +
                    "         <applicationtoken>123123123123</applicationtoken>\n" +
                    "         <applicationid>123</applicationid>\n" +
                    "         <applicationname>faktura</applicationname>\n" +
                    "         <expires>3213213212</expires>\n" +
                    "     </params> \n" +
                    " </token>\n";
       // UserToken userToken = UserToken.createUserIdentity(appXML, identityXML);

        //String xml = freemarkerProcessor.toXml(userToken);

        /*
        assertEquals("436276390081408", userToken.getPersonRef());
        assertEquals("BENTE", userToken.getFirstName());
        assertEquals("LONGVA", userToken.getLastName());
        assertEquals("bentelongva@hotmail.com", userToken.getEmail());
        */
    }
}
