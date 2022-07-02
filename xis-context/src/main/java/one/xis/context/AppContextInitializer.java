package one.xis.context;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class AppContextInitializer {

    protected final AppReflection reflections;
    protected final Collection<Object> additionalSingeltons;
    protected final FieldInjection fieldInjection;
    protected final InitMethodInvocation initInvokers;

    @Getter
    private Set<Object> singletons;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName(), Collections.emptySet());
    }

    public AppContextInitializer(String basePackage) {
        this(basePackage, Collections.emptySet());
    }

    public AppContextInitializer(String basePackage, Collection<Object> additionalSingeltons) {
        this(new DefaultAppReflection(basePackage), additionalSingeltons);
    }

    public AppContextInitializer(AppReflection reflections) {
        this(reflections, Collections.emptySet());
    }

    public AppContextInitializer(AppReflection reflections, Collection<Object> additionalSingeltons) {
        this.reflections = reflections;
        this.additionalSingeltons = additionalSingeltons;
        fieldInjection = new FieldInjection(reflections);
        initInvokers = new InitMethodInvocation();
    }

    public void initializeContext() {
        SingletonInstantiation singletonInstantiation = singletonInstantiation();
        singletonInstantiation.runInstantiation();
        singletonInstantiation.populateAddionalSingletons();
        singletonInstantiation.createInstances();
        postCheck(singletonInstantiation);
        fieldInjection.doInjection();
        initInvokers.invokeAll();
        singletons = singletonInstantiation.getSingletons();
    }

    protected SingletonInstantiation singletonInstantiation() {
        return new SingletonInstantiation(fieldInjection, initInvokers, reflections, additionalSingeltons);
    }

    private void postCheck(SingletonInstantiation singletonInstantiation) {
        new SingletonInstantiationPostCheck(singletonInstantiation).check();
    }
}
