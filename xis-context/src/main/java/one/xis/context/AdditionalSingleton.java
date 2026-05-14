package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
class AdditionalSingleton implements SingletonProducer {
    private final Object singleton;

    @Getter
    private boolean invoked;

    @Getter
    private final List<SingletonConsumer> consumers = new ArrayList<>();
    private final Set<SingletonCreationListener> creationListeners = new HashSet<>();

    @Override
    public Class<?> getSingletonClass() {
        return singleton.getClass();
    }

    @Override
    public boolean isInvocable() {
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
        invoked = true;
        Object exposed = assignValueInConsumers(singleton);
        notifySingletonCreationListeners(exposed);
    }

    private void notifySingletonCreationListeners(Object o) {
        for (var listener : creationListeners) {
            listener.onSingletonCreated(o);
        }
    }

    private Object assignValueInConsumers(Object o) {
        return SingletonConsumerNotifier.assignValueInConsumers(o, consumers);
    }
}
