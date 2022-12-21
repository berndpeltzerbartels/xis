package one.xis.context;

import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SingletonInstantiation {

    @Getter
    private final Set<SingletonInstantiator> singletonInstantiators;

    @Getter
    private final Set<SingletonInstantiator> unusedSingletonInstantiators;
    private final FieldInjection fieldInjection;
    private final InitMethodInvocation initMethodInvocation;
    private final SingeltonClassReplacer classReplacer;
    protected final Collection<Object> additionalSingeltons;
    protected final Collection<Class<?>> additionalClasses;

    @Getter
    private final Set<Object> singletons = new HashSet<>();

    SingletonInstantiation(FieldInjection fieldInjection, InitMethodInvocation initMethodInvocation, Reflection reflections, Collection<Object> additionalSingeltons, Collection<Class<?>> additionalClasses) {
        this.fieldInjection = fieldInjection;
        this.initMethodInvocation = initMethodInvocation;
        this.classReplacer = new SingeltonClassReplacer();
        this.additionalSingeltons = additionalSingeltons;
        this.additionalClasses = additionalClasses;
        this.singletonInstantiators = createInstantiators(reflections);
        this.unusedSingletonInstantiators = new HashSet<>(singletonInstantiators);
    }

    void runInstantiation() {
        populateSingletonClasses();
    }

    private Set<SingletonInstantiator> createInstantiators(Reflection reflections) {
        return classesToInstantiate(reflections).stream()
                .map(this::createInstantiator)//
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Class<?>> classesToInstantiate(Reflection reflections) {
        var candidates = getComponentClasses(reflections);
        candidates.addAll(additionalClasses);
        return classReplacer.doReplacement(candidates);
    }


    private Set<Class<?>> getComponentClasses(Reflection reflections) {
        return reflections.getComponentTypes().stream()// includes types having custom component-annotations, too
                .filter(c -> !c.isAnnotation())
                .collect(Collectors.toSet());
    }

    private void populateSingletonClasses() {
        Set<Class<?>> singletonClasses = new HashSet<>(getSingletonClasses());
        singletonClasses.addAll(getAdditionalSingletonClasses());
        singletonInstantiators.forEach(instantitor -> instantitor.registerSingletonClasses(singletonClasses));
    }

    protected Set<Class<?>> getSingletonClasses() {
        return singletonInstantiators.stream().map(SingletonInstantiator::getType).collect(Collectors.toSet());
    }

    protected Set<Class<?>> getAdditionalSingletonClasses() {
        return additionalSingeltons.stream().map(Object::getClass).collect(Collectors.toSet());
    }

    private SingletonInstantiator createInstantiator(Class<?> aClass) {
        SingletonInstantiator instantitor = new SingletonInstantiator(aClass);
        instantitor.init();
        return instantitor;
    }

    void createInstances() {
        unusedSingletonInstantiators.stream().filter(SingletonInstantiator::isParameterCompleted).findFirst().ifPresent(this::createInstance);
    }

    private void createInstance(SingletonInstantiator instantitor) {
        unusedSingletonInstantiators.remove(instantitor);
        populateComponent(instantitor.createInstance());
    }

    private void populateComponent(Object o) {
        singletons.add(o);
        fieldInjection.onComponentCreated(o);
        initMethodInvocation.onComponentCreated(o);
        singletonInstantiators.forEach(instantitor -> instantitor.onComponentCreated(o));
        createInstances();
    }

    void postCheck() {
        new SingletonInstantiationPostCheck(this).check();
    }

    void populateAddionalSingletons() {
        additionalSingeltons.forEach(this::populateComponent);
    }
}
