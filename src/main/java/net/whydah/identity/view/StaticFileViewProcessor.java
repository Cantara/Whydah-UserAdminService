package net.whydah.identity.view;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.io.*;

/**
 * Returns static text files from /webfiles on the classpath.
 */
@Provider
public class StaticFileViewProcessor implements ViewProcessor<String> {
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    private static final String WEBFILES_PATH = "/webfiles";
    private static final String CONTENT_ENCODING_UTF8 = "UTF-8";

    @Override
    public String resolve(String name) {
        log.debug("Resolving path {}", name);
        if (name.endsWith(".css") || name.endsWith("js") || name.endsWith("html")) {
            return name;
        } else {
            return null;
        }
    }

    @Override
    public void writeTo(String filepath, Viewable viewable, OutputStream out) throws IOException {
        String path = WEBFILES_PATH + filepath;
        InputStream classpathStream = getClass().getResourceAsStream(path);
        if (classpathStream == null)
            throw new IllegalArgumentException("Fant ikke " + path + " for " + filepath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(classpathStream, CONTENT_ENCODING_UTF8));
        String line;
        while ((line = reader.readLine()) != null) {
            out.write(line.getBytes(CONTENT_ENCODING_UTF8));
            out.write('\n');
        }
        reader.close();
    }
}
