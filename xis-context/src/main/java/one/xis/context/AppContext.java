package one.xis.context;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import one.xis.utils.lang.CollectorUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppContext {

    private final Collection<Object> singeltons;
    private static final Map<AppContextKey, AppContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static AppContext getInstance(String packageName) {
        AppContextKey appContextKey = AppContextKey.getKey(packageName);
        synchronized (appContextKey) {
            return CONTEXT_MAP.computeIfAbsent(appContextKey, key -> new AppContext(packageName));
        }
    }

    public <T> T getSingleton(Class<T> type) {
        return singeltons.stream().filter(type::isInstance).map(type::cast).collect(CollectorUtils.onlyElement());
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class AppContextKey {
        private final String packageName;
        private static final Map<String, AppContextKey> keys = new ConcurrentHashMap<>();

        static AppContextKey getKey(String packageName) {
            return keys.computeIfAbsent(packageName, AppContextKey::new);
        }

    }

    private AppContext(String packageName) {
        AppContextInitializer initializer = new AppContextInitializer(packageName);
        initializer.initializeContext();
        singeltons = initializer.getSingletons();
    }
}
