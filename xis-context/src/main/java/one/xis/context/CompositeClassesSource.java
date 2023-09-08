package one.xis.context;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CompositeClassesSource implements ClassesSource {

    private final Collection<ClassesSource> classesSources = new HashSet<>();

    public CompositeClassesSource(ClassesSource... classesSources) {
        Arrays.stream(classesSources).forEach(this::addReflection);
    }

    public CompositeClassesSource addReflection(ClassesSource classesSource) {
        this.classesSources.add(classesSource);
        return this;
    }

    @Override
    public Set<Field> getDependencyFields() {
        return classesSources.stream().map(ClassesSource::getDependencyFields)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getComponentTypes() {
        return classesSources.stream().map(ClassesSource::getComponentTypes)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }
}
