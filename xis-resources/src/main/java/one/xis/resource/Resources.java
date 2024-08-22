package one.xis.resource;

import lombok.NonNull;
import one.xis.context.XISComponent;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanners;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

@XISComponent
public class Resources {
    public synchronized Resource getByPath(String path) {
        String resourcePath = removeTrailingSlash(path);
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

    public Collection<Resource> getClassPathResources(String prefix, @NonNull String suffix) {
        return new Reflections(prefix, Scanners.Resources).getResources("*"+suffix).stream()
                .map(StaticResource::new).collect(Collectors.toSet());
    }

    public boolean exists(String path) {
        return ClassLoader.getSystemClassLoader().getResource(path) != null;
    }

    private String removeTrailingSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}
