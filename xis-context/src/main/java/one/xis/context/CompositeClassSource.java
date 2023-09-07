package one.xis.context;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CompositeClassSource implements ClassSource {

    private final Collection<ClassSource> classSources = new HashSet<>();

    public CompositeClassSource(ClassSource... classSources) {
        Arrays.stream(classSources).forEach(this::addReflection);
    }

    public CompositeClassSource addReflection(ClassSource classSource) {
        this.classSources.add(classSource);
        return this;
    }

    @Override
    public Set<Field> getDependencyFields() {
        return classSources.stream().map(ClassSource::getDependencyFields)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getComponentTypes() {
        return classSources.stream().map(ClassSource::getComponentTypes)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }
}
