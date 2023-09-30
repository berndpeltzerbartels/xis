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
    private final Set<Class<? extends Annotation>> initAnnotation = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanMethodAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> proxyAnnotations = new HashSet<>();
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
        this.initAnnotation.add(beanInitAnnotation);
        return this;
    }

    @Override
    public AppContextBuilder withBeanMethodAnnotation(Class<? extends Annotation> beanMethodAnnotation) {
        this.beanMethodAnnotations.add(beanMethodAnnotation);
        return this;
    }

    @Override
    public AppContextBuilder withProxyAnnotation(Class<? extends Annotation> clazz) {
        if (!clazz.isAnnotationPresent(XISProxy.class)) {
            throw new IllegalStateException("annotation " + clazz.getSimpleName() + " must be annotated for proxy (@XISProxy)");
        }
        proxyAnnotations.add(clazz);
        return this;
    }


    @Override
    public AppContext build() {
        componentAnnotations.add(XISComponent.class);
        initAnnotation.add(XISInit.class);
        dependencyFieldAnnotations.add(XISInject.class);
        beanMethodAnnotations.add(XISBean.class);
        var contextFactory = new AppContextFactory(singletonClasses,
                singletons,
                proxyAnnotations,
                componentAnnotations,
                dependencyFieldAnnotations,
                initAnnotation,
                beanMethodAnnotations,
                packagesToScan);
        return contextFactory.createContext();
    }


    private void validate() {
        if (componentAnnotations.isEmpty()) {
            throw new IllegalStateException("method withComponentAnnotation(Class) was never called, so no annotations to identify a component, e.g. @Component, @Service etc. is registrated");
        }
    }

}
