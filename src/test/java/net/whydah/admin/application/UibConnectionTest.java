package net.whydah.admin.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by baardl on 08.04.14.
 */
public class UibConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(UibConnectionTest.class);
    UibConnection uibConnection = null;

    public UibConnectionTest(UibConnection uibConnection) {
        this.uibConnection = uibConnection;
    }

    public static void main(String[] args) throws Exception {
        UibConnectionTest uibConnectionTest = new UibConnectionTest(new UibConnection());
        uibConnectionTest.testAddApplication();
        uibConnectionTest.testGetApplication();
    }
    public void testAddApplication() throws Exception {

        Application application = new Application("id1", "test2", "defRole", "defOrigid");
        uibConnection.addApplication("1","1",application.toJson());

    }

    public void testGetApplication() throws Exception {
        Application application = uibConnection.getApplication("1","1", "id1");
        log.info("fetched application {}", application);



    }
}
