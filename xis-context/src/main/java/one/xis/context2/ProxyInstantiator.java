package one.xis.context2;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
class ProxyInstantiator implements SingletonProducer {

    private Class<?> interfaceClass;

    private final List<SingletonConsumer> consumers = new ArrayList<>();
    private final Set<SingletonCreationListener> creationListeners = new HashSet<>();

    @Override
    public Class<?> getSingletonClass() {
        return interfaceClass;
    }

    @Override
    public boolean isInvocable() {
        return true;
    }

    @Override
    public void addConsumer(SingletonConsumer consumer) {

    }

    @Override
    public void addListener(SingletonCreationListener listener) {

    }

    @Override
    public void invoke() {
        var o = invoke(args);
        notifySingletonCreationListeners(o);
        assignValueInConsumers(o);
    }

    protected void notifySingletonCreationListeners(Object o) {
        for (var listener : creationListeners) {
            listener.onSingletonCreated(o);
        }
    }

    protected void assignValueInConsumers(Object o) {
        for (var i = 0; i < consumers.size(); i++) {
            consumers.get(i).assignValue(o);
        }
    }
}
