package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class AppContextImpl implements AppContext {

    @Getter
    private Collection<Object> singletons;
    private Map<Class<?>, Object> singletonCache = new ConcurrentHashMap<>();

    void setSingletons(Collection<Object> singletons) {
        this.singletons = Collections.unmodifiableCollection(singletons);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSingleton(Class<T> type) {
        return (T) singletonCache.computeIfAbsent(type, t -> findSingleton(type));
    }

    private <T> T findSingleton(Class<T> type) {
        var list = (List<T>) singletons.stream().filter(type::isInstance).map(type::cast).toList();
        if (list.size() == 1) {
            return list.get(0);
        }
        throw createException(list, type);
    }

    private RuntimeException createException(List<?> candidates, Class<?> type) {
        return new IllegalStateException(createExceptionText(candidates, type));
    }

    private String createExceptionText(List<?> candidates, Class<?> type) {
        return switch (candidates.size()) {
            case 0 -> "no component found: " + type;
            case 1 -> throw new IllegalStateException("should never happen");
            default -> "too many component of type " + type;
        };
    }
}
