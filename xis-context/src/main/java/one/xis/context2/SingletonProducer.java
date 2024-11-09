package one.xis.context2;

public interface SingletonProducer {

    Class<?> getSingletonClass();

    void onParameterAssigned();

    boolean isSatisfied();

    void addListener(SingletonConsumer consumer);
}
