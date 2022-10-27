package one.xis.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import one.xis.utils.lang.CollectorUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppContext {

    @Getter
    private final Collection<Object> singletons;
    private static final Map<AppContextKey, AppContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    public static AppContext getInstance(String rootPackageName) {
        return getInstance(rootPackageName, Collections.emptySet());
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static AppContext getInstance(String rootPackageName, Collection<Object> externalSingletons) {
        AppContextKey appContextKey = AppContextKey.getKey(rootPackageName);
        synchronized (appContextKey) {
            return CONTEXT_MAP.computeIfAbsent(appContextKey, key -> new AppContext(rootPackageName, externalSingletons));
        }
    }

    public static AppContext getExistingInstance(String rootPackageName) {
        return CONTEXT_MAP.get(AppContextKey.getKey(rootPackageName));
    }

    public <T> T getSingleton(Class<T> type) {
        return singletons.stream().filter(type::isInstance).map(type::cast).collect(CollectorUtils.onlyElement());
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

    AppContext(String packageName, Collection<Object> externalSingletons) {
        AppContextInitializer initializer = new AppContextInitializer(packageName, externalSingletons);
        initializer.initializeContext();
        singletons = initializer.getSingletons();
    }

}
