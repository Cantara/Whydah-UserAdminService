package net.whydah.identity.data.helper;

import net.whydah.identity.data.UserToken;
import net.whydah.identity.exception.AuthenticationFailedException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

public class TestUserAuthenticator implements UserAuthenticator {
    private final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    public UserToken logonUser(String appTokenXml, String userCredentialXml) {
        String username;
        try {
            username = getUsername(userCredentialXml);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Authentication failed", e);
        }

        String userxml;
        if (username != null && username.equals("bentelongva@hotmail.com")) {
            userxml = bentelongva();
        } else if (username != null && username.equals("frustaalstrom@gmail.com")) {
            userxml = frustaalstrom();
        } else if (username != null && username.equals("sjohli@gmail.com")) {
            userxml = sjohli();
        } else if (username != null && username.equals("sofia.hasselberg@gmail.com")) {
            userxml = sofiahasselberg();
        } else if (username != null && username.equals("badm")) {
            userxml = badm();
        } else if (username != null && username.equals("ingrid.schubeler@whydah.net")) {
            userxml = sching();
        } else if (username != null && username.equals("ostroy")) {
            userxml = ostroy();
        } else if (username != null && username.equals("tvehel")) {
            userxml = tvehel();
        } else if (username != null && username.equals("adejoh")) {
            userxml = adejoh();
        } else if (username != null && username.equals("asabo2")) {
            userxml = asabo2();
        } else if (username != null && username.equals("thoand")) {
            userxml = thoand();
        } else if (username != null && username.equals("johani")) {
            userxml = johani();
        } else if (username != null && username.equals("olsole")) {
            userxml = olsole();
        } else {
            userxml = resten();
        }
        UserToken token = UserToken.createFromUserTokenXML(userxml);
        ActiveUserTokenRepository.addUserToken(token);
        return token;
    }

    @Override
    public UserToken createAndLogonUser(String appTokenXml, String userCredentialXml, String fbUserXml) {
        throw new IllegalStateException("Not implemented.");
    }

