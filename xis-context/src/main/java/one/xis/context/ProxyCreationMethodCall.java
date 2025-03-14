package one.xis.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@RequiredArgsConstructor
public class ProxyCreationMethodCall implements SingletonProducer {
    private final SingletonWrapper parent;
    private final Class<?> interf;
    private final List<SingletonConsumer> consumers = new ArrayList<>();
    private final Set<SingletonCreationListener> creationListeners = new HashSet<>();
    private static final Method METHOD;


    static {
        try {
            METHOD = ProxyFactory.class.getDeclaredMethod("createProxy", Class.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getSingletonClass() {
        return interf;
    }

    @Override
    public boolean isInvocable() {
        return parent.getBeanClass() != null;
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
        try {
            var proxy = METHOD.invoke(parent.getBean(), interf);
            notifySingletonCreationListeners(proxy);
            notifyConsumers(proxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void notifyConsumers(Object proxy) {
        for (var consumer : consumers) {
            consumer.assignValueIfMatching(proxy);
        }
    }

    private void notifySingletonCreationListeners(Object proxy) {
        for (var listener : creationListeners) {
            listener.onSingletonCreated(proxy);
        }
    }
}
