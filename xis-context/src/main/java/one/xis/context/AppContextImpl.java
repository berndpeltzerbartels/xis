package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.CollectorUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AppContextImpl implements AppContext {

    @Getter
    private Collection<Object> singletons;

    void setSingletons(Collection<Object> singletons) {
        this.singletons = Collections.unmodifiableCollection(singletons);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSingleton(Class<T> type) {
        return singletons.stream().filter(type::isInstance).map(type::cast).collect(CollectorUtils.toOnlyElement(list -> createException(list, type)));
    }

    public <T> Collection<T> getSingletonsOfType(Class<T> type) {
        return singletons.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toSet());
    }


    private RuntimeException createException(List<Object> candidates, Class<?> type) {
        return new IllegalStateException(createExceptionText(candidates, type));
    }

    private String createExceptionText(List<Object> candidates, Class<?> type) {
        return switch (candidates.size()) {
            case 0 -> "no component found: " + type;
            case 1 -> throw new IllegalStateException("should never happen");
            default -> "too many component of type " + type;
        };
    }
}
