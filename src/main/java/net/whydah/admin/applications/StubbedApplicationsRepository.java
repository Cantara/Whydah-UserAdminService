package net.whydah.admin.applications;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 13.06.15.
 */
@Repository
public class StubbedApplicationsRepository {
    private static final Logger log = getLogger(StubbedApplicationsRepository.class);

    private static String applicationJson;
    private static String applicationListJson;

    public StubbedApplicationsRepository() {
        log.warn("You are using data from a stubbed repository.");
        readStubbedFiles();
    }

    protected void readStubbedFiles() {
        applicationJson = readFile("stubbedData/application.json");
        applicationListJson = readFile("stubbedData/applications.json");


    }

    protected String readFile(String fileName) {
        String content = "";
        try  {
            content = IOUtils.toString(new ClassPathResource(fileName).getInputStream());
        } catch (IOException x) {
            log.info("Failed to read file: " + fileName);
        }
        return content;
    }

    public String findAll() {
        return applicationListJson;
    }

    public String findByName(String applicationName) {
        return applicationJson;
    }
}
