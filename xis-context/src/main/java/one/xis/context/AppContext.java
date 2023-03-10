package one.xis.context;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface AppContext {

    Map<AppContextKey, AppContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    static AppContextBuilder builder() {
        return new AppContextBuilderImpl();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    static AppContext getInstance(String rootPackageName) {
        AppContextKey appContextKey = AppContextKey.getKey(rootPackageName);
        synchronized (appContextKey) {
            return CONTEXT_MAP.computeIfAbsent(appContextKey, key -> createContext(rootPackageName));
        }
    }

    private static AppContext createContext(String rootPackageName) {
        return new AppContextInitializer(rootPackageName).initializeContext();
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




