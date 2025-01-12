package one.xis.context2;

interface SingletonProducer {

    Class<?> getSingletonClass();

    boolean isInvocable();

    void addConsumer(SingletonConsumer consumer);

    void addListener(SingletonCreationListener listener);

    void invoke();

    default void doNotify() {
        if (isInvocable()) invoke();
    }
}
