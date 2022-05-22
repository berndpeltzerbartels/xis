package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class TestReflection implements AppReflection {

    private final Set<Class<?>> classes;

    TestReflection(Class<?>... classes) {
        this.classes = Arrays.stream(classes).collect(Collectors.toSet());
    }

    @Override
    public <A extends Annotation> Collection<Field> getFieldsAnnotatedWith(Class<A> anno) {
        return classes.stream().map(type -> getFieldsAnnotatedWith(type, anno))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return classes.stream().filter(type -> type.isAnnotationPresent(annotation)).collect(Collectors.toSet());
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
