package one.xis.context2;

public interface Param extends SingletonConsumer {

    void assignValue(Object o);

    boolean isValuesAssigned();

    Object getValue();
}
