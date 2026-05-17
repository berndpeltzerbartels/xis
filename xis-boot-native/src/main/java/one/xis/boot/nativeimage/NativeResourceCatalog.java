package one.xis.boot.nativeimage;

import one.xis.utils.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

final class NativeResourceCatalog {

    private static final String CATALOG_PATH = "META-INF/xis/native/resources/catalog.txt";

    private NativeResourceCatalog() {
    }

    static Map<String, NativeResource> load() {
        try {
            var catalog = IOUtils.getResourceAsString(CATALOG_PATH);
            var resources = new LinkedHashMap<String, NativeResource>();
            catalog.lines()
                    .filter(line -> !line.isBlank())
                    .forEach(line -> addResource(resources, line));
            return Map.copyOf(resources);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static void addResource(Map<String, NativeResource> resources, String line) {
        var separator = line.indexOf('\t');
        if (separator < 0) {
            return;
        }
        var path = line.substring(0, separator);
        var encoded = line.substring(separator + 1);
        var content = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        resources.put(path, new NativeResource(path, content));
    }
}
