package one.xis.context;

public interface Param extends SingletonConsumer {

    void assignValueIfMatching(Object o);

    boolean isValuesAssigned();

    Object getValue();
}
