package one.xis.resource;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class ResourceCache<T> {
    private final Function<Resource, T> loadFunction;
    private final Map<String, Resource> resources;
    private final Map<String, T> cache = new ConcurrentHashMap<>();


    public Optional<T> getResourceContent(String id) {
        if (!resources.containsKey(id)) {
            return Optional.empty();
        }
        var resource = resources.get(id);
        if (!cache.containsKey(id) || resource.isObsolete()) {
            return Optional.of(cache.computeIfAbsent(id, key -> loadFunction.apply(resource)));
        }
        return Optional.of(cache.get(id));
    }

}
