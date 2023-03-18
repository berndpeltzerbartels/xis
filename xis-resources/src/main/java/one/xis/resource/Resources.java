package one.xis.resource;

import one.xis.context.XISComponent;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@XISComponent
public class Resources {
    public synchronized Resource getByPath(String path) {
        String resourcePath = removeTrailingSlah(path);
        URL url = ClassLoader.getSystemClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new NoSuchResourceException(path);
        }
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (uri.getScheme().equals("jar")) {
            return new StaticResource(resourcePath);
        } else {
            return new StaticResource(resourcePath);
        }
    }

    public boolean exists(String path) {
        return ClassLoader.getSystemClassLoader().getResource(path) != null;
    }

    private String removeTrailingSlah(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}
