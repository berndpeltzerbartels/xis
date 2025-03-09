package one.xis.context;

import lombok.Getter;

import java.util.*;


class AppContextImpl implements AppContext {

    @Getter
    private final List<Object> singletons;

    AppContextImpl(Collection<Object> singletons) {
        var singletonList = new ArrayList<>(singletons);
        singletonList.add(this);
        this.singletons = Collections.unmodifiableList(singletonList);
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
