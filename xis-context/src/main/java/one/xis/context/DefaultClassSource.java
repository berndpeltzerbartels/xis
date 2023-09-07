package one.xis.context;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class DefaultClassSource implements ClassSource {

    private final Reflections reflections;
    private final Set<Class<? extends Annotation>> componentTypeAnnotations;
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations;

    DefaultClassSource(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName());
    }

    DefaultClassSource(String... basePackages) {
        this(Set.of(basePackages), Set.of(XISComponent.class), Set.of(XISInject.class));
    }

    DefaultClassSource(Set<String> basePackages) {
        this(basePackages, Set.of(XISComponent.class), Set.of(XISInject.class));
    }


    DefaultClassSource(Set<String> basePackages, Set<Class<? extends Annotation>> componentTypeAnnotations,
                       Set<Class<? extends Annotation>> dependencyFieldAnnotations) {
        this.componentTypeAnnotations = componentTypeAnnotations;
        this.dependencyFieldAnnotations = dependencyFieldAnnotations;
        reflections = new Reflections(basePackages, new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());
    }

    @Override
    public Set<Field> getDependencyFields() {
        return dependencyFieldAnnotations.stream()
                .map(reflections::getFieldsAnnotatedWith)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getComponentTypes() {
        return componentTypeAnnotations.stream()
                .map(reflections::getTypesAnnotatedWith)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}
