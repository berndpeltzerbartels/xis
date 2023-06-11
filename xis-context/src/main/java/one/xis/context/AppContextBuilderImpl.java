package one.xis.context;

import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class AppContextBuilderImpl implements AppContextBuilder {

    private final Set<Class<?>> singletonClasses = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanInitAnnotation = new HashSet<>();
    private final Set<String> packagesToScan = new HashSet<>();

    @Override
    public AppContextBuilder withSingletonClass(Class<?> clazz) {
        singletonClasses.add(clazz);
        return this;
    }

    @Override
    public AppContextBuilder withSingletonClass(String clazz) {
        singletonClasses.add(ClassUtils.classForName(clazz));
        return this;
    }

    @Override
    public AppContextBuilder withSingletonClasses(Class<?>... classes) {
        singletonClasses.addAll(Arrays.asList(classes));
        return this;
    }

    @Override
    public AppContextBuilder withSingleton(Object bean) {
        if (bean instanceof Class) {
            withSingletonClass((Class<?>) bean);
        } else {
            singletons.add(bean);
        }
        return this;
    }

    @Override
    public AppContextBuilder withPackage(String pack) {
        packagesToScan.add(pack);
        return this;
    }

    @Override
    public AppContextBuilder withComponentAnnotation(Class<? extends Annotation> componentAnnotation) {
        this.componentAnnotations.add(componentAnnotation);
        return this;
    }

    @Override
    public AppContextBuilder withDependencyFieldAnnotation(Class<? extends Annotation> dependencyFieldAnnotation) {
        this.dependencyFieldAnnotations.add(dependencyFieldAnnotation);
        return this;
    }

    @Override
    public AppContextBuilder withBeanInitAnnotation(Class<? extends Annotation> beanInitAnnotation) {
        this.beanInitAnnotation.add(beanInitAnnotation);
        return this;
    }

    @Override
    public AppContext build() {
        componentAnnotations.add(XISComponent.class);
        beanInitAnnotation.add(XISInit.class);
        dependencyFieldAnnotations.add(XISInject.class);
        var appContextWrapper = new AppContextWrapper();
        singletons.add(appContextWrapper);
        validate();
        var noScanReflection = new NoScanReflection(singletons, singletonClasses, componentAnnotations, dependencyFieldAnnotations);
        var defaultReflection = new DefaultReflection(packagesToScan, componentAnnotations, dependencyFieldAnnotations);
        var reflection = new CompositeReflection(noScanReflection, defaultReflection);
        var initializer = new AppContextInitializer(reflection, singletonClasses, singletons, beanInitAnnotation);
        var appContext = initializer.initializeContext();
        appContextWrapper.setAppContext(appContext);
        return appContext;
    }


    private void validate() {
        if (componentAnnotations.isEmpty()) {
            throw new IllegalStateException("method withComponentAnnotation(Class) was never called, so no annotations to identify a component, e.g. @Component, @Service etc. is registrated");
        }
    }

}
