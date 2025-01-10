package one.xis.context2;

interface SingletonProducer {

    Class<?> getSingletonClass();

    boolean isReadyForProduction();

    void addConsumer(SingletonConsumer consumer);

    void addListener(SingletonCreationListener listener);

    void invoke();
}
