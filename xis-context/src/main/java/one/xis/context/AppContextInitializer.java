package one.xis.context;

import lombok.Getter;

import java.util.Set;

public class AppContextInitializer {

    private final AppReflection reflections;
    @Getter
    private Set<Object> singletons;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName());
    }

    public AppContextInitializer(String basePackage) {
        reflections = new DefaultAppReflection(basePackage);
    }

    public AppContextInitializer(AppReflection reflections) {
        this.reflections = reflections;
    }


    public void initializeContext() {
        FieldInjection fieldInjection = new FieldInjection(reflections);
        InitMethodInvocation initInvokers = new InitMethodInvocation();
        SingletonInstantiation singletonInstantiation = new SingletonInstantiation(fieldInjection, initInvokers, reflections);
        singletonInstantiation.init();
        singletonInstantiation.createInstances();
        singletonInstantiation.postCheck();
        fieldInjection.doInjection();
        initInvokers.invokeAll();
        singletons = singletonInstantiation.getSingletons();
    }


}
