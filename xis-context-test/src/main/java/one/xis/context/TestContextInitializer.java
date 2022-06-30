package one.xis.context;

import java.util.Collection;
import java.util.Set;

public class TestContextInitializer extends AppContextInitializer {
    
    public TestContextInitializer(Collection<Object> mocks, Set<Class<?>> singletonClasses) {
        super(new TestReflection(singletonClasses), mocks);
    }

    @Override
    protected SingletonInstantiation singletonInstantiation() {
        return new TestSingletonInstantiation(fieldInjection, initInvokers, reflections, additionalSingeltons);
    }
}
