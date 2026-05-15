package one.xis.server;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class StorageParameterValues {
    static final String REQUEST_SCOPE_KEY = "$xis:storage-parameter-values";

    private final Map<StorageParameterScope, Map<String, Object>> values = new EnumMap<>(StorageParameterScope.class);

    Optional<Object> value(StorageParameterScope scope, String key) {
        return Optional.ofNullable(values.getOrDefault(scope, Map.of()).get(key));
    }

    void put(StorageParameterScope scope, String key, Object value) {
        values.computeIfAbsent(scope, ignored -> new HashMap<>()).put(key, value);
    }
}
