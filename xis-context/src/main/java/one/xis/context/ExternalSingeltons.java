package one.xis.context;

import one.xis.utils.lang.FieldUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates singeltons and singleton classes requiring no reflection. Allows
 * adding componentes without packagescan e.g. for testing.
 */
public class ExternalSingeltons implements ClassesSource {

    private final Collection<Class<?>> classes = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentTypeAnnotations;
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations;

    public ExternalSingeltons(Collection<Object> beans,
                              Collection<Class<?>> classes,
                              Set<Class<? extends Annotation>> componentTypeAnnotations,
                              Set<Class<? extends Annotation>> dependencyFieldAnnotations) {
        this.classes.addAll(classes);
        this.classes.addAll(beans.stream().map(Object::getClass).collect(Collectors.toSet()));
        this.componentTypeAnnotations = componentTypeAnnotations;
        this.dependencyFieldAnnotations = dependencyFieldAnnotations;
    }

    public ExternalSingeltons(Collection<Object> beans,
                              Collection<Class<?>> classes) {
        this(beans, classes, Set.of(XISComponent.class), Set.of(XISInject.class));
    }

    @Override
    public Set<Field> getDependencyFields() {
        return classes.stream().map(FieldUtil::getAllFields)
                .flatMap(Collection::stream)
                .filter(f -> isAnnotatedWithAtLeastOne(f, dependencyFieldAnnotations))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getComponentTypes() {
        return classes.stream()
                .filter(c -> isAnnotatedWithAtLeastOne(c, componentTypeAnnotations))
                .collect(Collectors.toSet());
    }


    private Stream<Class<? extends Annotation>> getAnnotationClasses(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotations()).map(Annotation::annotationType);
    }

    private boolean isAnnotatedWithAtLeastOne(AnnotatedElement element, Set<Class<? extends Annotation>> annotations) {
        return getAnnotationClasses(element).anyMatch(annotations::contains);
    }
}
