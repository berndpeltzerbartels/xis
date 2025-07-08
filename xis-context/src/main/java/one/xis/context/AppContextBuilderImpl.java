package one.xis.context;

import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppContextBuilderImpl implements AppContextBuilder {

    private final Set<Class<?>> singletonClasses = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> initAnnotation = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanMethodAnnotations = new HashSet<>();
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
    public AppContextBuilder withBasePackageClass(Class<?> basePackageCLass) {
        return withPackage(basePackageCLass.getPackageName());
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
    public AppContext build() {
        var packageScan = new PackageScan(packagesToScan, annotations());
        var scanResult = packageScan.doScan();
        var contextFactory = new AppContextFactory(new ArrayList<>(singletons), singletonClasses.toArray(Class[]::new), scanResult);
        return contextFactory.createContext();
    }


    private Annotations annotations() {
        var annotations = new Annotations()
                .addComponentClassAnnotation(XISComponent.class)
                .addComponentClassAnnotation(XISDefaultComponent.class)
                .addDependencyFieldAnnotation(XISInject.class)
                .addInitAnnotation(XISInit.class)
                .addBeanMethodAnnotation(XISBean.class);
        annotations.addComponentClassAnnotations(componentAnnotations);
        annotations.addDependencyFieldAnnotations(dependencyFieldAnnotations);
        annotations.addInitAnnotations(initAnnotation);
        annotations.addBeanMethodAnnotations(beanMethodAnnotations);
        return annotations;
    }

    private void validate() {
        if (componentAnnotations.isEmpty()) {
            throw new IllegalStateException("method withComponentAnnotation(Class) was never called, so no annotations to identify a component, e.g. @Component, @Service etc. is registrated");
        }
    }

}
