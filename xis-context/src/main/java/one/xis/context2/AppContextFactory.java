package one.xis.context2;

import one.xis.context.AppContext;
import one.xis.context.ProxyFactory;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


class AppContextFactory implements SingletonCreationListener {
    private final Set<SingletonProducer> singletonProducers = new HashSet<>();
    private final Set<SingletonConsumer> singletonConsumers = new HashSet<>();
    private final Set<SingletonProducer> initialProducers = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    private final Object[] additionalSingletons;
    private final Class<?>[] additionalSingletonClasses;
    private final ParameterFactory parameterFactory = new ParameterFactory();
    private final Annotations annotations;
    private final Class<?>[] annotatedComponentClasses;
    private final ProxyConfiguration proxyConfiguration;

    AppContextFactory(Object[] additionalSingletons,
                      Class<?>[] additionalSingletonClasses,
                      PackageScanResult scanResult) {
        this.additionalSingletons = additionalSingletons;
        this.additionalSingletonClasses = additionalSingletonClasses;
        this.annotations = scanResult.getAnnotations();
        this.proxyConfiguration = scanResult.getProxyConfiguration();
        this.annotatedComponentClasses = scanResult.getAnnotatedComponentClasses().toArray(Class[]::new);
    }


    public AppContext createContext() {
        evaluateAnnotatedComponents();
        evaluateAdditionalSingletonClasses();
        evaluateAdditionalSingletons();
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
                    consumer.mapProducer(producer);
                    break;
                } else {
                    throw new UnsatisfiedDependencyException(consumer.getConsumedClass());
                }
            }
        }
    }

    private void evaluateAnnotatedComponents() {
        for (var i = 0; i < annotatedComponentClasses.length; i++) {
            evaluateContext(annotatedComponentClasses[i]);
        }
    }

    private void evaluateAdditionalSingletonClasses() {
        for (var i = 0; i < additionalSingletonClasses.length; i++) {
            evaluateContext(additionalSingletonClasses[i]);
        }
    }

    private void evaluateAdditionalSingletons() {
        for (var i = 0; i < additionalSingletons.length; i++) {
            var singletonWrapper = new SingletonWrapper(additionalSingletons[i].getClass(), annotations);
            evaluateContext(additionalSingletons[i].getClass());
        }
    }

    private void evaluateContext(Class<?> singleton) {
        evaluateContext(new SingletonWrapper(singleton, annotations));
    }

    @SuppressWarnings("unchecked")
    private void evaluateContext(SingletonWrapper singleton) {
        singletonConsumers.add(singleton);
        if (isProxyFactory(singleton.getBeanClass())) {
            var factory = (ProxyFactory<Object>) singleton.getBean();
            proxyConfiguration.proxyInterfacesForFactory(factory).forEach(interfaceClass -> {
                var proxyCreator = new ProxyCreationMethodCall(singleton, interfaceClass);
                proxyCreator.addListener(this);
                singleton.addProxyCreationMethodCall(proxyCreator);
                singletonProducers.add(proxyCreator);
            });
        }
        if (annotations.isAnnotatedComponent(singleton.getBeanClass())) {
            var singletonConstructor = new SingletonConstructor(ClassUtils.getUniqueConstructor(singleton.getBeanClass()), parameterFactory);
            singletonConstructor.addListener(this);
            singletonProducers.add(singletonConstructor);
            singletonConsumers.addAll(singletonConstructor.getParameters());
            if (!singletonConstructor.isInvocable()) {
                initialProducers.add(singletonConstructor);
            }
            evaluateContext(new SingletonWrapper(singletonConstructor.getSingletonClass(), annotations));
        }
        MethodUtils.methods(singleton.getBeanClass(), this::isSingletonMethod).forEach(method -> {
            SingletonMethod singletonMethod;
            if (isInitMethod(method)) {
                var initMethod = new InitMethod(method, singleton, parameterFactory);
                singleton.addInitMethod(initMethod);
                singletonMethod = initMethod;
            } else {
                var beanMethod = new BeanCreationMethod(method, singleton, parameterFactory);
                singleton.addBeanMethod(beanMethod);
                singletonMethod = beanMethod;
            }
            singletonConsumers.addAll(singletonMethod.getParameters());
            if (singletonMethod.getReturnType() != Void.TYPE) {
                singletonMethod.addListener(this);
                singletonProducers.add(singletonMethod);
            }
            evaluateContext(new SingletonWrapper(singletonMethod.getReturnType(), annotations));
        });
    }

    private boolean isProxyFactory(Class<?> c) {
        return ProxyFactory.class.isAssignableFrom(c);
    }

    private boolean isBeanMethod(Method method) {
        return annotations.isBeanMethod(method);
    }

    private boolean isSingletonMethod(Method method) {
        return annotations.isAnnotatedMethod(method);
    }

    private boolean isInitMethod(Method method) {
        return annotations.isInitializerMethod(method);
    }


    @Override
    public void onSingletonCreated(Object o) {
        singletons.add(o);
    }


}
