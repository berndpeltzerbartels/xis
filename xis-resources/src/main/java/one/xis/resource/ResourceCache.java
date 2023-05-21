package one.xis.resource;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class ResourceCache<T> {
    private final Function<Resource, T> updateFunction;
    private final Map<String, Resource> resources;
    private final Map<String, T> cache = new HashMap<>();


    public Optional<T> getResourceContent(String id) {
        if (!resources.containsKey(id)) {
            return Optional.empty();
        }
        var resource = resources.get(id);
        if (!cache.containsKey(id) || resource.isObsolete()) {
            return Optional.of(cache.computeIfAbsent(id, key -> updateFunction.apply(resources.get(key))));
        }
        return Optional.of(cache.get(id));
    }

}
