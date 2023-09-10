package one.xis.context;

public interface FieldWrapper {

    boolean isInjected();

    void onComponentCreated(Object o);
}
