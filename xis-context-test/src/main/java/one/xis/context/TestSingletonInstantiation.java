package one.xis.context;

import org.mockito.MockingDetails;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class TestSingletonInstantiation extends SingletonInstantiation {

    TestSingletonInstantiation(FieldInjection fieldInjection, InitMethodInvocation initMethodInvocation, AppReflection reflections, Collection<Object> mocks) {
        super(fieldInjection, initMethodInvocation, reflections, mocks);
    }

    @Override
    protected Set<Class<?>> getAdditionalSingletonClasses() {
        return additionalSingeltons.stream().map(this::getOriginalClass).collect(Collectors.toSet());
    }

    private Class<?> getOriginalClass(Object o) {
        MockingDetails mockingDetails = Mockito.mockingDetails(o);
        if (mockingDetails.isMock() || mockingDetails.isSpy()) {
            return mockingDetails.getMockCreationSettings().getTypeToMock();
        }
        return o.getClass();// May be user wants to use a subclass intead of a mock
    }
}
