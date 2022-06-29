package one.xis.context;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TestReflection implements AppReflection {

    private final Set<Class<?>> classes;
    private final Set<Class<? extends Annotation>> annotationClasses;

    @SuppressWarnings("unchecked")
    TestReflection(Set<Class<?>> classes) {
        this.classes = classes;
        this.annotationClasses = new Reflections("one.xis", new TypeAnnotationsScanner(), new SubTypesScanner()).getTypesAnnotatedWith(Target.class).stream()
                .filter(Class::isAnnotation)
                .map(c -> (Class<? extends Annotation>) c).collect(Collectors.toSet());
    }

    TestReflection(Class<?>... classes) {
        this(Arrays.stream(classes).collect(Collectors.toSet()));
    }

    @Override
    public <A extends Annotation> Collection<Field> getFieldsAnnotatedWith(Class<A> anno) {
        return classes.stream().map(type -> getFieldsAnnotatedWith(type, anno))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<Class<? extends Annotation>> annotations = getPrimaryAndSecondaryAnnotations(annotation);
        return classes.stream().filter(c -> isAnnotatedWithAtLeastOne(c, annotations)).collect(Collectors.toSet());
    }

    private Set<Class<? extends Annotation>> getPrimaryAndSecondaryAnnotations(Class<? extends Annotation> primaryAnnotation) {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(getAnnotatedAnnotations(primaryAnnotation));
        annotations.add(primaryAnnotation);
        return Collections.unmodifiableSet(annotations);
    }

    private Stream<Class<? extends Annotation>> getTypeAnnotationClasses(Class<?> type) {
        return Arrays.stream(type.getAnnotations()).map(Annotation::annotationType);
    }

    private boolean isAnnotatedWithAtLeastOne(Class<?> clazz, Set<Class<? extends Annotation>> annotations) {
        return getTypeAnnotationClasses(clazz).anyMatch(annotations::contains);
    }


    private Set<Class<? extends Annotation>> getAnnotatedAnnotations(Class<? extends Annotation> primaryAnnotation) {
        return annotationClasses.stream()//
                .filter(type -> type.isAnnotationPresent(primaryAnnotation))//
                .filter(Class::isAnnotation)
                .map(c -> (Class<? extends Annotation>) c)
                .collect(Collectors.toSet());
    }

    private <A extends Annotation> Collection<Field> getFieldsAnnotatedWith(Class<?> owner, Class<A> anno) {
        Collection<Field> fields = new HashSet<>();
        Class<?> c = owner;
        while (c != null && !c.equals(Object.class)) {
            fields.addAll(Arrays.stream(c.getDeclaredFields()).filter(field -> field.isAnnotationPresent(anno)).collect(Collectors.toSet()));
            c = c.getSuperclass();
        }
        return fields;
    }
}
