package one.xis.context;

import lombok.extern.slf4j.Slf4j;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
class AppContextFactory implements SingletonCreationListener {
    private final LinkedList<SingletonProducer> singletonProducers = new LinkedList<>();
    private final LinkedList<SingletonConsumer> singletonConsumers = new LinkedList<>();
    private final LinkedList<MultiValueConsumer> multiValueConsumers = new LinkedList<>();
    private final Set<SingletonProducer> initialProducers = new HashSet<>();
    private final List<Object> singletons = new ArrayList<>();
    private final List<Object> additionalSingletons;
    private final Class<?>[] additionalSingletonClasses;
    private final ParameterFactory parameterFactory = new ParameterFactory();
    private final Annotations annotations;
    private final Class<?>[] annotatedComponentClasses;
    private final PackageScanResult scanResult;
    private final Reflections reflections;

    AppContextFactory(List<Object> additionalSingletons,
                      Class<?>[] additionalSingletonClasses,
                      PackageScanResult scanResult,
                      Reflections reflections) {
        this.additionalSingletons = additionalSingletons;
        this.additionalSingletonClasses = additionalSingletonClasses;
        this.annotations = scanResult.getAnnotations();
        this.scanResult = scanResult;
        this.reflections = reflections;
        this.annotatedComponentClasses = scanResult.getAnnotatedComponentClasses().toArray(Class[]::new);
    }

    public AppContext createContext() {
        var context = new AppContextImpl(singletons);
        additionalSingletons.add(context);
        evaluateAnnotatedComponents();
        evaluateAdditionalSingletonClasses();
        evaluateAdditionalSingletons();
        mapProducers();
        createSingletons();
        context.lockModification();
        return context;
    }

    private void createSingletons() {
        for (var i = 0; i < multiValueConsumers.size(); i++) {
            var consumer = multiValueConsumers.get(i);
            if (consumer.getProducerCount().get() == 0) {
                consumer.notifyParent();
            }
        }
        var producers = new ArrayList<>(initialProducers);
        for (var i = 0; i < producers.size(); i++) {
            var producer = producers.get(i);
            producer.invoke();
        }
    }

    private void mapProducers() {
        while (!singletonConsumers.isEmpty()) {
            var consumer = singletonConsumers.poll();
            if (consumer instanceof MultiValueConsumer) {
                multiValueConsumers.add((MultiValueConsumer) consumer);
            }
            for (var j = 0; j < singletonProducers.size(); j++) {
                var producer = singletonProducers.get(j);
                if (consumer.isConsumerFor(producer.getSingletonClass())) {
                    producer.addConsumer(consumer);
                    consumer.mapProducer(producer);
                }
            }
            if (!(consumer instanceof MultiValueConsumer) && !consumer.hasProducer()) {
                throw new UnsatisfiedDependencyException(consumer.getConsumedClass(), consumer);
            }
        }
    }

    private void mapProducers2() {
        while (!singletonConsumers.isEmpty()) {
            var consumer = singletonConsumers.poll();
            if (consumer instanceof MultiValueConsumer) {
                multiValueConsumers.add((MultiValueConsumer) consumer);
            }
            for (var j = 0; j < singletonProducers.size(); j++) {
                var producer = singletonProducers.get(j);
                if (consumer.isConsumerFor(producer.getSingletonClass())) {
                    producer.addConsumer(consumer);
                    consumer.mapProducer(producer);
                }
            }
            if (!(consumer instanceof MultiValueConsumer) && !consumer.hasProducer()) {
                throw new UnsatisfiedDependencyException(consumer.getConsumedClass(), consumer);
            }
        }
    }

    private void evaluateAnnotatedComponents() {
        for (var i = 0; i < annotatedComponentClasses.length; i++) {
            evaluate(new SingletonWrapper(annotatedComponentClasses[i], true));
        }
    }

    private void evaluateAdditionalSingletonClasses() {
        for (var i = 0; i < additionalSingletonClasses.length; i++) {
            evaluate(new SingletonWrapper(additionalSingletonClasses[i], true));
        }
    }

