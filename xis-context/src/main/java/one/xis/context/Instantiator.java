package one.xis.context;

public interface Instantiator {
    boolean isExecutable();

    void createInstance();

    void onComponentCreated(Object o);
}
