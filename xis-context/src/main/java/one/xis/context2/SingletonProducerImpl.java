package one.xis.context2;

import lombok.Getter;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class SingletonProducerImpl implements SingletonProducer, SingletonConsumer {
    private final List<SingletonConsumer> consumers = new ArrayList<>();
    private final Set<SingletonCreationListener> creationListeners = new HashSet<>();
    @Getter
    private final List<Param> parameters;
    private boolean satisfied;

    SingletonProducerImpl(Parameter[] params, ParameterFactory parameterFactory) {
        this.parameters = new ArrayList<>(params.length);
        for (var i = 0; i < params.length; i++) {
            this.parameters.add(parameterFactory.createParam(params[i], this));
        }
    }

    @Override
    public boolean isReadyForProduction() {
        return isValuesAssigned();
    }

    @Override
    public boolean isValuesAssigned() {
        if (satisfied) {
            return true;
        }
        for (var i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).isValuesAssigned()) {
                return false;
            }
        }
        satisfied = true;
        return true;
    }

    @Override
    public boolean isProducersComplete() {
        for (var i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).isProducersComplete()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void addConsumer(SingletonConsumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void addListener(SingletonCreationListener listener) {
        creationListeners.add(listener);
    }

    @Override
    public void invoke() {
        var args = getArgs();
        var o = invoke(args);
        notifySingletonCreationListeners(o);
        notifyConsumers(o);
    }


    protected void notifySingletonCreationListeners(Object o) {
        for (var listener : creationListeners) {
            listener.onSingletonCreated(o);
        }
    }

    protected void notifyConsumers(Object o) {
        for (var i = 0; i < consumers.size(); i++) {
            consumers.get(i).assignValue(o);
        }
    }


    protected Object[] getArgs() {
        var args = new Object[parameters.size()];
        for (var i = 0; i < parameters.size(); i++) {
            args[i] = parameters.get(i).getValue();
        }
        return args;
    }

    protected abstract Object invoke(Object[] args);

}
