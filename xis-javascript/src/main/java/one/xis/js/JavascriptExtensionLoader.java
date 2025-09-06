package one.xis.js;

import one.xis.context.XISComponent;
import one.xis.utils.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Checks for all occurrences of META-INF/xis/js-extension in the classpath. Reads a list vor
 * resource paths (private  method) and loads the js code (another private method).
 * Returns a list of js code snippets created with empty lines in between.
 */
@XISComponent
class JavascriptExtensionLoader {
    List<String> loadExtensions() {
        List<String> extensions = new ArrayList<>();
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/xis/extensions");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                List<String> resourcePaths = readResourcePaths(url);
                for (String resourcePath : resourcePaths) {
                    String jsCode = loadJsResource(resourcePath);
                    extensions.add(jsCode);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JS extensions", e);
        }
        return extensions;
    }

    private String loadJsResource(String resourcePath) {
        return IOUtils.getClassPathResourceContent(resourcePath);
    }

    private List<String> readResourcePaths(URL url) {
        List<String> resourcePaths = new ArrayList<>();
        try (InputStream is = url.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    resourcePaths.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JS extension resource paths from " + url, e);
        }
        return resourcePaths;
    }

}
