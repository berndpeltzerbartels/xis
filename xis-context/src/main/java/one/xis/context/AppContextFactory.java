package one.xis.context;

import lombok.extern.slf4j.Slf4j;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
class AppContextFactory implements SingletonCreationListener {
    private final List<SingletonProducer> singletonProducers = new ArrayList<>();
    private final List<SingletonConsumer> singletonConsumers = new ArrayList<>();
    private final List<MultiValueConsumer> multiValueConsumers = new ArrayList<>();
    private final Set<SingletonProducer> initialProducers = new HashSet<>();
    private final List<Object> singletons = new ArrayList<>();
    private final List<Object> additionalSingletons;
    private final Class<?>[] additionalSingletonClasses;
    private final ParameterFactory parameterFactory = new ParameterFactory();
    private final Annotations annotations;
    private final Class<?>[] annotatedComponentClasses;
    private final PackageScanResult scanResult;
    private final EventDispatcher eventDispatcher = new EventDispatcher();

    AppContextFactory(List<Object> additionalSingletons,
                      Class<?>[] additionalSingletonClasses,
                      PackageScanResult scanResult) {
        this.additionalSingletons = additionalSingletons;
        this.additionalSingletonClasses = additionalSingletonClasses;
        this.annotations = scanResult.getAnnotations();
        this.scanResult = scanResult;
        this.annotatedComponentClasses = scanResult.getAnnotatedComponentClasses().toArray(Class[]::new);
    }

    public AppContext createContext() {
        long t0 = System.currentTimeMillis();
        var context = new AppContextImpl(singletons);
        var eventEmitter = new EventEmitterImpl(eventDispatcher);
        additionalSingletons.add(context);
        additionalSingletons.add(eventEmitter);
        evaluateAnnotatedComponents();
        evaluateAdditionalSingletonClasses();
        evaluateAdditionalSingletons();
        long t1 = System.currentTimeMillis();
        log.info("Evaluating singletons took {} ms", t1 - t0);
        mapProducers();
        long t2 = System.currentTimeMillis();
        log.info("Mapping producers took {} ms", t2 - t1);
        createSingletons();
        long t3 = System.currentTimeMillis();
        log.info("Creating singletons took {} ms", t3 - t2);
        finalizeSingletonInitialization();
        long t4 = System.currentTimeMillis();
        log.info("Finalizing singletons took {} ms", t4 - t3);
        context.lockModification();
        long t5 = System.currentTimeMillis();
        log.info("Context lock took {} ms", t5 - t3);
        eventEmitter.emitEvent(new AppContextInitializedEvent(context));
        return context;
    }

    private void finalizeSingletonInitialization() {
        singletonConsumers.stream()
                .filter(SingletonWrapper.class::isInstance)
                .map(SingletonWrapper.class::cast)
                .forEach(SingletonWrapper::doFinalize);

        List<SingletonProducer> uninvokedProducers = singletonProducers.stream()
                .filter(producer -> !producer.isInvoked())
                .collect(Collectors.toList());

        // In AppContextFactory.java, innerhalb von finalizeSingletonInitialization()

        if (!uninvokedProducers.isEmpty()) {
            // Übergebe alle Producer, damit der Analyzer die gesamte Abhängigkeitskarte hat
            String errorMessage = new UnresolvedDependencyAnalyzer(uninvokedProducers, singletonProducers).analyze();
            throw new IllegalStateException(errorMessage);
        }
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
        for (var i = 0; i < singletonConsumers.size(); i++) {
            var consumer = singletonConsumers.get(i);
            if (consumer instanceof MultiValueConsumer) {
                multiValueConsumers.add((MultiValueConsumer) consumer);
                mapProducersForMultiValueConsumer(consumer);
            } else {
                mapProducersForSingleValueConsumer(consumer);
            }
        }
    }

    private void mapProducersForMultiValueConsumer(SingletonConsumer consumer) {
        for (var j = 0; j < singletonProducers.size(); j++) {
            var producer = singletonProducers.get(j);
            if (consumer.isConsumerFor(producer.getSingletonClass())) {
                consumer.mapProducer(producer);
                producer.addConsumer(consumer);
                if (producer instanceof SingletonCreationListener) {
                    producer.addListener(this);
                }
            }
        }
    }

    private void mapProducersForSingleValueConsumer(SingletonConsumer consumer) {
        var matchingProducers = new ArrayList<SingletonProducer>();
        var matchingDefaultProducers = new ArrayList<SingletonProducer>();
        for (var j = 0; j < singletonProducers.size(); j++) {
            var producer = singletonProducers.get(j);
            if (consumer.isConsumerFor(producer.getSingletonClass())) {
                if (isDefaultProducer(producer)) {
                    matchingDefaultProducers.add(producer);
                } else {
                    matchingProducers.add(producer);
                }
            }
        }
        List<SingletonProducer> producersToUse = matchingProducers.isEmpty() ? matchingDefaultProducers : matchingProducers;
        for (var producer : producersToUse) {
            consumer.mapProducer(producer);
            producer.addConsumer(consumer);
            if (producer instanceof SingletonCreationListener) {
                producer.addListener(this);
            }
        }
    }

    private boolean isDefaultProducer(SingletonProducer producer) {
        if (producer instanceof SingletonConstructor constructor) {
            return constructor.getSingletonClass().isAnnotationPresent(DefaultComponent.class);
        }
        return false;
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
                .map(field -> DependencyFields.createField(field, singleton)).collect(Collectors.toList());
    }

    private Collection<DependencyField> unassignedDependencyFields(Object bean, SingletonWrapper wrapper) {
        return FieldUtil.getFields(bean.getClass(), annotations::isDependencyField).stream()
                .filter(field -> FieldUtil.getFieldValue(bean, field) == null)
                .map(field -> DependencyFields.createField(field, wrapper)).collect(Collectors.toList());
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
            if (!isEventListenerMethod(singletonMethod.getMethod())) {
                singletonConsumers.addAll(singletonMethod.getParameters());
            }
            if (singletonMethod.getReturnType() != Void.TYPE) {
                singletonMethod.addListener(this);
                singletonProducers.add(singletonMethod);
            }
            if (!singletonMethod.getReturnType().equals(Void.TYPE) && !singletonMethod.getReturnType().equals(Void.class)) {
                evaluate(new SingletonWrapper(singletonMethod.getSingletonClass()));
            }
            if (isEventListenerMethod(method)) {
                validateEventMethod(method);
                eventDispatcher.addEventListenerMethod(new EventListenerMethod(singleton, method));
            }
        });
    }

    private boolean isEventListenerMethod(Method method) {
        return annotations.isEventListenerMethod(method);
    }

    private void validateEventMethod(Method method) {
        if (!"void".equals(method.getReturnType().getName()) && !method.getReturnType().equals(Void.TYPE)) {
            throw new IllegalStateException("Event listener method must return void: " + method);
        }
        if (method.getParameterCount() != 1) {
            throw new IllegalStateException("Event listener method must have exactly one parameter: " + method);
        }
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
