package one.xis.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.tinylog.Logger;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class SingletonProducerImpl implements SingletonProducer {
    private final List<SingletonConsumer> consumers = new ArrayList<>();
    private final Set<SingletonCreationListener> creationListeners = new HashSet<>();
    @Getter
    private final List<Param> parameters;
    @Getter
    @Setter
    private SingletonProducer producer;
    private boolean isInvoked = false;

    SingletonProducerImpl(Parameter[] params, ParameterFactory parameterFactory) {
        this.parameters = new ArrayList<>(params.length);
        for (var i = 0; i < params.length; i++) {
            this.parameters.add(parameterFactory.createParam(params[i], this));
        }
    }

    @Override
    public boolean isInvocable() {
        if (isInvoked) return false;
        for (var i = 0; i < getParameters().size(); i++) {
            if (!getParameters().get(i).isValuesAssigned()) {
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
        isInvoked = true;
        var args = getArgs();
        var o = invoke(args);
        if (o != null) {
            Logger.debug("Singleton created by method: {}", o);
            notifySingletonCreationListeners(o);
            assignValueInConsumers(o);
        }

    }

    protected void notifySingletonCreationListeners(Object o) {
        for (var listener : creationListeners) {
            listener.onSingletonCreated(o);
        }
    }

    protected void assignValueInConsumers(@NonNull Object o) {
        for (var i = 0; i < consumers.size(); i++) {
            consumers.get(i).assignValueIfMatching(o);
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
