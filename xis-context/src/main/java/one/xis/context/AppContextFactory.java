package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.reflect.AnnotationUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
class AppContextFactory implements ComponentCreationListener {

    @Getter
    private final Collection<Class<?>> replacedClasses = new HashSet<>();
    private final Set<Class<? extends Annotation>> componentAnnotations;
    private final Reflections reflections;
    private final Collection<ConstructorWrapper> executableConstructorWrappers = new ConcurrentLinkedDeque<>();
    private final Collection<ComponentWrapper> componentWrappers;
    private final Vector<Object> components = new Vector<>();
    private final AppContextImpl appContext = new AppContextImpl();

    AppContextFactory(Set<Class<?>> customComponentClasses,
                      Set<Object> customComponents,
                      Set<Class<? extends Annotation>> customProxyAnnotations,
                      Set<Class<? extends Annotation>> componentAnnotations,
                      Set<Class<? extends Annotation>> dependencyFieldAnnotations,
                      Set<Class<? extends Annotation>> initMethodAnnotations,
                      Set<Class<? extends Annotation>> beanMethodAnnotations,
                      Set<String> packagesToScan) {

        this.componentAnnotations = componentAnnotations;

        this.reflections = new Reflections(packagesToScan, new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());

        var componentProducers = new HashSet<ComponentProducer>();
        var componentConsumers = new HashSet<ComponentConsumer>();
        var constructorWrapperCreator = new ConstructorWrapperCreator(dependencyFieldAnnotations,
                initMethodAnnotations,
                beanMethodAnnotations,
                executableConstructorWrappers,
                componentProducers,
                componentConsumers,
                this);

        var componentWrapperCreator = new ComponentWrapperCreator(dependencyFieldAnnotations,
                initMethodAnnotations,
                beanMethodAnnotations,
                componentProducers,
                componentConsumers,
                this);

        var proxyIntantiators = new ProxyIntantiatorCreator(reflections,
                customProxyAnnotations,
                componentProducers,
                componentConsumers,
                this);

        scanComponentClasses(constructorWrapperCreator, proxyIntantiators);
        customComponentClasses.forEach(clazz -> {
            constructorWrapperCreator.accept(clazz);
            proxyIntantiators.accept(clazz);
        });
        customComponents.forEach(componentWrapperCreator);
        customComponents.add(appContext);
        this.componentWrappers = componentWrapperCreator.getComponentWrappers();
        components.addAll(customComponents);
        for (var consumer : componentConsumers) {
            consumer.mapProducers(componentProducers);
            consumer.mapInitialComponents(customComponents);
            if (consumer instanceof ConstructorWrapper constructorWrapper) {
                if (constructorWrapper.isPrepared() && !executableConstructorWrappers.contains(constructorWrapper)) {
                    executableConstructorWrappers.add(constructorWrapper);
                }
            }
        }
    }


    AppContext createContext() {
        runInstiationLoop();
        appContext.setSingletons(components);
        runPostCheck();
        return appContext;
    }

    void removeConstructorWrapper(ConstructorWrapper constructorWrapper) {
        executableConstructorWrappers.remove(constructorWrapper);
    }

    private void runInstiationLoop() {
        for (var constructorWrapper : executableConstructorWrappers) {
            if (constructorWrapper.isPrepared()) {
                removeConstructorWrapper(constructorWrapper);
                constructorWrapper.execute();
            }
        }
    }

    private Collection<Class<?>> scanComponentClasses(Consumer<Class<?>>... consumers) {
        return componentAnnotations.stream()
                .map(reflections::getTypesAnnotatedWith)
                .flatMap(Set::stream)
                .peek(c -> Arrays.stream(consumers).forEach(consumer -> consumer.accept(c)))
                .collect(Collectors.toSet());
    }

    @Override
    public void componentCreated(Object o, ComponentProducer producer) {
        if (!(o instanceof Empty)) {
            components.add(o);
            if (producer instanceof ConstructorWrapper constructorWrapper) {
                componentWrappers.add(new ComponentWrapper(o, constructorWrapper, this));
            }
        }
    }

