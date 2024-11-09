package one.xis.context2;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

abstract class SingletonProducerImpl implements SingletonProducer {
    private final List<SingletonConsumer> listeners = new ArrayList<>();
    private final Param[] parameters;

    private boolean satisfied;

    SingletonProducerImpl(Parameter[] params, Parameters parameters) {
        this.parameters = new Param[params.length];
        for (var i = 0; i < params.length; i++) {
            this.parameters[i] = parameters.createParam(params[i], this);
        }
    }

    protected void dispatchSingleton(Object o) {
        for (var i = 0; i < listeners.size(); i++) {
            listeners.get(i).assignValue(o);
        }
    }

    protected Object[] getArgs() {
        var args = new Object[parameters.length];
        for (var i = 0; i < parameters.length; i++) {
            args[i] = parameters[i].getValue();
        }
        return args;
    }

    @Override
    public Class<?> getSingletonClass() {
        return null;
    }

}
