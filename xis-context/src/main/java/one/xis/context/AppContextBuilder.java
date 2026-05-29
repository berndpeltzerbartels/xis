package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * Builder for manually creating a XIS {@link AppContext}.
 *
 * <p>This is mainly useful for tests, embedded applications, and integrations
 * that do not start through XIS Boot. The convenience method {@link #withXIS()}
 * enables the standard XIS annotations and framework package scan.</p>
 */
@SuppressWarnings({"unchecked", "unused"})
public interface AppContextBuilder {

    /**
     * Creates a new builder instance.
     */
    static AppContextBuilder createInstance() {
        return new AppContextBuilderImpl();
    }

    /**
     * Registers an annotation whose methods create singleton beans.
     */
    AppContextBuilder withBeanMethodAnnotation(Class<? extends Annotation> beanMethodAnnotation);

    /**
     * Builds and initializes the context.
     */
    AppContext build();

    /**
     * Adds a singleton class that should be constructed by the context.
     */
    AppContextBuilder withSingletonClass(Class<?> clazz);

    default AppContextBuilder withSingletonClasses(Class<?>... classes) {
        withSingletonClasses(Arrays.asList(classes));
        return this;
    }

    default AppContextBuilder withSingletonClasses(Collection<Class<?>> classes) {
        classes.forEach(this::withSingletonClass);
        return this;
    }

    /**
     * Adds an already constructed singleton instance.
     */
    AppContextBuilder withSingleton(Object bean);

    /**
     * Adds a package to scan for components and proxy interfaces.
     */
    AppContextBuilder withPackage(String pack);

    /**
     * Enables the standard XIS context annotations and scans the framework package.
     */
    default AppContextBuilder withXIS() {
        return withBeanInitAnnotation(Init.class)
                .withComponentAnnotation(Component.class)
                .withDependencyFieldAnnotation(Inject.class)
                .withPackage("one.xis");
    }

    default AppContextBuilder withPackages(String... packages) {
        Arrays.stream(packages).forEach(this::withPackage);
        return this;
    }

    /**
     * Registers an annotation that marks component classes.
     */
    AppContextBuilder withComponentAnnotation(Class<? extends Annotation> componentAnnotation);

    default AppContextBuilder withComponentAnnotations(Class<? extends Annotation>... componentAnnotations) {
        Arrays.stream(componentAnnotations).forEach(this::withComponentAnnotation);
        return this;
    }

    default AppContextBuilder withComponentAnnotations(Collection<Class<? extends Annotation>> componentAnnotations) {
        componentAnnotations.forEach(this::withComponentAnnotations);
        return this;
    }

    /**
     * Registers an annotation used for dependency-injected fields.
     */
    AppContextBuilder withDependencyFieldAnnotation(Class<? extends Annotation> dependencyFieldAnnotation);

    default AppContextBuilder withDependencyFieldAnnotations(Collection<Class<? extends Annotation>> dependencyFieldAnnotations) {
        dependencyFieldAnnotations.forEach(this::withDependencyFieldAnnotation);
        return this;
    }

    default AppContextBuilder withDependencyFieldAnnotations(Class<? extends Annotation>... dependencyFieldAnnotations) {
        Arrays.stream(dependencyFieldAnnotations).forEach(this::withDependencyFieldAnnotation);
        return this;
    }


    /**
     * Registers an annotation used for post-construction initialization methods.
     */
    AppContextBuilder withBeanInitAnnotation(Class<? extends Annotation> beanInitAnnotation);

    default AppContextBuilder withBeanInitAnnotations(Collection<Class<? extends Annotation>> beanInitAnnotations) {
        beanInitAnnotations.forEach(this::withBeanInitAnnotation);
        return this;
    }

    default AppContextBuilder withBeanInitAnnotations(Class<? extends Annotation>... beanInitAnnotations) {
        Arrays.stream(beanInitAnnotations).forEach(this::withBeanInitAnnotation);
        return this;
    }

    /**
     * Adds a singleton class by fully qualified class name.
     */
    AppContextBuilder withSingletonClass(String s);

    default AppContextBuilder withSingletons(Object... objects) {
        Arrays.stream(objects).forEach(this::withSingleton);
        return this;
    }

    default AppContextBuilder withSingletons(Collection<Object> objects) {
        objects.forEach(this::withSingleton);
        return this;
    }

    /**
     * Adds the package of this class as a package scan root.
     */
    AppContextBuilder withBasePackageClass(Class<?> basePackageCLass);

}
