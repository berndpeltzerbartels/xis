package one.xis.utils.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

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
        InputStream in = ClassLoader.getSystemResourceAsStream(resourcePath);
        if (in == null) {
            throw new NoSuchResourceException(resourcePath);
        }
        return in;
    }

    public static InputStream getResourceForClass(Class<?> aClass, String resourcName) {
        String path = aClass.getPackageName().replace('.', '/') + '/' + resourcName;
        return aClass.getClassLoader().getResourceAsStream(path);
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
            }
            return resultStringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
