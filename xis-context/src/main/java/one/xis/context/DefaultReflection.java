package one.xis.context;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class DefaultReflection implements Reflection {

    private final Reflections reflections;
    private final Set<Class<? extends Annotation>> componentTypeAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();

    DefaultReflection(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName());
    }

    DefaultReflection(String basePackage) {
        this(basePackage, Set.of(XISComponent.class), Set.of(XISInject.class));
    }

    public <A1 extends Annotation, A2 extends Annotation> DefaultReflection(String basePackage, Set<Class<A1>> extraComponentTypeAnnotations,
                                                                            Set<Class<A2>> extraDependencyFieldAnnotations) {
        this.componentTypeAnnotations.addAll(extraComponentTypeAnnotations);
        this.componentTypeAnnotations.add(XISComponent.class);
        this.dependencyFieldAnnotations.addAll(extraDependencyFieldAnnotations);
        this.dependencyFieldAnnotations.add(XISInject.class);
        reflections = new Reflections(basePackage, new SubTypesScanner(),
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
