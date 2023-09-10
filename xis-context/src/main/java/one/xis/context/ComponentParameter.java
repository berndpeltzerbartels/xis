package one.xis.context;

interface ComponentParameter {

    void onComponentCreated(Object o);

    boolean isComplete();

    int getIndex();

    Object getValue();
}