    private void runPostCheck() {
        componentWrapperPostCheck();
        // TODO Creators below
    }

    private void componentWrapperPostCheck() {
        componentWrappers.stream().map(ComponentWrapperPostCheck::new).forEach(ComponentWrapperPostCheck::postCheck);
    }

    private static class ComponentWrapperCreator extends ComponentReflector implements Consumer<Object> {

        @Getter
        private final Queue<ComponentWrapper> componentWrappers = new ConcurrentLinkedDeque<>();
        private final Collection<ComponentConsumer> componentConsumers;
        private final AppContextFactory contextFactory;

        public ComponentWrapperCreator(Set<Class<? extends Annotation>> dependencyFieldAnnotations,
                                       Set<Class<? extends Annotation>> initMethodAnnotations,
                                       Set<Class<? extends Annotation>> beanMethodAnnotations,
                                       Collection<ComponentProducer> componentProducers,
                                       Collection<ComponentConsumer> componentConsumers,
                                       AppContextFactory contextFactory) {
            super(dependencyFieldAnnotations,
                    initMethodAnnotations,
                    beanMethodAnnotations,
                    componentProducers,
                    contextFactory);
            this.componentConsumers = componentConsumers;
            this.contextFactory = contextFactory;
        }

        @Override
        public void accept(Object component) {
            componentWrappers.add(componentWrapper(component));
        }

        private ComponentWrapper componentWrapper(Object component) {
            var componentWrapper = new ComponentWrapper(component, contextFactory);
            var placeholder = new ComponentWrapperPlaceholder(componentWrapper);
            componentWrapper.setFieldWrappers(fieldWrappers(component.getClass(), placeholder));
            componentWrapper.setInitMethods(initMethods(component.getClass(), placeholder));
            componentWrapper.setBeanMethods(beanMethods(component.getClass(), placeholder));
            componentConsumers.add(componentWrapper);
            return componentWrapper;
        }
    }

    private static class ConstructorWrapperCreator extends ComponentReflector implements Consumer<Class<?>> {

        @Getter
        private final Collection<ConstructorWrapper> executableContructors;

        private final Collection<ComponentProducer> componentProducers;
        private final Collection<ComponentConsumer> componentConsumers;

        public ConstructorWrapperCreator(Set<Class<? extends Annotation>> dependencyFieldAnnotations,
                                         Set<Class<? extends Annotation>> initMethodAnnotations,
                                         Set<Class<? extends Annotation>> beanMethodAnnotations,
                                         Collection<ConstructorWrapper> executableContructors,
                                         Collection<ComponentProducer> componentProducers,
                                         Collection<ComponentConsumer> componentConsumers,
                                         AppContextFactory contextFactory) {
            super(dependencyFieldAnnotations,
                    initMethodAnnotations,
                    beanMethodAnnotations,
                    componentProducers,
                    contextFactory);
            this.componentProducers = componentProducers;
            this.componentConsumers = componentConsumers;
            this.executableContructors = executableContructors;
        }

