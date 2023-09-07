package one.xis.context;

import lombok.Getter;
import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SingletonInstantiation {

    @Getter
    private final Set<SingletonInstantiator<?>> singletonInstantiators;

    @Getter
    private final Set<SingletonInstantiator<?>> unusedSingletonInstantiators;
    private final FieldInjection fieldInjection;
    private final InitMethodInvocation initMethodInvocation;
    private final SingletonClassReplacer classReplacer;
    protected final Collection<Object> additionalSingletons;
    protected final Collection<Class<?>> additionalClasses;

    @Getter
    private final Set<Object> singletons = new HashSet<>();

    SingletonInstantiation(FieldInjection fieldInjection, InitMethodInvocation initMethodInvocation, ClassSource classSource, Collection<Object> additionalSingletons, Collection<Class<?>> additionalClasses) {
        this.fieldInjection = fieldInjection;
        this.initMethodInvocation = initMethodInvocation;
        this.classReplacer = new SingletonClassReplacer();
        this.additionalSingletons = additionalSingletons;
        this.additionalClasses = additionalClasses;
        this.singletonInstantiators = createInstantiators(classSource);
        this.unusedSingletonInstantiators = new HashSet<>(singletonInstantiators);
    }

    void createInstances() {
        unusedSingletonInstantiators.stream().filter(SingletonInstantiator::isParameterCompleted).findFirst().ifPresent(this::createInstance);
    }

    void runInstantiation() {
        populateSingletonClasses();
    }

    private Set<SingletonInstantiator<?>> createInstantiators(ClassSource classSource) {
        return classesToInstantiate(classSource).stream()
                .map(this::createInstantiator)//
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Class<?>> classesToInstantiate(ClassSource classSource) {
        var candidates = getComponentClasses(classSource);
        candidates.addAll(additionalClasses);
        additionalSingletons.stream().map(Object::getClass).toList().forEach(candidates::remove);
        return classReplacer.doReplacement(candidates);
    }


    private Set<Class<?>> getComponentClasses(ClassSource classSource) {
        return classSource.getComponentTypes().stream()// includes types having custom component-annotations, too
                .filter(c -> !c.isAnnotation())
                .collect(Collectors.toSet());
    }

    private void populateSingletonClasses() {
        Set<Class<?>> singletonClasses = new HashSet<>(getSingletonClasses());
        singletonClasses.addAll(getAdditionalSingletonClasses());
        singletonInstantiators.forEach(instantitor -> instantitor.onSingletonClassesFound(singletonClasses));
    }

    protected Set<Class<?>> getSingletonClasses() {
        return singletonInstantiators.stream().map(SingletonInstantiator::getType).collect(Collectors.toSet());
    }

    protected Set<Class<?>> getAdditionalSingletonClasses() {
        return additionalSingletons.stream().map(Object::getClass).collect(Collectors.toSet());
    }

    private SingletonInstantiator<?> createInstantiator(Class<?> aClass) {
        if (requiresProxy(aClass)) {
            return createProxyInstantiator(aClass);
        }
        return createConstructorInstantiator(aClass);
    }

    private boolean requiresProxy(Class<?> c) {
        if (!c.isInterface()) {
            return false;
        }
        return Arrays.stream(c.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class));
    }


    private XISProxy proxyAnnotation(Class<?> interf) {
        return Arrays.stream(interf.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class))
                .map(Annotation::annotationType)
                .map(interf::getAnnotation)
                .map(Annotation::annotationType)
                .map(type -> type.getAnnotation(XISProxy.class))
                .findFirst().orElseThrow();
    }

    private SingletonInstantiator<?> createConstructorInstantiator(Class<?> aClass) {
        ConstructorInstantiator instantitor = new ConstructorInstantiator(aClass);
        instantitor.init();
        return instantitor;
    }

    @SuppressWarnings("unchecked")
    private SingletonInstantiator<?> createProxyInstantiator(Class<?> interf) {
        var xisProxy = proxyAnnotation(interf);
        if (xisProxy.factory() != NoProxyFactoryClass.class) {
            return new ProxyInstantiator(interf, xisProxy.factory());
        } else {
            return new ProxyInstantiator(interf, ClassUtils.classForName(xisProxy.factoryName()));
        }
    }

    private void createInstance(SingletonInstantiator<?> instantitor) {
        unusedSingletonInstantiators.remove(instantitor);
        populateComponent(instantitor.createInstance());
    }

    private void populateComponent(Object o) {
        singletons.add(o);
        fieldInjection.onComponentCreated(o);
        initMethodInvocation.onComponentCreated(o);
        unusedSingletonInstantiators.forEach(instantitor -> instantitor.onComponentCreated(o));
        createInstances();
    }

    // TODO
    void postCheck() {
        new SingletonInstantiationPostCheck(this).check();
    }

    void populateAddionalSingletons() {
        additionalSingletons.forEach(this::populateComponent);
    }
}
