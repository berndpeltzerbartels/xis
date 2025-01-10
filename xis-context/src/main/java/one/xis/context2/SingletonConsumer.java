package one.xis.context2;

import java.util.Collection;

interface SingletonConsumer {

    void assignValue(Object o);

    boolean isConsumerFor(Class<?> c);

    boolean isProducersComplete();

    boolean isValuesAssigned();

    Collection<Class<?>> getUnsatisfiedDependencies();

    SingletonProducer getProducer();

    void setProducer(SingletonProducer producer);
}
