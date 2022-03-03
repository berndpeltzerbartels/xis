package one.xis.resource;


import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

@RequiredArgsConstructor
public class WebResources {

    private long timestamp = -1;
    private String content;
    private WatchService watchService;

    void init() throws URISyntaxException, IOException {
        String s = System.getProperty("java.class.path");
        watchService = FileSystems.getFileSystem(new URI("classpath://public")).newWatchService();
    }

    void stop() throws IOException {
        watchService.close();
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        WebResources webResources = new WebResources();
        webResources.init();
        webResources.stop();

    }


}
