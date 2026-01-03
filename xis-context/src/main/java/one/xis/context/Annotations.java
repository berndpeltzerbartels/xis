package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class encapsulates annotation reflection for a framework witch custom proxy-annotations.
 * It is used to determine if a method is annotated as a bean producer or initializer,
 * if a class is annotated, and if a field is a dependency.
 */
@Getter
@RequiredArgsConstructor
public class Annotations {
    private final Set<Class<? extends Annotation>> initAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanMethodAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentClassAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> proxyAnnotations = new HashSet<>();
    private Class<? extends Annotation> eventListenerAnnotation;

    @Setter
    private Predicate<Annotation> isDefault = annotation -> false;

    public Annotations addInitAnnotation(Class<? extends Annotation> annotation) {
        initAnnotations.add(annotation);
        return this;
    }

    public Annotations addBeanMethodAnnotation(Class<? extends Annotation> annotation) {
        beanMethodAnnotations.add(annotation);
        return this;
    }

    public Annotations addComponentClassAnnotation(Class<? extends Annotation> annotation) {
        componentClassAnnotations.add(annotation);
        return this;
    }

    public Annotations addDependencyFieldAnnotation(Class<? extends Annotation> annotation) {
        dependencyFieldAnnotations.add(annotation);
        return this;
    }

    public Annotations addProxyAnnotation(Class<? extends Annotation> annotation) {
        proxyAnnotations.add(annotation);
        return this;
    }

    public Annotations addInitAnnotations(Collection<Class<? extends Annotation>> annotations) {
        initAnnotations.addAll(annotations);
        return this;
    }

    public Annotations addBeanMethodAnnotations(Collection<Class<? extends Annotation>> annotations) {
        beanMethodAnnotations.addAll(annotations);
        return this;
    }

    public Annotations addComponentClassAnnotations(Collection<Class<? extends Annotation>> annotations) {
        componentClassAnnotations.addAll(annotations);
        return this;
    }

    public Annotations addDependencyFieldAnnotations(Collection<Class<? extends Annotation>> annotations) {
        dependencyFieldAnnotations.addAll(annotations);
        return this;
    }

    public Annotations addProxyAnnotations(Collection<Class<? extends Annotation>> annotations) {
        proxyAnnotations.addAll(annotations);
        return this;
    }

    public Annotations append(Annotations annotations) {
        initAnnotations.addAll(annotations.initAnnotations);
        beanMethodAnnotations.addAll(annotations.beanMethodAnnotations);
        componentClassAnnotations.addAll(annotations.componentClassAnnotations);
        dependencyFieldAnnotations.addAll(annotations.dependencyFieldAnnotations);
        return this;
    }

    /**
     * Ruft die erste gefundene Abhängigkeitsannotation eines Feldes ab.
     *
     * @param field Das zu prüfende Feld.
     * @return Ein Optional, das die Annotation enthält, oder ein leeres Optional, wenn keine Abhängigkeitsannotation gefunden wird.
     */
    public Optional<Annotation> getDependencyAnnotation(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if (dependencyFieldAnnotations.contains(annotation.annotationType())) {
                return Optional.of(annotation);
            }
        }
        return Optional.empty();
    }

    /**
     * Determines if a method is annotated as a bean producer or initializer.
     *
     * @param method
     * @return
     */
    boolean isAnnotatedMethod(Method method) {
        for (var i = 0; i < method.getAnnotations().length; i++) {
            Annotation annotation = method.getAnnotations()[i];
            if (beanMethodAnnotations.contains(annotation.annotationType())
                    || initAnnotations.contains(annotation.annotationType())
                    || proxyAnnotations.contains(annotation.annotationType())
                    || (eventListenerAnnotation != null && eventListenerAnnotation.equals(annotation.annotationType()))) {
                return true;
            }
        }
        return false;
    }

    boolean isInitializerMethod(Method method) {
        for (var i = 0; i < method.getAnnotations().length; i++) {
            Annotation annotation = method.getAnnotations()[i];
            if (initAnnotations.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class is a default component, either directly via @Component
     * or indirectly via a meta-annotation.
     *
     * @param type The class to check.
     * @return true if the class is marked as a default component.
     */
    boolean isDefault(Class<?> type) {
        // Check for direct annotation
        if (type.isAnnotationPresent(XISDefaultComponent.class)) {
            return true;
        }

        // Check for meta-annotations recursively
        for (Annotation annotation : type.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            // Avoid recursion into standard Java annotations
            if (!annotationType.getPackage().getName().startsWith("java.lang.annotation")) {
                if (isDefault(annotationType)) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean isProxyMethod(Method method) {
        for (var i = 0; i < method.getAnnotations().length; i++) {
            Annotation annotation = method.getAnnotations()[i];
            if (proxyAnnotations.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if a class is a component.
     *
     * @param c
     * @return
     */
    boolean isAnnotatedComponent(Class<?> c) {
        for (var i = 0; i < c.getAnnotations().length; i++) {
            Annotation annotation = c.getAnnotations()[i];
            if (componentClassAnnotations.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    boolean isDefaultComponent(Class<?> c) {
        for (var i = 0; i < c.getAnnotations().length; i++) {
            Annotation annotation = c.getAnnotations()[i];
            if (isDefault.test(annotation)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if a field is a dependency.
     *
     * @param field
     * @return
     */
    boolean isDependencyField(Field field) {
        for (var i = 0; i < field.getAnnotations().length; i++) {
            Annotation annotation = field.getAnnotations()[i];
            if (dependencyFieldAnnotations.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isBeanMethod(Method method) {
        for (var i = 0; i < method.getAnnotations().length; i++) {
            Annotation annotation = method.getAnnotations()[i];
            if (beanMethodAnnotations.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    public Annotations setEventListenerAnnotation(Class<? extends Annotation> eventListenerAnnotation) {
        this.eventListenerAnnotation = eventListenerAnnotation;
        return this;
    }

    public boolean isEventListenerMethod(Method method) {
        return eventListenerAnnotation != null && method.isAnnotationPresent(eventListenerAnnotation);
    }
}
