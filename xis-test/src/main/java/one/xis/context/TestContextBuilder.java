package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestContextBuilder implements ContextBuilder<AppContext> {

    private final Set<Class<?>> singletonClasses = new HashSet<>();
    private final Set<Object> mocks = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanInitAnnotation = new HashSet<>();
    private final Set<String> packagesToScan = new HashSet<>();

    @Override
    public TestContextBuilder withSingletonClass(Class<?> clazz) {
        singletonClasses.add(clazz);
        return this;
    }

    @Override
    public TestContextBuilder withSingletonClasses(Class<?>... classes) {
        singletonClasses.addAll(Arrays.asList(classes));
        return this;
    }

    @Override
    public TestContextBuilder withMock(Object singleton) {
        if (singleton instanceof Class) {
            return withSingletonClass((Class<?>) singleton);
        }
        mocks.add(singleton);
        return this;
    }

    @Override
    public TestContextBuilder withMocks(Object... mocks) {
        Arrays.stream(mocks).forEach(this::withMock);
        return this;
    }

    @Override
    public TestContextBuilder withPackage(String pack) {
        packagesToScan.add(pack);
        return this;
    }

    @Override
    public TestContextBuilder withComponentAnnotation(Class<? extends Annotation> componentAnnotation) {
        this.componentAnnotations.add(componentAnnotation);
        return this;
    }

    @Override
    public TestContextBuilder withDependencyFieldAnnotation(Class<? extends Annotation> dependencyFieldAnnotation) {
        this.dependencyFieldAnnotations.add(dependencyFieldAnnotation);
        return this;
    }

    @Override
    public TestContextBuilder withBeanInitAnnotation(Class<? extends Annotation> beanInitAnnotation) {
        this.beanInitAnnotation.add(beanInitAnnotation);
        return this;
    }

    @Override
    public AppContext build() {
        validate();
        var noScanReflection = new NoScanReflection(mocks, singletonClasses, componentAnnotations, dependencyFieldAnnotations);
        var defaultReflection = new DefaultReflection(packagesToScan, componentAnnotations, dependencyFieldAnnotations);
        var reflection = new CompositeReflection(noScanReflection, defaultReflection);
        var initializer = new AppContextInitializer(reflection, singletonClasses, mocks, beanInitAnnotation);
        return initializer.initializeContext();
    }


    private void validate() {
        if (componentAnnotations.isEmpty()) {
            throw new IllegalStateException("method withComponentAnnotation(Class) was never called, so no annotations to identify a component, e.g. @Component, @Service etc. is registrated");
        }
    }

    /*
    private Set<Class<? extends Annotation>> getAllXISAnnotations() {
        return new Reflections("one.xis", new TypeAnnotationsScanner(), new SubTypesScanner()).getTypesAnnotatedWith(Target.class).stream()
                .filter(Class::isAnnotation)
                .map(c -> (Class<? extends Annotation>) c).collect(Collectors.toSet());
    }
    */

}
