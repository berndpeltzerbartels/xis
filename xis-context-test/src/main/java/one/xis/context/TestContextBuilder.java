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
            throw new IllegalArgumentException("use withSingeltonClass(es) for adding  classes to context");
        }
        singletons.add(singleton);
        return this;
    }

    public TestContextBuilder withSingletons(Object... mocks) {
        Arrays.stream(mocks).forEach(this::withSingleton); // validates each one
        return this;
    }

    public TestContext build() {
        TestContextInitializer initializer = new TestContextInitializer(singletons, singletonClasses);
        initializer.initializeContext();
        return new TestContext(initializer.getSingletons());
    }

}
