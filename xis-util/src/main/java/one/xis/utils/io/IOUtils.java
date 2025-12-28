package one.xis.utils.io;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

    /**
     * Lists all resources in the given path. Works for file system resources and classpath resources.
     *
     * @param path
     * @return
     */
    public static List<String> getResources(String path, String suffix) {
        List<String> resources = new ArrayList<>();
        try {
            var url = ClassLoader.getSystemClassLoader().getResource(path);
            if (url == null) {
                return resources;
            }
            if (url.getProtocol().equals("file")) {
                File dir = new File(url.getFile());
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && (suffix == null || file.getName().endsWith(suffix))) {
                                resources.add(path + "/" + file.getName());
                            }
                        }
                    }
                }
            } else if (url.getProtocol().equals("jar")) {
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath)) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.startsWith(path) && !entry.isDirectory() && (suffix == null || entryName.endsWith(suffix))) {
                            resources.add(entryName);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resources;
    }

    public static long getResourceLastModified(String resourcePath) {
        try {
            return ClassLoader.getSystemClassLoader().getResource(resourcePath).openConnection().getLastModified();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getResourceAsString(String resourcePath) {
        return getContent(getResourceAsStream(resourcePath), "UTF-8");
    }

    public static InputStream getResourceAsStream(String resourcePath) {
        URL in = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (in == null) {
            throw new NoSuchResourceException(resourcePath);
        }
        try {
            return in.openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getResourceForClass(Class<?> aClass, String resourcName) {
        String path = aClass.getPackageName().replace('.', '/') + '/' + resourcName;
        return getResourceAsStream(path);
    }

    public static List<String> getContentLines(InputStream inputStream, String charset) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    public static String getContent(File file, String encoding) {
        try {

            return getContent(new FileInputStream(file), encoding);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getContent(InputStream inputStream, String charset) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
                return resultStringBuilder.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getClassPathResourceContent(String resourcePath) {
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        try (InputStream inputStream = getResourceAsStream(resourcePath)) {
            return getContent(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }

    public static PrintWriter printWriter(File file) {
        return printWriter(file, StandardCharsets.UTF_8.name());
    }

    public static PrintWriter printWriter(File file, String charset) {
        try {
            return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
