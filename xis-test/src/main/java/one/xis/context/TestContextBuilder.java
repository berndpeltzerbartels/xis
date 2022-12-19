package one.xis.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestContextBuilder {

    private final Set<Class<?>> singletonClasses = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public TestContextBuilder withSingletonClass(Class<?> clazz) {
        singletonClasses.add(clazz);
        return this;
    }

    public TestContextBuilder withSingletonClasses(Class<?>... classes) {
        this.singletonClasses.addAll(Arrays.asList(classes));
        return this;
    }

    public TestContextBuilder withSingleton(Object singleton) {
        if (singleton instanceof Class) {
            return withSingletonClass((Class<?>) singleton);
        }
        singletons.add(singleton);
        return this;
    }

    public TestContextBuilder withSingletons(Object... mocks) {
        Arrays.stream(mocks).forEach(this::withSingleton);
        return this;
    }

    public AppContext build() {
        AppContextInitializer initializer = new AppContextInitializer(singletons, singletonClasses);
        initializer.initializeContext();
        return new AppContextImpl(singletons, singletonClasses);
    }

}