        @Override
        public void accept(Class<?> c) {
            if (!c.isInterface() && !c.isAnnotation()) {
                var placeholder = new ComponentWrapperPlaceholder();
                var constructorWrapper = new ConstructorWrapper(ClassUtils.getAccessibleContructor(c).orElseThrow(), placeholder, contextFactory);
                constructorWrapper.setFieldWrappers(fieldWrappers(c, placeholder));
                constructorWrapper.setInitMethods(initMethods(c, placeholder));
                constructorWrapper.setBeanMethods(beanMethods(c, placeholder));
                componentProducers.add(constructorWrapper);
                componentConsumers.add(constructorWrapper);
                if (constructorWrapper.isPrepared()) {
                    executableContructors.add(constructorWrapper);
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static class ComponentReflector {
        private final Set<Class<? extends Annotation>> dependencyFieldAnnotations;
        private final Set<Class<? extends Annotation>> initMethodAnnotations;
        private final Set<Class<? extends Annotation>> beanMethodAnnotations;
        private final Collection<ComponentProducer> componentProducers;
        protected final AppContextFactory contextFactory;

        protected Collection<FieldWrapper> fieldWrappers(Class<?> c, ComponentWrapperPlaceholder placeholder) {
            return FieldUtil.getAllFields(c).stream()
                    .filter(this::isDependencyField)
                    .map(field -> fieldWrapper(field, placeholder))
                    .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        }

        protected Collection<InitMethodWrapper> initMethods(Class<?> c, ComponentWrapperPlaceholder placeholder) {
            return MethodUtils.allMethods(c).stream()
                    .filter(this::isInitMethod)
                    .map(method -> initMethodWrapper(method, placeholder))
                    .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        }

        protected Collection<BeanMethodWrapper> beanMethods(Class<?> c, ComponentWrapperPlaceholder placeholder) {
            return MethodUtils.allMethods(c).stream()
                    .filter(this::isBeanMethod)
                    .map(method -> beanMethodWrapper(method, placeholder))
                    .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        }

        private BeanMethodWrapper beanMethodWrapper(Method method, ComponentWrapperPlaceholder placeholder) {
            var beanMethodWrapper = new BeanMethodWrapper(method, placeholder, contextFactory);
            componentProducers.add(beanMethodWrapper);
            return beanMethodWrapper;
        }

        private InitMethodWrapper initMethodWrapper(Method method, ComponentWrapperPlaceholder placeholder) {
            return new InitMethodWrapper(method, placeholder);
        }

        private FieldWrapper fieldWrapper(Field field, ComponentWrapperPlaceholder placeholder) {
            return new FieldWrapper(field, placeholder);
        }

        private boolean isDependencyField(Field field) {
            return AnnotationUtils.hasAtLeasOneAnnotation(field, dependencyFieldAnnotations);
        }

        private boolean isInitMethod(Method method) {
            return AnnotationUtils.hasAtLeasOneAnnotation(method, initMethodAnnotations);
        }

        private boolean isBeanMethod(Method method) {
            return AnnotationUtils.hasAtLeasOneAnnotation(method, beanMethodAnnotations);
        }
    }


    private static class ProxyIntantiatorCreator implements Consumer<Class<?>> {
        private final Reflections reflections;
        private final Collection<Class<? extends Annotation>> proxyAnnotations = new HashSet<>();
        private final Collection<ComponentProducer> componentProducers;
        private final Collection<ComponentConsumer> componentConsumers;
        private final AppContextFactory contextFactory;
        @Getter
        private final Collection<ProxyInstantiator> proxyInstantiators = new HashSet<>();

        ProxyIntantiatorCreator(Reflections reflections,
                                Collection<Class<? extends Annotation>> customProxyAnnotations,
                                Collection<ComponentProducer> componentProducers,
                                Collection<ComponentConsumer> componentConsumers,
                                AppContextFactory contextFactory) {
            this.reflections = reflections;
            this.componentProducers = componentProducers;
            this.componentConsumers = componentConsumers;
            this.proxyAnnotations.addAll(customProxyAnnotations);
            this.proxyAnnotations.addAll(scanProxyAnnotations());
            this.contextFactory = contextFactory;
        }

        @SuppressWarnings("unchecked")
        private Set<Class<? extends Annotation>> scanProxyAnnotations() {
            return reflections.getTypesAnnotatedWith(XISProxy.class)
                    .stream().map(a -> (Class<? extends Annotation>) a).collect(Collectors.toSet());
        }

        private ProxyInstantiator proxyInstantiator(Class<?> interf) {
            return new ProxyInstantiator((Class<Object>) interf, ProxyUtils.factoryClass(interf).orElseThrow(), contextFactory);
        }

        @Override
        public void accept(Class<?> clazz) {
            if (clazz.isInterface() && AnnotationUtils.hasAtLeasOneAnnotation(clazz, proxyAnnotations)) {
                var instantiator = proxyInstantiator(clazz);
                instantiator.addComponentCreationListener(contextFactory);
                proxyInstantiators.add(instantiator);
                componentProducers.add(instantiator);
                componentConsumers.add(instantiator);
            }
        }
    }
}


