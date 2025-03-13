package one.xis.context;

public interface SingletonConsumer {

    void assignValue(Object o);

    boolean isConsumerFor(Class<?> c);

    void mapProducer(SingletonProducer producer);

    Class<?> getConsumedClass();

    boolean isSingleValueConsumer();
}
