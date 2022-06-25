package one.xis.context;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class Instantiation {

    private final Set<Instantitor> instantitors;
    private final FieldInjection fieldInjection;
    private final InitMethodInvocation initMethodInvocation;

    @Getter
    private final Set<Object> singletons = new HashSet<>();

    Instantiation(FieldInjection fieldInjection, InitMethodInvocation initMethodInvocation, AppReflection reflections) {
        this.fieldInjection = fieldInjection;
        this.initMethodInvocation = initMethodInvocation;
        this.instantitors = createInstantiators(reflections);
        populateSingletonClasses();
    }

    private Set<Instantitor> createInstantiators(AppReflection reflections) {
        return reflections.getTypesAnnotatedWith(XISComponent.class).stream()//
                .map(this::createInstantiator)//
                .collect(Collectors.toSet());
    }

    private void populateSingletonClasses() {
        Set<Class<?>> singletonClasses = getSingletonClasses();
        instantitors.forEach(instantitor -> instantitor.populateSingletonClasses(singletonClasses));


    }

    private Set<Class<?>> getSingletonClasses() {
        return instantitors.stream().map(Instantitor::getType).collect(Collectors.toSet());
    }

    private Instantitor createInstantiator(Class<?> aClass) {
        Instantitor instantitor = new Instantitor(aClass);
        instantitor.init();
        return instantitor;
    }

    void createInstances() {
        instantitors.stream().filter(Instantitor::isParameterCompleted).findFirst().ifPresent(this::createInstance);
    }

    private void createInstance(Instantitor instantitor) {
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
                    .map(Instantitor::getType)//
                    .map(Class::getName)//
                    .collect(Collectors.joining(", ")));
        }
    }
}
