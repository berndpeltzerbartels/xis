package one.xis.boot;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Compatibility layer for Spring Framework annotations in XIS-Boot.
 * <p>
 * <b>Important:</b> XIS-Boot is designed to be significantly simpler and more lightweight than the Spring
 * Framework application context. Consequently, many Spring features and annotations are not supported.
 * However, some commonly used Spring annotations are recognized to ease migration and provide familiar patterns
 * for developers coming from Spring.
 * </p>
 * <p>
 * To see which Spring annotations are currently supported by XIS-Boot, refer to the constant sets defined in this class:
 * <ul>
 *   <li>{@link #SPRING_COMPONENT_ANNOTATIONS} - Component stereotypes (@Component, @Service, @Repository, etc.)</li>
 *   <li>{@link #SPRING_INIT_ANNOTATIONS} - Lifecycle callbacks (@PostConstruct for javax and jakarta)</li>
 *   <li>{@link #SPRING_BEAN_METHOD_ANNOTATION} - Factory methods (@Bean)</li>
 *   <li>{@link #SPRING_DEPENDENCY_INJECTION_ANNOTATION} - Dependency injection (@Autowired, @Inject)</li>
 * </ul>
 * </p>
 * <p>
 * <b>How to find this class:</b> Navigate to the {@code xis-boot} module and open
 * {@code one.xis.boot.SpringCompatibility} to see the complete and current list of supported annotations.
 * </p>
 */
class SpringCompatibility {

    /**
     * Annotation declaring Components in Spring Framework.
     */
    private static final Set<String> SPRING_COMPONENT_ANNOTATIONS = Set.of(
            "org.springframework.stereotype.Component",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Repository",
            "org.springframework.stereotype.Controller",
            "org.springframework.context.annotation.Configuration"
    );

    /**
     * Annotation declaring Spring init methods(@PostConstruct).
     */
    private static final Set<String> SPRING_INIT_ANNOTATIONS = Set.of(
            "javax.annotation.PostConstruct",
            "jakarta.annotation.PostConstruct"
    );

    /**
     * Annotation declaring Spring Bean methods(@Bean).
     */
    private static final String SPRING_BEAN_METHOD_ANNOTATION =
            "org.springframework.context.annotation.Bean";

    /**
     * Annotation declaring Spring dependency injection points(@Autowired).
     */
    private static final Set<String> SPRING_DEPENDENCY_INJECTION_ANNOTATION = Set.of(
            "org.springframework.beans.factory.annotation.Autowired",
            "javax.inject.Inject",
            "jakarta.inject.Inject");


    Stream<Class<? extends Annotation>> getSpringComponentAnnotationsInClassPath() {
        return SPRING_COMPONENT_ANNOTATIONS.stream()
                .map(this::loadAnnotationClass)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    Stream<Class<? extends Annotation>> getSpringInitAnnotationsInClassPath() {
        return SPRING_INIT_ANNOTATIONS.stream()
                .map(this::loadAnnotationClass)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


    Optional<Class<? extends Annotation>> getSpringBeanMethodAnnotationInClassPath() {
        return loadAnnotationClass(SPRING_BEAN_METHOD_ANNOTATION);
    }

    Stream<Class<? extends Annotation>> getSpringDependencyInjectionAnnotationsInClassPath() {
        return SPRING_DEPENDENCY_INJECTION_ANNOTATION.stream()
                .map(this::loadAnnotationClass)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<Class<? extends Annotation>> loadAnnotationClass(String className) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> cls = (Class<? extends Annotation>) Class.forName(className);
            return Optional.of(cls);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }


}
