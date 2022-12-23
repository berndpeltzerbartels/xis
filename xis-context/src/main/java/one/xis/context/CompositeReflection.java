package one.xis.context;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CompositeReflection implements Reflection {

    private final Collection<Reflection> reflections = new HashSet<>();

    public CompositeReflection(Reflection... reflections) {
        Arrays.stream(reflections).forEach(this::addReflection);
    }

    public CompositeReflection addReflection(Reflection reflection) {
        this.reflections.add(reflection);
        return this;
    }

    @Override
    public Set<Field> getDependencyFields() {
        return reflections.stream().map(Reflection::getDependencyFields)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getComponentTypes() {
        return reflections.stream().map(Reflection::getComponentTypes)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }
}
