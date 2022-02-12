package one.xis.utils.io;

import lombok.experimental.UtilityClass;

import java.io.*;

@UtilityClass
public class IOUtils {

    public long getResourceLastModified(String resourcePath) {
        try {
            return ClassLoader.getSystemClassLoader().getResource(resourcePath).openConnection().getLastModified();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResourceAsString(String resourcePath) {
        return getContent(getResourceAsStream(resourcePath), "UTF-8");
    }

    public InputStream getResourceAsStream(String resourcePath) {
        return ClassLoader.getSystemResourceAsStream(resourcePath);
    }

    public InputStream getResourceForClass(Class<?> aClass, String resourcName) {
        String path = aClass.getPackageName().replace('.', '/') + '/' + resourcName;
        return aClass.getClassLoader().getResourceAsStream(path);
    }

    public String getContent(File file, String encoding) {
        try {
            return getContent(new FileInputStream(file), encoding);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getContent(InputStream inputStream, String charset) {
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
}