    private String getUsername(String userCredentialXml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        String username;
        DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new StringReader(userCredentialXml)));
        XPath xPath = XPathFactory.newInstance().newXPath();
        username = (String) xPath.evaluate("//username", doc, XPathConstants.STRING);
        return username;
    }


    private static String bentelongva() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>ae76714c-be53-4b10-9c96-63808f9ccc7c</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>BENTE</firstname>\n" +
                "    <lastname>LONGVA</lastname>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"101\">\n" +
                "        <applicationName>Invoice</applicationName>\n" +
                "        <organization ID=\"0001\">\n" +
                "            <organizationName>Etterstad I Borettslag</organizationName>\n" +
                "            <role name=\"SM1\" value=\"2009 - 2011\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String frustaalstrom() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>179d13c9-2b35-45fe-8bc9-0cf463ad6b53</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>HILDE B HAUG</firstname>\n" +
                "    <etternavn>HALVORSEN</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"0001\">\n" +
                "            <organizationName>Etterstad I Borettslag</organizationName>\n" +
                "            <role name=\"styreleder\" value=\"\"/>\n" +
                "            <role name=\"vaktmester\" value=\"Konstituert\"/>\n" +
                "        </organization>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"styremedlem\" value=\"Valgt 2009\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <application ID=\"101\">\n" +
                "        <applicationName>Invoice</applicationName>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"SM1\" value=\"Valgt 2009\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String sjohli() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>683c79be-bfdb-4ee7-8849-2a60011f53c6</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>SYNNE BLOMLI</firstname>\n" +
                "    <etternavn>JOHNSEN</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"101\">\n" +
                "        <applicationName>Invoice</applicationName>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"FM\" value=\"2010 - 2012\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"Styreleder\" value=\"\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }


    private static String sofiahasselberg() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>3999be72-0a40-4112-b70e-b7da3baed309</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>SOFIA</firstname>\n" +
                "    <etternavn>HASSELBERG</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>200000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"nestleder\" value=\"\"/>\n" +
                "        </organization>\n" +
                "        <organization ID=\"0001\">\n" +
                "            <organizationName>Etterstad I Borettslag</organizationName>\n" +
                "            <role name=\"sekretar\" value=\"2009 - 2011\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <application ID=\"101\">\n" +
                "        <applicationName>Invoice</applicationName>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"VF\" value=\"2009 - 2011\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String badm() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>4ba0c259-af44-4606-be72-d5a0390855d1</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Bruker</firstname>\n" +
                "    <etternavn>Admin</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"1\">\n" +
                "        <applicationName>Brukeradmin</applicationName>\n" +
                "        <organization ID=\"9999\">\n" +
                "            <organizationName>Whydah</organizationName>\n" +
                "            <role name=\"Brukeradmin\" value=\"\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String sching() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>0f3d852a-87e3-4cf9-8886-a7722844b563</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid>b20175e4-1a05-492a-9b71-310c57d398e0</personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Ingrid</firstname>\n" +
                "    <etternavn>Schubeler</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"FM\" value=\"2011-2013\"/>\n" +
                "            <role name=\"DL\" value=\"2011-2013\"/>\n" +
                "            <role name=\"Delegert Whydah gereralforsamling\" value=\"2011-2013\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <application ID=\"101\">\n" +
                "        <applicationName>Invoice</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"FM\" value=\"2011-2013\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String ostroy() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>26ba8afd-b2df-45a8-a3da-42999c76ff3c</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Roy Tore</firstname>\n" +
                "    <etternavn>Østensen</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"VF\" value=\"2011-2013\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String tvehel() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>d8cfb19f-ad83-41dd-b24c-30d53d3a56c6</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Helene</firstname>\n" +
                "    <etternavn>Tveter</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"SE\" value=\"2011-2012\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String adejoh() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>a95fb448-52f7-4e0a-ac9c-b4c40c923e3b</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Johnny</firstname>\n" +
                "    <etternavn>Ademaj</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"SM\" value=\"2011-2012\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }


    private static String asabo2() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>cf24a001-d16a-4f05-b632-06eae60de150</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Bojan</firstname>\n" +
                "    <etternavn>Asanovic</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"SM\" value=\"2011-2013\"/>\n" +
                "            <role name=\"DR\" value=\"\"/>\n" +
                "            <role name=\"KV\" value=\"2011-2012\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String thoand() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>afe863f4-f783-4ff7-9f0f-89eecc2d0f05</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Anders</firstname>\n" +
                "    <etternavn>Thorud</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"VM\" value=\"2011-2013\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String johani() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>7669df3c-27d3-430e-872e-c60d279dc9fd</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Anita J.</firstname>\n" +
                "    <etternavn>Vegsgaard</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"SM\" value=\"2011-2012\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String olsole() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>ee635e16-db72-4430-ad85-afafb2110b0c</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Ole</firstname>\n" +
                "    <etternavn>Olsen</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"8999\">\n" +
                "            <organizationName>Test</organizationName>\n" +
                "            <role name=\"VA\" value=\"\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

    private static String resten() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<token xmlns:ns2=\"http://www.w3.org/1999/xhtml\" id=\"" + UUID.randomUUID() + "\">\n" +
                "    <uid>ee635e16-db72-4430-ad85-afafb2110b0c</uid>\n" +
                "    <securitylevel>1</securitylevel>\n" +
                "    <personid></personid>\n" +
                "    <medlemsnummer></medlemsnummer>\n" +
                "    <firstname>Stian</firstname>\n" +
                "    <etternavn>Andersen</etternavn>\n" +
                "    <timestamp>" + System.currentTimeMillis() + "</timestamp>\n" +
                "    <lifespan>3600000</lifespan>\n" +
                "    <issuer>http://10.10.3.88:9998/iam/9056ac3f744957ae6a86daffb5aa98d3/usertoken</issuer>\n" +
                "    <application ID=\"201\">\n" +
                "        <applicationName>Styrerommet</applicationName>\n" +
                "        <organization ID=\"0078\">\n" +
                "            <organizationName>Marmorberget Borettslag</organizationName>\n" +
                "            <role name=\"komitemedlem\" value=\"\"/>\n" +
                "        </organization>\n" +
                "    </application>\n" +
                "    <ns2:link type=\"application/xml\" href=\"/19506df7-4e9d-47df-8f9e-2e3aee551873\" rel=\"self\"/>\n" +
                "    <hash type=\"MD5\">45409705a03d44acd597ca583ed98669</hash>\n" +
                "</token>";
    }

}
