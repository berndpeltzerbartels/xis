package one.xis.context;


import lombok.Getter;

import java.lang.reflect.Executable;
import java.util.Collection;

abstract class ExecutableWrapper<E extends Executable> implements ComponentConsumer {

    @Getter
    private final Collection<ParameterWrapper> parameters;

    @Getter
    private final Object[] args;

    ExecutableWrapper(E executable) {
        this.parameters = ParameterWrapper.createParameterWrappers(executable.getParameters(), this);
        this.args = new Object[parameters.size()];
    }

    @Override
    public void mapProducers(Collection<ComponentProducer> producers) {
        parameters.forEach(param -> param.mapProducers(producers));
    }

    @Override
    public void mapInitialComponents(Collection<Object> components) {
        parameters.forEach(param -> param.mapInitialComponents(components));
    }

    boolean isPrepared() {
        return parameters.isEmpty();
    }

    boolean parameterAssigned(Object o, ParameterWrapper parameter) {
        parameters.remove(parameter);
        args[parameter.getIndex()] = o;
        return parameters.isEmpty();
    }
}
