package one.xis.boot.nativeimage;

import one.xis.context.Component;
import one.xis.resource.Resource;
import one.xis.resource.Resources;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

@Component
public class NativeResources extends Resources {

    private final Map<String, NativeResource> resources = NativeResourceCatalog.load();

    @Override
    public synchronized Resource getByPath(String path) {
        var resourcePath = normalize(path);
        var resource = resources.get(resourcePath);
        if (resource == null) {
            throw new RuntimeException("No such resource: " + path);
        }
        return resource;
    }

    @Override
    public Collection<Resource> getClassPathResources(String folder, String suffix) {
        var normalizedFolder = normalize(folder);
        if (!normalizedFolder.endsWith("/")) {
            normalizedFolder += "/";
        }
        var finalFolder = normalizedFolder;
        return resources.values().stream()
                .filter(resource -> resource.getResourcePath().startsWith(finalFolder))
                .filter(resource -> resource.getResourcePath().endsWith(suffix))
                .sorted(Comparator.comparing(Resource::getResourcePath))
                .map(resource -> (Resource) resource)
                .toList();
    }

    @Override
    public boolean exists(String path) {
        return resources.containsKey(normalize(path));
    }

    private static String normalize(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}
