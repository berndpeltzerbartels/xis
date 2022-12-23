package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public interface ContextBuilder<C extends AppContext> {

    C build();

    ContextBuilder<C> withSingletonClass(Class<?> clazz);

    default ContextBuilder<C> withSingletonClasses(Class<?>... classes) {
        Arrays.stream(classes).forEach(this::withSingletonClass);
        return this;
    }


    ContextBuilder<C> withMock(Object singleton);

    default ContextBuilder<C> withMocks(Object... mocks) {
        Arrays.stream(mocks).forEach(this::withMock);
        return this;
    }

    ContextBuilder<C> withPackage(String pack);

    default ContextBuilder<C> withPackages(String... packages) {
        Arrays.stream(packages).forEach(this::withPackage);
        return this;
    }

    ContextBuilder<C> withComponentAnnotation(Class<? extends Annotation> componentAnnotation);

    default ContextBuilder<C> withComponentAnnotations(Class<? extends Annotation>... componentAnnotations) {
        Arrays.stream(componentAnnotations).forEach(this::withComponentAnnotation);
        return this;
    }

    ContextBuilder<C> withDependencyFieldAnnotation(Class<? extends Annotation> dependencyFieldAnnotation);

    default ContextBuilder<C> withDependencyFieldAnnotations(Class<? extends Annotation>... dependencyFieldAnnotations) {
        Arrays.stream(dependencyFieldAnnotations).forEach(this::withDependencyFieldAnnotation);
        return this;
    }


    ContextBuilder<C> withBeanInitAnnotation(Class<? extends Annotation> beanInitAnnotation);

    default ContextBuilder<C> withbeanInitAnnotations(Class<? extends Annotation>... beanInitAnnotations) {
        Arrays.stream(beanInitAnnotations).forEach(this::withBeanInitAnnotation);
        return this;
    }

}
