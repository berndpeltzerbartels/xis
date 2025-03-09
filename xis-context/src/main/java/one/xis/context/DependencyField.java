package one.xis.context;

interface DependencyField extends SingletonConsumer {

    boolean isValueAssigned();

    void doInject();
}
