package one.xis.context2;

import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@RequiredArgsConstructor
class ApplicationContextFactory implements SingletonCreationListener {
    private final Set<SingletonProducer> singletonProducers = new HashSet<>();
    private final Set<SingletonConsumer> singletonConsumers = new HashSet<>();
    private final Set<SingletonProducer> initialProducers = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    private final ParameterFactory parameterFactory = new ParameterFactory();
    private final Annotations annotations;

    private final Class<?>[] annotatedComponentClasses;


    public AppContext createContext() {
        evaluateAnnotatedComponents();
        mapProducers();
        createSingletons();
        return new AppContextImpl(singletons);
    }

    private void createSingletons() {
        var producers = new ArrayList<>(initialProducers);
        for (var i = 0; i < producers.size(); i++) {
            var producer = producers.get(i);
            producer.invoke();
        }
    }


    private void mapProducers() {
        var consumers = new LinkedList<>(singletonConsumers);
        var producers = new LinkedList<>(singletonProducers);
        while (!consumers.isEmpty()) {
            var consumer = consumers.poll();
            for (var i = 0; i < singletonProducers.size(); i++) {
                var producer = producers.get(i);
                if (consumer.isConsumerFor(producer.getSingletonClass())) {
                    producer.addConsumer(consumer);
                    consumer.setProducer(producer);
                }
            }
            if (!consumer.isProducersComplete()) {
                throw new UnsatisfiedDependencyException(consumer.getUnsatisfiedDependencies());
            }
        }
    }

    private void evaluateAnnotatedComponents() {
        for (var i = 0; i < annotatedComponentClasses.length; i++) {
            evaluateContext(annotatedComponentClasses[i]);
        }
    }

    private void evaluateContext(Class<?> singleton) {
        evaluateContext(new SingletonWrapper(singleton, annotations));
    }

    private void evaluateContext(SingletonWrapper singleton) {
        if (annotations.isAnnotatedComponent(singleton.getBeanClass())) {
            var singletonConstructor = new SingletonConstructor(ClassUtils.getUniqueConstructor(singleton.getBeanClass()), parameterFactory);
            singletonConstructor.addListener(this);
            singletonProducers.add(singletonConstructor);
            singletonConsumers.add(singletonConstructor);
            if (!singletonConstructor.isReadyForProduction()) {
                initialProducers.add(singletonConstructor);
            }
        }
        MethodUtils.methods(singleton.getBeanClass(), this::isBeanMethod).forEach(method -> {
            var singletonMethod = new SingletonMethod(method, singleton, parameterFactory);
            singletonProducers.add(singletonMethod);
            singletonConsumers.add(singletonMethod);
            if (!singletonMethod.isReadyForProduction()) {
                initialProducers.add(singletonMethod);
            }
            if (singletonMethod.getReturnType() != Void.TYPE) {
                singletonMethod.addListener(this);
            }
            var singletonWrapper = new SingletonWrapper(method.getReturnType(), annotations);
            singletonConsumers.addAll(singletonWrapper.getSingletonFields());
            evaluateContext(new SingletonWrapper(method.getReturnType(), annotations));
        });
    }

    private boolean isBeanMethod(Method method) {
        return annotations.isAnnotatedMethod(method);
    }


    @Override
    public void onSingletonCreated(Object o) {
        singletons.add(o);
    }


}
