package one.xis.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestContextBuilder {

    private Set<Class<?>> classes = new HashSet<>();
    private Set<Object> mocks = new HashSet<>();

    public TestContextBuilder withSingleton(Class<?> clazz) {
        classes.add(clazz);
        return this;
    }

    public TestContextBuilder withSingletons(Class<?>... classes) {
        this.classes.addAll(Arrays.asList(classes));
        return this;
    }

    public TestContextBuilder withMockedSingleton(Object mock) {
        mocks.add(mock);
        return this;
    }

    public TestContextBuilder withMockedSingletons(Object... mocks) {
        this.mocks.addAll(Arrays.asList(mocks));
        return this;
    }

    public TestContext build() {
        TestContextInitializer initializer = new TestContextInitializer(mocks, classes);
        initializer.initializeContext();
        return new TestContext(initializer.getSingletons());
    }

}
