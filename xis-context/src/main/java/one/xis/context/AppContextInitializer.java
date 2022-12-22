package one.xis.context;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.emptySet;


public class AppContextInitializer {

    private final Reflection reflection;
    private final FieldInjection fieldInjection;
    private final InitMethodInvocation initInvokers;
    private final Collection<Class<?>> additionalClasses;
    private final Collection<Object> additionalSingeltons;

    @Getter
    private Set<Object> singletons;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(new DefaultReflection(basePackageClass));
    }

    public AppContextInitializer(String basePackage) {
        this(new DefaultReflection(basePackage));
    }

    public AppContextInitializer(Collection<Class<?>> classes) {
        this(new NoScanReflection(emptySet(), classes));
    }

    public AppContextInitializer(Reflection reflection) {
        this(reflection,
                emptySet(),
                emptySet(),
                Set.of(XISInit.class));
    }


    public AppContextInitializer(Reflection reflection,
                                 Collection<Class<?>> additionalClasses,
                                 Collection<Object> additionalSingeltons,
                                 Set<Class<? extends Annotation>> beanInitAnnotation) {
        this.reflection = reflection;
        this.additionalClasses = additionalClasses;
        this.additionalSingeltons = additionalSingeltons;
        this.fieldInjection = new FieldInjection(reflection, additionalClasses, additionalSingeltons);
        this.initInvokers = new InitMethodInvocation(beanInitAnnotation);
    }

    public AppContextInitializer(Class<?>... classes) {
        this(new NoScanReflection(Collections.emptySet(), Arrays.asList(classes)));
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
        return new SingletonInstantiation(fieldInjection, initInvokers, reflection, additionalSingeltons, additionalClasses);
    }

    private void postCheck(SingletonInstantiation singletonInstantiation) {
        new SingletonInstantiationPostCheck(singletonInstantiation).check();
    }
}
