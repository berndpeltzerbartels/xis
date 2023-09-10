package one.xis.context;


import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

class AppContextFactory {

    private final Queue<Instantiator> instantiators = new ConcurrentLinkedQueue<>();
    private final Queue<Instantiator> executableInstantiators = new ConcurrentLinkedQueue<>();
    private final Queue<MethodWrapper> executableMethods = new ConcurrentLinkedQueue<>();
    private final ComponentWrapperFactory componentWrapperFactory;
    private final InstantiatorFactory instantiatorFactory;
    private final Queue<ComponentWrapper> componentWrappers;
    private final HashSet<Object> components = new HashSet<>();

    AppContextFactory(Set<Class<?>> singletonClasses,
                      Set<Object> singletons,
                      Set<Class<? extends Annotation>> componentAnnotations,
                      Set<Class<? extends Annotation>> dependencyFieldAnnotations,
                      Set<Class<? extends Annotation>> initMethodAnnotations,
                      Set<Class<? extends Annotation>> beanMethodAnnotations,
                      Set<String> packagesToScan) {
        var reflector = new ComponentClassReflector(singletonClasses, singletons, initMethodAnnotations, packagesToScan, beanMethodAnnotations, componentAnnotations, dependencyFieldAnnotations);// TODO builder
        var reflectionResult = reflector.findComponentClasses();
        var parameterFactory = new ParameterFactory(reflectionResult.getAllComponentClasses());
        var fieldWrapperFactory = new FieldWrapperFactory(reflectionResult.getAllComponentClasses());
        this.componentWrapperFactory = new ComponentWrapperFactory(parameterFactory, reflector, fieldWrapperFactory, this::onComponentCreated);
        this.instantiatorFactory = new InstantiatorFactory(parameterFactory, reflector, this::onComponentCreated);
        this.componentWrappers = createWrappers(singletons);
        createConstructorInstantiators(reflectionResult.getClassesToInstantiate());
    }


    public AppContext createContext() {
        while (!executableInstantiators.isEmpty() && !executableMethods.isEmpty()) {
            var active = runExecutableConstructorIntantiators();
            active = active && runExecutableMethods();
            if (!active) {
                break;
            }
        }
        runPostCheck();
        return new AppContextImpl(components);
    }

    void onComponentCreated(Object o) {
        components.add(o);
        componentWrappers.add(createWrapper(o));
        runComponentWrapperLoop(o);
        runConstructorInstantiatorLoop(o);
    }

    private boolean runExecutableConstructorIntantiators() {
        var active = false;
        var instantiator = executableInstantiators.poll();
        while (instantiator != null) {
            instantiator.createInstance();
            instantiator = executableInstantiators.poll();
            active = true;
        }
        return active;
    }


    private boolean runExecutableMethods() {
        var active = false;
        var methodWrapper = executableMethods.poll();
        while (methodWrapper != null) {
            var componentWrapper = methodWrapper.getComponentWrapper();
            componentWrapper.removeExecutableMethod(methodWrapper);
            methodWrapper.execute();
            if (componentWrapper.isDone()) {
                componentWrappers.remove(componentWrapper);
            }
            methodWrapper = executableMethods.poll();
            active = true;
        }
        return active;
    }

    private void runComponentWrapperLoop(Object o) {
        componentWrappers.forEach(componentWrapper -> {
            componentWrapper.onComponentCreated(o);
            if (componentWrapper.isDependencyFieldsInjected()) {
                executableMethods.addAll(componentWrapper.removeExecutableMethods());
            }
            if (componentWrapper.isDone()) {
                componentWrappers.remove(componentWrapper);
            }
        });
    }

    private void runConstructorInstantiatorLoop(Object o) {
        for (var instantiator : instantiators) {
            instantiator.onComponentCreated(o);
            if (instantiator.isExecutable()) {
                instantiators.remove(instantiator);
                instantiator.createInstance();
            }
        }
    }

    private void createConstructorInstantiators(Collection<Class<?>> classes) {
        for (var c : classes) {
            var instantiator = instantiatorFactory.createComponentInstantiator(c);
            instantiators.add(instantiator);
            if (instantiator.isExecutable()) {
                executableInstantiators.add(instantiator);
            }
        }
    }

    private Queue<ComponentWrapper> createWrappers(Collection<Object> components) {
        return components.stream().map(this::createWrapper).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    private ComponentWrapper createWrapper(Object component) {
        return componentWrapperFactory.createComponentWrapper(component);
    }

    private void runPostCheck() {
        // TODO
    }


}
