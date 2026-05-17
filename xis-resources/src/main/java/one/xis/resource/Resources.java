package one.xis.resource;

import lombok.NonNull;
import one.xis.context.Component;
import one.xis.context.DefaultComponent;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@DefaultComponent
public class Resources {
    private List<Path> developmentResourceRoots;

    public Resources() {
        developmentResourceRoots = List.of(Path.of("src/main/java"), Path.of("src/main/resources"));
    }

    static Resources withDevelopmentResourceRoots(List<Path> developmentResourceRoots) {
        Resources resources = new Resources();
        resources.developmentResourceRoots = developmentResourceRoots;
        return resources;
    }

    public synchronized Resource getByPath(String path) {
        String resourcePath = removeTrailingSlash(path);
        Optional<File> developmentFile = getDevelopmentFile(resourcePath);
        if (developmentFile.isPresent()) {
            return new DevelopmentResource(resourcePath, developmentFile.get());
        }
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
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
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(folder);
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
                result.add(getByPath(resourcePath));
            }
        }
    }


    private void scanJarFile(URL url, String prefix, String suffix, List<Resource> result) throws IOException {
        var connection = url.openConnection();
        connection.setUseCaches(false);
        if (connection instanceof JarURLConnection jarConnection) {
            try (JarFile jarFile = jarConnection.getJarFile()) {
                scanJarEntries(jarFile, prefix, suffix, result);
            }
            return;
        }

        String path = url.getPath();
        int separator = path.indexOf("!");
        if (separator < 0 || !path.startsWith("file:")) {
            return;
        }
        String jarPath = path.substring("file:".length(), separator);
        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            scanJarEntries(jarFile, prefix, suffix, result);
        }
    }

    private void scanJarEntries(JarFile jarFile, String prefix, String suffix, List<Resource> result) {
        for (JarEntry entry : Collections.list(jarFile.entries())) {
            String name = entry.getName();
            if (name.startsWith(prefix) && name.endsWith(suffix)) {
                result.add(new StaticResource(name));
            }
        }
    }

    public boolean exists(String path) {
        String resourcePath = removeTrailingSlash(path);
        if (getDevelopmentFile(resourcePath).isPresent()) {
            return true;
        }
        try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            return in != null;

        } catch (IOException e) {
            return false;
        }
    }

    private String removeTrailingSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public Optional<File> getDevelopmentFile(String path) {
        String resourcePath = removeTrailingSlash(path);
        for (Path root : developmentResourceRoots) {
            File file = root.resolve(resourcePath).toFile();
            if (file.isFile()) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }
}
