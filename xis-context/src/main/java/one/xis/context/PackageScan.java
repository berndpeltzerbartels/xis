package one.xis.context;

import one.xis.utils.lang.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "deprecation"})
class PackageScan {
    private final Reflections reflections;

    PackageScan(Collection<String> packagesToScan) {
        this.reflections = new Reflections(packagesToScan,
                new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());
    }

    PackageScanResult doScan() {
        var annotations = new Annotations();
        annotations.addProxyAnnotations(proxyAnnotations().collect(Collectors.toSet()));
        return new PackageScanResult(annotations, scanAnnotatedComponentClasses(annotations), proxyConfiguration(annotations));
    }


    private Collection<Class<?>> scanAnnotatedComponentClasses(Annotations annotations) {
        return annotations.getComponentClassAnnotations().stream()
                .map(reflections::getTypesAnnotatedWith)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    <A extends Annotation, P extends ProxyFactory<?>> Map<Class<A>, Class<P>> proxyFactoriesByAnnotation() {
        return proxyAnnotations()
                .map(annotation -> (Class<A>) annotation)
                .collect(Collectors.toMap(Function.identity(), this::getFactoryClass));
    }

    private ProxyConfiguration proxyConfiguration(Annotations annotations) {
        return new ProxyConfiguration(annotations.getProxyAnnotations(), reflections);
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
                .map(clazz -> (Class<A>) clazz);
    }

}
