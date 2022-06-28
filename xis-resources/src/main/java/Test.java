import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

public class Test {

    static void test(Object lock) {
        System.out.println("run: " + lock);
        synchronized (lock) {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("ready: " + lock);

    }

    public static void main(String[] args) throws Exception {
        Object lock1 = "1";
        Object lock2 = "2";
        new Thread(() -> test(lock1)).start();
        new Thread(() -> test(lock1)).start();
        new Thread(() -> test(lock2)).start();
        // new Resource(args[0]);
    }

    static class Resource {
        //private final long lastModified;
        private final String resourcePath;
        private final URI uri;
        //private  File file;
        //private boolean fileExists;
        private final String content;
        private final String schema;

        Resource(String resourcePath) throws Exception {
            this.resourcePath = resourcePath;
            uri = getClass().getResource(resourcePath).toURI();
            schema = uri.getScheme();
            //file = new File(uri);
            //fileExists = file.exists();
            //lastModified = file.lastModified();
            content = getResourceAsString(resourcePath);
            System.out.println(this);
        }

        @Override
        public String toString() {
            return "ResourceFile{" +
                    ", resourcePath='" + resourcePath + '\'' +
                    ", uri=" + uri +
                    ", content='" + content + '\'' +
                    ", schema='" + schema + '\'' +
                    '}';
        }

        public String getResourceAsString(String resourcePath) {
            return getContent(getResourceAsStream(resourcePath), "UTF-8");
        }

        public InputStream getResourceAsStream(String resourcePath) {
            InputStream in = ClassLoader.getSystemResourceAsStream(resourcePath);
            if (in == null) {
                throw new RuntimeException(resourcePath);
            }
            return in;
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
}
