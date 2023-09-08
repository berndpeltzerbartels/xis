package one.xis.context;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.emptySet;


public class AppContextInitializer {

    private final ClassesSource classesSource;
    private final FieldInjection fieldInjection;
    private final InitMethodInvocation initInvokers;
    private final Collection<Class<?>> additionalClasses;
    private final Collection<Object> additionalSingletons;

    @Getter
    private Set<Object> singletons;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(new DefaultClassesSource(basePackageClass));
    }

    public AppContextInitializer(String basePackage) {
        this(new DefaultClassesSource(basePackage));
    }

    public AppContextInitializer(Collection<Class<?>> classes) {
        this(new ExternalSingeltons(emptySet(), classes));
    }

    public AppContextInitializer(ClassesSource classesSource) {
        this(classesSource,
                emptySet(),
                emptySet(),
                Set.of(XISInit.class));
    }

    public AppContextInitializer(Set<String> packagesToScan,
                                 Set<Class<?>> additionalClasses,
                                 Set<Object> additionalSingletons) {
        this(new DefaultClassesSource(packagesToScan,
                        Set.of(XISComponent.class),
                        Set.of(XISInject.class)),
                additionalClasses,
                additionalSingletons,
                Set.of(XISInit.class));

    }


    AppContextInitializer(ClassesSource classesSource,
                          Collection<Class<?>> additionalClasses,
                          Collection<Object> additionalSingletons,
                          Set<Class<? extends Annotation>> beanInitAnnotation) {
        this.classesSource = classesSource;
        this.additionalClasses = additionalClasses;
        this.additionalSingletons = additionalSingletons;
        this.fieldInjection = new FieldInjection(classesSource, additionalClasses, additionalSingletons);
        this.initInvokers = new InitMethodInvocation(beanInitAnnotation);
    }

    public AppContextInitializer(Class<?>... classes) {
        this(new ExternalSingeltons(Collections.emptySet(), Arrays.asList(classes)));
    }

    public AppContextInitializer(Set<String> packagesToScan, Set<Class<?>> singletonClasses, Set<Object> mocks, Set<Class<? extends Annotation>> beanInitAnnotation) {
        this(new CompositeClassesSource(new DefaultClassesSource(packagesToScan), new ExternalSingeltons(mocks, singletonClasses)), singletonClasses, mocks, beanInitAnnotation);
    }

    public AppContext initializeContext() {
        SingletonInstantiation singletonInstantiation = singletonInstantiation();
        singletonInstantiation.runInstantiation();
        singletonInstantiation.populateAddionalSingletons();
        singletonInstantiation.createInstances();
        postCheck(singletonInstantiation);
        fieldInjection.doInjection();
        initInvokers.invokeAll();
        singletons = singletonInstantiation.getSingletons();
        return new AppContextImpl(Collections.unmodifiableSet(singletons));
    }

    private SingletonInstantiation singletonInstantiation() {
        return new SingletonInstantiation(fieldInjection, initInvokers, classesSource, additionalSingletons, additionalClasses);
    }

    private void postCheck(SingletonInstantiation singletonInstantiation) {
        new SingletonInstantiationPostCheck(singletonInstantiation).check();
    }
}
