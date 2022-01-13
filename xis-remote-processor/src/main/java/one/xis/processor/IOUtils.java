package one.xis.processor;

import lombok.experimental.UtilityClass;

import java.io.*;

@UtilityClass
class IOUtils {
    
    String getContent(File file, String encoding) throws IOException {
        return getContent(new FileInputStream(file), encoding);
    }

    private String getContent(InputStream inputStream, String charset) throws IOException {
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
