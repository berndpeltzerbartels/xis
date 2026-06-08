package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * App context builder for native-image applications.
 * <p>
 * The regular builder discovers proxy annotations through package scanning. Native applications
 * cannot rely on that scan, so generated catalogs register proxy interfaces explicitly here.
 */
public class NativeAppContextBuilderImpl extends AppContextBuilderImpl {

    private final Set<Class<? extends Annotation>> proxyAnnotations = new LinkedHashSet<>();
    private final Map<Class<ProxyFactory<?>>, Set<Class<?>>> proxyInterfacesByFactory = new LinkedHashMap<>();
    private final Set<Class<?>> componentClasses = new LinkedHashSet<>();

    public NativeAppContextBuilderImpl withComponentClass(Class<?> componentClass) {
        componentClasses.add(componentClass);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NativeAppContextBuilderImpl withProxyInterface(Class<? extends Annotation> proxyAnnotation,
                                                         Class<? extends ProxyFactory<?>> proxyFactory,
                                                         Class<?> proxyInterface) {
        proxyAnnotations.add(proxyAnnotation);
        proxyInterfacesByFactory.computeIfAbsent((Class) proxyFactory, ignored -> new LinkedHashSet<>()).add(proxyInterface);
        return this;
    }

    @Override
    protected PackageScanResult scanResult(Annotations annotations) {
        annotations.addProxyAnnotations(proxyAnnotations);
        return appendExplicitComponents(appendExplicitProxyInterfaces(super.scanResult(annotations)));
    }

    private PackageScanResult appendExplicitComponents(PackageScanResult scanResult) {
        if (componentClasses.isEmpty()) {
            return scanResult;
        }
        var mergedComponents = new LinkedHashSet<>(scanResult.getAnnotatedComponentClasses());
        mergedComponents.addAll(componentClasses);
        return new PackageScanResult(
                scanResult.getAnnotations(),
                mergedComponents,
                scanResult.getProxyInterfacesByFactory()
        );
    }

    private PackageScanResult appendExplicitProxyInterfaces(PackageScanResult scanResult) {
        if (proxyInterfacesByFactory.isEmpty()) {
            return scanResult;
        }
        var merged = new LinkedHashMap<Class<ProxyFactory<?>>, Collection<Class<?>>>();
        scanResult.getProxyInterfacesByFactory().forEach((factory, interfaces) ->
                merged.put(factory, new LinkedHashSet<>(interfaces)));
        proxyInterfacesByFactory.forEach((factory, interfaces) ->
                merged.computeIfAbsent(factory, ignored -> new LinkedHashSet<>()).addAll(interfaces));
        return new PackageScanResult(
                scanResult.getAnnotations(),
                scanResult.getAnnotatedComponentClasses(),
                merged.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (left, right) -> left,
                                LinkedHashMap::new
                        ))
        );
    }
}
