package one.xis.js;

import one.xis.utils.io.IOUtils;

import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class JavascriptResourcePaths {

    private static final String WIDGET_INFO_PATH = "META-INF/xis/widgets";
    private static final String PAGES_INFO_PATH = "META-INF/xis/pages";
    private long metaTimestmap;
    private Collection<String> resourcePaths;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private Collection<String> loadTemplateResources() {
        return null;
    }

    static void test() throws Exception {
        Path directory = Paths.get(ClassLoader.getSystemResource("").toURI());
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            WatchKey watchKey = directory.register(watcher, ENTRY_MODIFY, ENTRY_CREATE);
            while (true) {
                WatchKey foundKey = watcher.take();
                List<WatchEvent<?>> events = foundKey.pollEvents();
                for (WatchEvent e : events) {
                    System.out.println(e.context());
                }
                watchKey.reset();
            }
        }
    }


    synchronized Collection<String> getResourcePaths() {
        if (resourcePaths == null || getLastModified() > metaTimestmap) {
            resourcePaths = loadResourcePaths();
        }
        return resourcePaths;
    }

    private Collection<String> loadResourcePaths() {
        return Arrays.stream(IOUtils.getResourceAsString(WIDGET_INFO_PATH).split("\n"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private static long getLastModified() {
        try {
            return IOUtils.getResourceLastModified(WIDGET_INFO_PATH);
        } catch (Exception e) {
            return -1;
        }

    }

    public static void main(String[] args) throws Exception {
        test();
    }

}
