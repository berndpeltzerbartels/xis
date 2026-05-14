package one.xis.context;

import java.util.Collection;

public interface SingletonProducer {

    Class<?> getSingletonClass();

    boolean isInvocable();

    void addConsumer(SingletonConsumer consumer);

    Collection<SingletonConsumer> getConsumers();

    void addListener(SingletonCreationListener listener);

    void invoke();

    default void doNotify() {
        if (isInvocable()) invoke();
    }

    boolean isInvoked();
}
