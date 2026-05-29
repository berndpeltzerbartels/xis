package one.xis.context;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Runtime component registry for a XIS application.
 *
 * <p>Most applications let XIS Boot create the context. Tests and embedded tools
 * can use {@link #builder()} or {@link #getInstance(Class)} directly to scan
 * packages, register singletons, and retrieve components.</p>
 */
public interface AppContext {

    Map<AppContextKey, AppContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    /**
     * Creates a configurable context builder.
     */
    static AppContextBuilder builder() {
        return new AppContextBuilderImpl();
    }

    /**
     * Returns the cached context for the package of the given root class,
     * creating it on first access.
     */
    static AppContext getInstance(Class<?> rootClass) {
        return getInstance(rootClass.getPackageName());
    }

    /**
     * Returns the cached context for a root package, creating it on first access.
     */
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

    /**
     * Returns the singleton of the requested type or fails if none is available.
     */
    <T> T getSingleton(Class<T> type);

    /**
     * Returns the singleton of the requested type when one is available.
     */
    <T> Optional<T> getOptionalSingleton(Class<T> type);

    /**
     * Returns all singleton instances known to this context.
     */
    Collection<Object> getSingletons();

    /**
     * Returns all singleton instances assignable to the requested type.
     */
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



