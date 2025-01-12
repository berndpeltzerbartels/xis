package one.xis.context2;

interface SingletonConsumer {

    void assignValue(Object o);

    boolean isConsumerFor(Class<?> c);

    void mapProducer(SingletonProducer producer);

    Class<?> getConsumedClass();
}
