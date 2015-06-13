package net.whydah.admin.applications;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 13.06.15.
 */
@Repository
public class StubbedApplicationsRepository {
    private static final Logger log = getLogger(StubbedApplicationsRepository.class);

    private static String applicationJson;
    private static String applicationListJson;
    private static Map<String,String> applications = new HashMap<>();

    public StubbedApplicationsRepository() {
        log.warn("You are using data from a stubbed repository.");
        readStubbedFiles();
    }

    protected void readStubbedFiles() {
        applicationJson = readFile("stubbedData/application.json");
        applicationListJson = readFile("stubbedData/applications.json");
        JSONArray array = new JSONArray(applicationListJson);
        for(int i = 0; i < array.length(); i++){
            JSONObject application = array.getJSONObject(i);

            String id = (String) application.get("id");
            String content = application.toString();
            applications.put(id, content);
        }
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

    public String findById(String id) {
        return applications.get(id);
    }
}
