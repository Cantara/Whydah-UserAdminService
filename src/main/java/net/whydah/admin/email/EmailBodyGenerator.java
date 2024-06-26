package net.whydah.admin.email;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


/**
 */
@Component
public class EmailBodyGenerator {
    private final Configuration freemarkerConfig;
    private static final String NEW_USER_EMAIL_TEMPLATE = "WelcomeNewUser.ftl";
    private static final String RESET_PASSWORD_EMAIL_TEMPLATE = "PasswordResetEmail.ftl";

    public EmailBodyGenerator() throws IOException {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_0);
        File customTemplate = new File("./templates/email");
        FileTemplateLoader ftl = null;
        if (customTemplate.exists()) {
            ftl = new FileTemplateLoader(customTemplate);
        }
        ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/templates/email");
        
        TemplateLoader[] loaders = null;
        if (ftl != null) {
          loaders = new TemplateLoader[]{ftl, ctl};
        } else {
          loaders = new TemplateLoader[]{ctl};
        }
        
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        freemarkerConfig.setTemplateLoader(mtl);
        freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setLocalizedLookup(false);
        freemarkerConfig.setTemplateUpdateDelayMilliseconds(6000);
        
    }
   
    public String resetPassword(String url, String username) {
        HashMap<String, String> model = new HashMap<>();
        model.put("username", username);
        model.put("url", url);
        return createBody(RESET_PASSWORD_EMAIL_TEMPLATE, model);
    }

    public String resetPassword(String url, String username, String systemName, String passwordResetEmailTemplateName) {
        HashMap<String, String> model = new HashMap<>();
        model.put("username", username);
        model.put("url", url);
        model.put("name", username);
        model.put("systemName", systemName );
        if (passwordResetEmailTemplateName == null || passwordResetEmailTemplateName.length() < 4) {
            return createBody(RESET_PASSWORD_EMAIL_TEMPLATE, model);
        }
        return createBody(passwordResetEmailTemplateName, model);
    }

    /*
    public String newUser(String name, String systemname, String url) {
        HashMap<String, String> model = new HashMap<>();
        model.put("name", name);
        model.put("url", url);
        return createBody(NEW_USER_EMAIL_TEMPLATE, model);
    }
    */

    public String createBody(String templateName, Map<String, String> model) {
        StringWriter stringWriter = new StringWriter();
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(model, stringWriter);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Populating template failed. templateName=" + templateName, e);
        }
        return stringWriter.toString();
    }
}
