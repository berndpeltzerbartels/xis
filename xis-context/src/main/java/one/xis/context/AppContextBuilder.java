package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public interface AppContextBuilder {

    AppContext build();

    AppContextBuilder withSingletonClass(Class<?> clazz);

    default AppContextBuilder withSingletonClasses(Class<?>... classes) {
        Arrays.stream(classes).forEach(this::withSingletonClass);
        return this;
    }


    AppContextBuilder withSingelton(Object bean);

    AppContextBuilder withPackage(String pack);

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

    default AppContextBuilder withbeanInitAnnotations(Class<? extends Annotation>... beanInitAnnotations) {
        Arrays.stream(beanInitAnnotations).forEach(this::withBeanInitAnnotation);
        return this;
    }

    AppContextBuilder withSingletonClass(String s);

    default AppContextBuilder withSingeltons(Object... objects) {
        Arrays.stream(objects).forEach(this::withSingelton);
        return this;
    }
}
