package one.xis.utils.io;

import lombok.experimental.UtilityClass;

import java.io.*;

@UtilityClass
public class IOUtils {

    public InputStream getResourceAsStream(String resourcePath) {
        return ClassLoader.getSystemResourceAsStream(resourcePath);
    }

    public InputStream getResourceForClass(Class<?> aClass, String resourcName) {
        String path = aClass.getPackageName().replace('.', '/') + '/' + resourcName;
        return aClass.getClassLoader().getResourceAsStream(path);
    }

    public String getContent(File file, String encoding) throws IOException {
        return getContent(new FileInputStream(file), encoding);
    }

    public String getContent(InputStream inputStream, String charset) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
