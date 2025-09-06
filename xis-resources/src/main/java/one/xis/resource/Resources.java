package one.xis.resource;

import lombok.NonNull;
import one.xis.context.XISComponent;

import java.io.File;
import java.io.IOException;
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
        try {
            if ("file".equals(url.getProtocol())) {
                File file = new File(url.toURI());
                if (file.exists()) {
                    return new DevelopmentResource(resourcePath, file);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new StaticResource(resourcePath);
    }


    public Collection<Resource> getClassPathResources(String folder, @NonNull String suffix) {
        try {
            List<Resource> result = new ArrayList<>();
            Enumeration<URL> urls = getClass().getClassLoader().getResources(folder);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File baseDir = new File(url.toURI());
                    scanDirectoryRecursive(baseDir, folder, suffix, result, baseDir);
                } else if ("jar".equals(url.getProtocol())) {
                    scanJarFile(url, folder, suffix, result);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load classpath resources", e);
        }
    }

    private void scanDirectoryRecursive(File currentDir, String currentPath, String suffix, List<Resource> result, File baseDir) {
        File[] files = currentDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryRecursive(file, currentPath, suffix, result, baseDir);
            } else if (file.getName().endsWith(suffix)) {
                String relativePath = baseDir.toURI().relativize(file.toURI()).getPath();
                String resourcePath = (currentPath + "/" + relativePath).replaceAll("/+", "/");
                result.add(new StaticResource(resourcePath));
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