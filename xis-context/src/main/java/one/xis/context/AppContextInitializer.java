package one.xis.context;

import lombok.Getter;

import java.util.Set;

public class AppContextInitializer {

    private final AppReflection reflections;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName());
    }

    public AppContextInitializer(String basePackage) {
        reflections = new DefaultAppReflection(basePackage);
    }

    public AppContextInitializer(AppReflection reflections) {
        this.reflections = reflections;
    }

    @Getter
    private Set<Object> singletons;

    public void initializeContext() {
        FieldInjection fieldInjection = new FieldInjection(reflections);
        InitMethodInvocation initInvokers = new InitMethodInvocation();
        Instantiation instantiation = new Instantiation(fieldInjection, initInvokers, reflections);
        instantiation.createInstances();
        instantiation.postCheck();
        fieldInjection.doInjection();
        initInvokers.invokeAll();
        singletons = instantiation.getSingletons();
    }


}
