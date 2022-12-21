package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestContextBuilder {

    private final Set<Class<?>> singletonClasses = new HashSet<>();
    private final Set<Object> mocks = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanInitAnnotation = new HashSet<>();

    public TestContextBuilder withSingletonClass(Class<?> clazz) {
        singletonClasses.add(clazz);
        return this;
    }

    public TestContextBuilder withSingletonClasses(Class<?>... classes) {
        singletonClasses.addAll(Arrays.asList(classes));
        return this;
    }

    public TestContextBuilder withMock(Object singleton) {
        if (singleton instanceof Class) {
            return withSingletonClass((Class<?>) singleton);
        }
        mocks.add(singleton);
        return this;
    }

    public TestContextBuilder withMocks(Object... mocks) {
        Arrays.stream(mocks).forEach(this::withMock);
        return this;
    }

    public TestContextBuilder withComponentAnnotations(Class<? extends Annotation>... componentAnnotation) {
        this.componentAnnotations.addAll(Arrays.asList(componentAnnotation));
        return this;
    }

    public TestContextBuilder withDependencyFieldAnnotations(Class<? extends Annotation>... dependencyFieldAnnotations) {
        this.dependencyFieldAnnotations.addAll(Arrays.asList(dependencyFieldAnnotations));
        return this;
    }

    public TestContextBuilder withBeanInitAnnotations(Class<? extends Annotation>... beanInitAnnotation) {
        this.beanInitAnnotation.addAll(Arrays.asList(beanInitAnnotation));
        return this;
    }

    public AppContext build() {
        var reflection = new NoScanReflection(mocks, singletonClasses, componentAnnotations, dependencyFieldAnnotations);
        var initializer = new AppContextInitializer(reflection, singletonClasses, mocks, beanInitAnnotation);
        return initializer.initializeContext();
    }

    /*
    private Set<Class<? extends Annotation>> getAllXISAnnotations() {
        return new Reflections("one.xis", new TypeAnnotationsScanner(), new SubTypesScanner()).getTypesAnnotatedWith(Target.class).stream()
                .filter(Class::isAnnotation)
                .map(c -> (Class<? extends Annotation>) c).collect(Collectors.toSet());
    }
    */

}
