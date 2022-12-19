package one.xis.context;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface AppContext {

    Map<AppContextKey, AppContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    static AppContext getInstance(String rootPackageName) {
        return getInstance(rootPackageName, Collections.emptySet());
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    static AppContext getInstance(String rootPackageName, Collection<Object> externalSingletons) {
        return getInstance(rootPackageName, externalSingletons, Collections.emptySet());
    }
    

    static AppContext getInstance(String rootPackageName, Collection<Object> externalSingletons, Collection<Class<?>> externalClasses) {
        AppContextKey appContextKey = AppContextKey.getKey(rootPackageName);
        synchronized (appContextKey) {
            return CONTEXT_MAP.computeIfAbsent(appContextKey, key -> new AppContextImpl(rootPackageName, externalSingletons, externalClasses));
        }
    }

    <T> T getSingleton(Class<T> type);

    java.util.Collection<Object> getSingletons();

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




