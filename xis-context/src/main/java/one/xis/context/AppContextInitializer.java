package one.xis.context;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


public class AppContextInitializer {

    protected final ClassReflection reflections;
    protected final Collection<Object> additionalSingeltons;
    protected final FieldInjection fieldInjection;
    protected final InitMethodInvocation initInvokers;

    @Getter
    private Set<Object> singletons;
    protected final Collection<Class<?>> additionalClasses;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(new DefaultClassReflection(basePackageClass.getPackageName()), Collections.emptySet(), Collections.emptySet());
    }

    public AppContextInitializer(String basePackage, Collection<Class<?>> additionalClasses, Collection<Object> additionalSingeltons) {
        this(new DefaultClassReflection(basePackage), additionalClasses, additionalSingeltons);
    }

    public AppContextInitializer(String basePackage, Collection<Object> additionalSingeltons) {
        this(new DefaultClassReflection(basePackage), Collections.emptySet(), additionalSingeltons);
    }

    public AppContextInitializer(ClassReflection classReflection, Collection<Object> additionalSingeltons) {
        this(classReflection, Collections.emptySet(), additionalSingeltons);
    }

    AppContextInitializer(Collection<Object> mocks, Collection<Class<?>> singletonClasses) {
        this(new NoopClassReflection(), singletonClasses, mocks);
    }

    public AppContextInitializer(ClassReflection reflections) {
        this(reflections, Collections.emptySet(), Collections.emptySet());
    }

    public AppContextInitializer(ClassReflection classReflection, Collection<Class<?>> additionalClasses, Collection<Object> additionalSingeltons) {
        this.reflections = classReflection;
        this.additionalClasses = additionalClasses;
        this.additionalSingeltons = additionalSingeltons;
        fieldInjection = new FieldInjection(classReflection, additionalClasses, additionalSingeltons);
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
        return new SingletonInstantiation(fieldInjection, initInvokers, reflections, additionalSingeltons, additionalClasses);
    }

    private void postCheck(SingletonInstantiation singletonInstantiation) {
        new SingletonInstantiationPostCheck(singletonInstantiation).check();
    }
}
