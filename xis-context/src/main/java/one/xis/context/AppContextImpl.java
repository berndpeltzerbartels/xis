package one.xis.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
class AppContextImpl implements AppContext {

    @Getter
    private List<Object> singletons;

    void lockModification() {
        singletons = Collections.unmodifiableList(singletons);
    }

    @Override
    public <T> T getSingleton(Class<T> type) {
        for (var i = 0; i < singletons.size(); i++) {
            var singleton = singletons.get(i);
            if (type.isAssignableFrom(singleton.getClass())) {
                return type.cast(singleton);
            }
        }
        throw new SingletonNotFoundException(type);
    }

    @Override
    public <T> Optional<T> getOptionalSingleton(Class<T> type) {
        for (var i = 0; i < singletons.size(); i++) {
            var singleton = singletons.get(i);
            if (type.isAssignableFrom(singleton.getClass())) {
                return Optional.of(type.cast(singleton));
            }
        }
        return Optional.empty();
    }

}
