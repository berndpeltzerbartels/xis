package one.xis.context2;

public interface Param extends SingletonConsumer {

    void assignValue(Object o);

    void onProducerCreated(SingletonProducer producer);

    boolean isSatisfied();

    Object getValue();
}
