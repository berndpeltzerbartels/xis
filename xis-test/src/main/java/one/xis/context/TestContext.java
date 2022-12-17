package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.CollectorUtils;

import java.util.Collection;
import java.util.Set;

@RequiredArgsConstructor
public class TestContext implements AppContext {

    public static TestContextBuilder builder() {
        return new TestContextBuilder();
    }

    @Getter
    private final Collection<Object> singletons;

    public TestContext(String basePackage, Set<Class<?>> classes, Set<Object> components) {
        TestContextInitializer initializer = new TestContextInitializer(basePackage, components, classes);
        initializer.initializeContext();
        singletons = initializer.getSingletons();
    }

    @Override
    public <T> T getSingleton(Class<T> type) {
        return singletons.stream().filter(type::isInstance).map(type::cast).collect(CollectorUtils.toOnlyElement());
    }
}
