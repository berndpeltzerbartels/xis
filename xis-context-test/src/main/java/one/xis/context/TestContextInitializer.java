package one.xis.context;

import lombok.Getter;

import java.util.Collection;
import java.util.Set;

public class TestContextInitializer {

    private final AppReflection reflections;
    private final Collection<Object> mocks;

    @Getter
    private Set<Object> singletons;

    public TestContextInitializer(Collection<Object> mocks, Class<?>... singletonClasses) {
        reflections = new TestReflection(singletonClasses);
        this.mocks = mocks;
    }

    public TestContextInitializer(Collection<Object> mocks, Set<Class<?>> singletonClasses) {
        reflections = new TestReflection(singletonClasses);
        this.mocks = mocks;
    }


    public void initializeContext() {
        FieldInjection fieldInjection = new FieldInjection(reflections);
        InitMethodInvocation initInvokers = new InitMethodInvocation();
        TestSingletonInstantiation singletonInstantiation = new TestSingletonInstantiation(fieldInjection, initInvokers, reflections, mocks);
        singletonInstantiation.init();
        singletonInstantiation.injectMocks();
        singletonInstantiation.createInstances();
        singletonInstantiation.postCheck();
        fieldInjection.doInjection();
        initInvokers.invokeAll();
        singletons = singletonInstantiation.getSingletons();
    }


}
