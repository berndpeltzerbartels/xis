package one.xis.context;

import java.util.concurrent.atomic.AtomicInteger;

public interface SingletonConsumer {

    void assignValueIfMatching(Object o);

    boolean isConsumerFor(Class<?> c);

    void mapProducer(SingletonProducer producer);

    Class<?> getConsumedClass();

    AtomicInteger getProducerCount();

    default boolean hasProducer() {
        return getProducerCount().get() > 0;
    }

    default boolean isOptional() {
        return false;
    }
}
