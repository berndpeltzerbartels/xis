package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class SingletonInstantiation {

    private Set<SingtelonInstantitor> instantitors;
    private final FieldInjection fieldInjection;
    private final InitMethodInvocation initMethodInvocation;
    private final AppReflection reflections;

    @Getter
    private final Set<Object> singletons = new HashSet<>();


    void init() {
        this.instantitors = createInstantiators(reflections);
        populateSingletonClasses();
    }

    private Set<SingtelonInstantitor> createInstantiators(AppReflection reflections) {
        return reflections.getTypesAnnotatedWith(XISComponent.class).stream()// also includes custom component-annotations
                .filter(c -> !c.isAnnotation())//
                .map(this::createInstantiator)//
                .collect(Collectors.toSet());
    }

    private void populateSingletonClasses() {
        Set<Class<?>> singletonClasses = getSingletonClasses();
        instantitors.forEach(instantitor -> instantitor.registerSingletonClasses(singletonClasses));
    }

    protected Set<Class<?>> getSingletonClasses() {
        return instantitors.stream().map(SingtelonInstantitor::getType).collect(Collectors.toSet());
    }

    private SingtelonInstantitor createInstantiator(Class<?> aClass) {
        SingtelonInstantitor instantitor = new SingtelonInstantitor(aClass);
        instantitor.init();
        return instantitor;
    }

    void createInstances() {
        instantitors.stream().filter(SingtelonInstantitor::isParameterCompleted).findFirst().ifPresent(this::createInstance);
    }

    private void createInstance(SingtelonInstantitor instantitor) {
        instantitors.remove(instantitor);
        onComponentCreated(instantitor.createInstance());
    }

    void onComponentCreated(Object o) {
        singletons.add(o);
        fieldInjection.onComponentCreated(o);
        initMethodInvocation.onComponentCreated(o);
        instantitors.forEach(instantitor -> instantitor.onComponentCreated(o));
        createInstances();
    }

    void postCheck() {
        if (!instantitors.isEmpty()) { // TODO Unsatified DependencyException instead, fixe Inatatiator to kow missing constructor-parameters
            throw new AppContextException("not created: " + instantitors.stream()//
                    .map(SingtelonInstantitor::getType)//
                    .map(Class::getName)//
                    .collect(Collectors.joining(", ")));
        }
    }
}
