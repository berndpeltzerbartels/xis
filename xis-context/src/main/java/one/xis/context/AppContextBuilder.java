package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings({"unchecked", "unused"})
public interface AppContextBuilder {

    AppContextBuilder withBeanMethodAnnotation(Class<? extends Annotation> beanMethodAnnotation);

    AppContextBuilder withProxyAnnotation(Class<? extends Annotation> clazz);

    AppContext build();

    static AppContextBuilder createInstance() {
        return new AppContextBuilderImpl();
    }

    AppContextBuilder withSingletonClass(Class<?> clazz);

    default AppContextBuilder withSingletonClasses(Class<?>... classes) {
        withSingletonClasses(Arrays.asList(classes));
        return this;
    }

    default AppContextBuilder withSingletonClasses(Collection<Class<?>> classes) {
        classes.forEach(this::withSingletonClass);
        return this;
    }

    AppContextBuilder withSingleton(Object bean);

    AppContextBuilder withPackage(String pack);

    default AppContextBuilder withXIS() {
        return withBeanInitAnnotation(XISInit.class)
                .withComponentAnnotation(XISComponent.class)
                .withDependencyFieldAnnotation(XISInject.class)
                .withProxyAnnotation(XISProxy.class)
                .withPackage("one.xis");
    }

    default AppContextBuilder withPackages(String... packages) {
        Arrays.stream(packages).forEach(this::withPackage);
        return this;
    }

    AppContextBuilder withComponentAnnotation(Class<? extends Annotation> componentAnnotation);

    default AppContextBuilder withComponentAnnotations(Class<? extends Annotation>... componentAnnotations) {
        Arrays.stream(componentAnnotations).forEach(this::withComponentAnnotation);
        return this;
    }

    AppContextBuilder withDependencyFieldAnnotation(Class<? extends Annotation> dependencyFieldAnnotation);

    default AppContextBuilder withDependencyFieldAnnotations(Class<? extends Annotation>... dependencyFieldAnnotations) {
        Arrays.stream(dependencyFieldAnnotations).forEach(this::withDependencyFieldAnnotation);
        return this;
    }


    AppContextBuilder withBeanInitAnnotation(Class<? extends Annotation> beanInitAnnotation);

    default AppContextBuilder withBeanInitAnnotations(Class<? extends Annotation>... beanInitAnnotations) {
        Arrays.stream(beanInitAnnotations).forEach(this::withBeanInitAnnotation);
        return this;
    }

    AppContextBuilder withSingletonClass(String s);

    default AppContextBuilder withSingletons(Object... objects) {
        Arrays.stream(objects).forEach(this::withSingleton);
        return this;
    }

    default AppContextBuilder withSingletons(Collection<Object> objects) {
        objects.forEach(this::withSingleton);
        return this;
    }
}
