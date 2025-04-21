package one.xis.resource;

import lombok.NonNull;
import one.xis.context.XISComponent;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        return new StaticResource(resourcePath);
    }


    public Collection<Resource> getClassPathResources(String prefix, @NonNull String suffix) {
        try {
            List<Resource> result = new ArrayList<>();
            Enumeration<URL> urls = getClass().getClassLoader().getResources(prefix);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url.getProtocol().equals("file")) {
                    File dir = new File(url.toURI());
                    scanDirectoryRecursive(dir, prefix, suffix, result);
                } else if (url.getProtocol().equals("jar")) {
                    scanJarFile(url, prefix, suffix, result);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load classpath resources", e);
        }
    }

    private void scanDirectoryRecursive(File dir, String prefix, String suffix, List<Resource> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryRecursive(file, prefix, suffix, result);
            } else if (file.getName().endsWith(suffix)) {
                String path = prefix + "/" + relativizePath(file, new File(getClass().getClassLoader().getResource(prefix).getFile()));
                result.add(new StaticResource(path));
            }
        }
    }

    private void scanJarFile(URL url, String prefix, String suffix, List<Resource> result) throws IOException {
        String path = url.getPath();
        String jarPath = path.substring(5, path.indexOf("!"));
        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            for (JarEntry entry : Collections.list(jarFile.entries())) {
                String name = entry.getName();
                if (name.startsWith(prefix) && name.endsWith(suffix)) {
                    result.add(new StaticResource(name));
                }
            }
        }
    }

    private String relativizePath(File file, File baseDir) {
        return baseDir.toURI().relativize(file.toURI()).getPath();
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
