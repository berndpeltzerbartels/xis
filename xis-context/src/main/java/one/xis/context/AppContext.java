package one.xis.context;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface AppContext {

    Map<AppContextKey, AppContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    static AppContextBuilder builder() {
        return new AppContextBuilderImpl();
    }

    static AppContext getInstance(Class<?> rootClass) {
        return getInstance(rootClass.getPackageName());
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    static AppContext getInstance(String rootPackageName) {
        AppContextKey appContextKey = AppContextKey.getKey(rootPackageName);
        synchronized (appContextKey) {
            return CONTEXT_MAP.computeIfAbsent(appContextKey, key -> createContext(rootPackageName));
        }
    }

    private static AppContext createContext(String rootPackageName) {
        return new AppContextBuilderImpl()
                .withPackage(rootPackageName)
                .build();

    }

    <T> T getSingleton(Class<T> type);

    Collection<Object> getSingletons();

    default Collection<Object> getSingletons(Class<?> type) {
        return getSingletons().stream().filter(type::isInstance).collect(Collectors.toSet());
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    class AppContextKey {
        private final String packageName;
        private static final Map<String, AppContextKey> keys = new ConcurrentHashMap<>();

        static AppContextKey getKey(String packageName) {
            return keys.computeIfAbsent(packageName, AppContextKey::new);
        }

    }
}




