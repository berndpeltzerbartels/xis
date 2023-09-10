package one.xis.context;

import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.reflect.AnnotationUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class ComponentClassReflector {
    private final Collection<Class<?>> extraComponentClasses;
    private final Collection<Object> extraComponents;
    private final Collection<Class<? extends Annotation>> beanMethodAnnotations;
    private final Collection<Class<? extends Annotation>> initMethodAnnotations;
    private final Collection<Class<? extends Annotation>> componentAnnotations;
    private final Collection<Class<? extends Annotation>> dependencyFieldAnnotations;
    private final Reflections reflections;

    ComponentClassReflector(Collection<Class<?>> extraComponentClasses,
                            Collection<Object> extraComponents,
                            Collection<Class<? extends Annotation>> initMethodAnnotations,
                            Collection<String> packagesToScan,
                            Collection<Class<? extends Annotation>> beanMethodAnnotations,
                            Collection<Class<? extends Annotation>> componentAnnotations,
                            Collection<Class<? extends Annotation>> dependencyFieldAnnotations) {
        this.extraComponentClasses = extraComponentClasses;
        this.extraComponents = extraComponents;
        this.initMethodAnnotations = initMethodAnnotations;
        this.beanMethodAnnotations = beanMethodAnnotations;
        this.componentAnnotations = componentAnnotations;
        this.dependencyFieldAnnotations = dependencyFieldAnnotations;
        reflections = new Reflections(packagesToScan, new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());
    }

    ReflectionResult findComponentClasses() {
        var scannedClasses = scanComponentClasses();
        var classesToInstantiate = new ArrayList<Class<?>>(); // Do not use Set. There might be more than one candidate
        classesToInstantiate.addAll(extraComponentClasses);
        classesToInstantiate.addAll(scannedClasses);
        var allClasses = findAllComponenetClasses(classesToInstantiate);
        return new ReflectionResult(allClasses, classesToInstantiate);
    }

    Stream<Method> beanMethods(Class<?> componentClass) {
        return MethodUtils.allMethods(componentClass)
                .stream()
                .filter(m -> !Modifier.isPrivate(m.getModifiers()))
                .filter(m -> AnnotationUtils.hasAtLeasOneAnnotation(m, beanMethodAnnotations));

    }

    Stream<Method> annotatedMethods(Class<?> componentClass) {
        return MethodUtils.allMethods(componentClass)
                .stream()
                .filter(m -> !Modifier.isPrivate(m.getModifiers()))
                .filter(m -> AnnotationUtils.hasAtLeasOneAnnotation(m, initMethodAnnotations)
                        || AnnotationUtils.hasAtLeasOneAnnotation(m, beanMethodAnnotations));

    }

    Stream<Field> dependencyFields(Class<?> componentClass) {
        return FieldUtil.getAllFields(componentClass).stream()
                .filter(field -> AnnotationUtils.hasAtLeasOneAnnotation(field, dependencyFieldAnnotations));
    }

    Constructor<?> findValidConstructor(Class<?> type) {
        List<Constructor<?>> constructors = Arrays.stream(type.getDeclaredConstructors()).filter(this::nonPrivate).collect(Collectors.toList());
        return switch (constructors.size()) {
            case 0 -> throw new AppContextException("no accessible constructor for " + type);
            case 1 -> constructors.get(0);
            default -> throw new AppContextException("too many constructors for " + type);
        };
    }

    private boolean nonPrivate(Executable accessibleObject) {
        return !Modifier.isPrivate(accessibleObject.getModifiers());
    }

    private List<Class<?>> scanComponentClasses() {
        return componentAnnotations.stream()
                .map(reflections::getTypesAnnotatedWith)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }


    private List<Class<?>> findAllComponenetClasses(Collection<Class<?>> classesToInstantiate) {
        var allComponentClasses = new ArrayList<Class<?>>();
        allComponentClasses.addAll(classesToInstantiate);
        allComponentClasses.addAll(extraComponents.stream().map(Object::getClass).toList());
        var classesToScanForBeanMethods = new ConcurrentLinkedQueue<>(allComponentClasses);
        while (!classesToScanForBeanMethods.isEmpty()) {
            var c = classesToScanForBeanMethods.poll();
            var returnValues = beanMethods(c).map(Method::getReturnType).toList();
            for (var returnValue : returnValues) {
                if (!allComponentClasses.contains(returnValue)) { // avoid endless recursion
                    classesToScanForBeanMethods.add(returnValue);
                    allComponentClasses.addAll(returnValues);
                }
            }
        }
        return allComponentClasses;
    }


}