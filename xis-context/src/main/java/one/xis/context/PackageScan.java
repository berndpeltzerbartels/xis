package one.xis.context;

import one.xis.utils.lang.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "deprecation"})
class PackageScan {

    private final Reflections reflections;
    private final Annotations annotations;

    PackageScan(Collection<String> packagesToScan, Annotations annotations) {
        this.reflections = new Reflections(packagesToScan,
                new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());
        this.annotations = annotations;
    }

    PackageScanResult doScan() {
        annotations.addProxyAnnotations(proxyAnnotations().collect(Collectors.toSet()));
        var customComponentAnnotations = new HashSet<Class<? extends Annotation>>();
        var componentClasses = new HashSet<Class<?>>();
        scanAnnotatedComponentClasses(annotations).forEach(componentClass -> {
            if (componentClass.isAnnotation()) {
                customComponentAnnotations.add((Class<Annotation>) componentClass);
            } else {
                componentClasses.add(componentClass);
            }
        });
        customComponentAnnotations.forEach(annotation -> {
            componentClasses.addAll(scanAnnotatedComponentClasses(annotation).collect(Collectors.toSet()));
            annotations.addComponentClassAnnotation(annotation);
        });
        return new PackageScanResult(annotations, componentClasses, proxyInterfacesByFactory());
    }


    private Stream<Class<?>> scanAnnotatedComponentClasses(Annotations annotations) {
        return annotations.getComponentClassAnnotations().stream()
                .map(reflections::getTypesAnnotatedWith)
                .flatMap(Set::stream);
    }

    private Stream<Class<?>> scanAnnotatedComponentClasses(Class<? extends Annotation> componentAnnotation) {
        return reflections.getTypesAnnotatedWith(componentAnnotation).stream();

    }

    private Collection<Class<Annotation>> scanCustomComponentAnnotations(Annotations annotations) {
        return annotations.getComponentClassAnnotations().stream()
                .flatMap(this::scanCustomComponentAnnotations)
                .map(clazz -> (Class<Annotation>) clazz)
                .collect(Collectors.toSet());
    }

    private Stream<Class<Annotation>> scanCustomComponentAnnotations(Class<? extends Annotation> componentAnnotation) {
        return reflections.getTypesAnnotatedWith(componentAnnotation)
                .stream()
                .filter(Class::isAnnotation)
                .map(clazz -> (Class<Annotation>) clazz);
    }


    private <A extends Annotation, F extends ProxyFactory<?>> Map<Class<F>, Collection<Class<?>>> proxyInterfacesByFactory() {
        return proxyAnnotations()
                .map(annotation -> (Class<A>) annotation)
                .collect(Collectors.toMap(this::getFactoryClass, this::interfaceClassesForProxyAnnotation));
    }

    private <A extends Annotation> Collection<Class<?>> interfaceClassesForProxyAnnotation(Class<A> annotation) {
        return reflections.getTypesAnnotatedWith(annotation);
    }


    private <F extends ProxyFactory<?>> Class<F> getFactoryClass(Class<? extends Annotation> annotation) {
        XISProxy proxyAnnotation = annotation.getAnnotation(XISProxy.class);
        if (!proxyAnnotation.factory().equals(NoProxyFactoryClass.class)) {
            return (Class<F>) proxyAnnotation.factory();
        }
        return (Class<F>) ClassUtils.classForName(proxyAnnotation.factoryName());
    }


    private <A extends Annotation> Stream<Class<A>> proxyAnnotations() {
        return reflections.getTypesAnnotatedWith(XISProxy.class).stream()
                .filter(Class::isAnnotation)
                .map(clazz -> (Class<A>) clazz);
    }
}