    private void evaluateAdditionalSingletons() {
        for (var i = 0; i < additionalSingletons.size(); i++) {
            evaluateAdditionalSingleton(additionalSingletons.get(i));

        }
    }

    private void evaluateAdditionalSingleton(Object singleton) {
        var additionalSingleton = new AdditionalSingleton(singleton);
        additionalSingleton.addListener(this);
        singletonProducers.add(additionalSingleton);
        initialProducers.add(additionalSingleton);
        var singletonWrapper = new SingletonWrapper(singleton.getClass(), true);
        singletonConsumers.add(singletonWrapper);
        var dependencyFields = unassignedDependencyFields(singleton, singletonWrapper);
        singletonWrapper.setSingletonFields(dependencyFields);
        singletonConsumers.addAll(dependencyFields);
        if (isProxyFactory(singleton.getClass())) {
            evaluateProxyFactory(singletonWrapper);
        }
        evaluateMethods(singletonWrapper);
    }

    private void evaluate(SingletonWrapper singleton) {
        if (singleton.getBean() != null) {
            throw new IllegalStateException("SingletonWrapper with bean already set");
        }
        singletonConsumers.add(singleton);
        var dependencyFields = dependencyFields(singleton);
        singleton.setSingletonFields(dependencyFields);
        singletonConsumers.addAll(dependencyFields);
        if (isProxyFactory(singleton.getBeanClass())) {
            evaluateProxyFactory(singleton);
        }
        if (requiresConstructor(singleton)) {
            evaluateConstructor(singleton);
        }
        evaluateMethods(singleton);
    }

    private Collection<DependencyField> dependencyFields(SingletonWrapper singleton) {
        return FieldUtil.getFields(singleton.getBeanClass(), annotations::isDependencyField).stream()
                .map(field -> Fields.createField(field, singleton)).collect(Collectors.toList());
    }

    private Collection<DependencyField> unassignedDependencyFields(Object bean, SingletonWrapper wrapper) {
        return FieldUtil.getFields(bean.getClass(), annotations::isDependencyField).stream()
                .filter(field -> FieldUtil.getFieldValue(bean, field) == null)
                .map(field -> Fields.createField(field, wrapper)).collect(Collectors.toList());
    }


    private void evaluateMethods(SingletonWrapper singleton) {
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
            if (!singletonMethod.getReturnType().equals(Void.TYPE) && !singletonMethod.getReturnType().equals(Void.class)) {
                evaluate(new SingletonWrapper(singletonMethod.getReturnType()));
            }
        });
    }

    private void evaluateConstructor(SingletonWrapper singleton) {
        var singletonConstructor = new SingletonConstructor(ClassUtils.getUniqueConstructor(singleton.getBeanClass()), parameterFactory);
        singletonConstructor.addListener(this);
        singletonProducers.add(singletonConstructor);
        singletonConsumers.addAll(singletonConstructor.getParameters());
        if (isInitial(singletonConstructor)) {
            initialProducers.add(singletonConstructor);
        }
    }


    private void evaluateProxyFactory(SingletonWrapper singleton) {
        scanResult.getProxyInterfacesByFactory().get(singleton.getBeanClass()).forEach(interfaceClass -> {
            var proxyCreator = new ProxyCreationMethodCall(singleton, interfaceClass);
            proxyCreator.addListener(this);
            singleton.addProxyCreationMethodCall(proxyCreator);
            singletonProducers.add(proxyCreator);
        });
    }

    private boolean requiresConstructor(SingletonWrapper singletonWrapper) {
        if (singletonWrapper.getBean() != null) {
            return false;
        }
        if (annotations.isAnnotatedComponent(singletonWrapper.getBeanClass())) {
            return true;
        }
        return singletonWrapper.isAdditionalClass();

    }

    private boolean isInitial(SingletonConstructor singletonConstructor) {
        return singletonConstructor.getParameters().isEmpty();
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
        if (log.isDebugEnabled()) {
            log.debug("Singleton created: {}", o);
        }
        singletons.add(o);
    }


}
