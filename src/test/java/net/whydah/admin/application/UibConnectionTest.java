package net.whydah.admin.application;

import net.whydah.admin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;

/**
 * Created by baardl on 08.04.14.
 */
public class UibConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(UibConnectionTest.class);
    private static UibApplicationConnection uibApplicationConnection = null;

    public UibConnectionTest(UibApplicationConnection uibApplicationConnection) {
        this.uibApplicationConnection = uibApplicationConnection;
    }

    public static void main(String[] args) throws Exception {
        AppConfig appConfig = mock(AppConfig.class);
        uibApplicationConnection = new UibApplicationConnection(appConfig);
        UibConnectionTest uibConnectionTest = new UibConnectionTest(uibApplicationConnection);
        uibConnectionTest.testAddApplication();
        uibConnectionTest.testGetApplication();
    }
    public void testAddApplication() throws Exception {

        Application application = new Application("id1", "test2", "defRole", "defOrigid");
        uibApplicationConnection.addApplication("1","1",application.toJson());

    }

    public void testGetApplication() throws Exception {
        Application application = uibApplicationConnection.getApplication("1","1", "id1");
        log.info("fetched application {}", application);



    }
}
