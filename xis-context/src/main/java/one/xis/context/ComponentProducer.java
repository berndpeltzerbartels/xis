package one.xis.context;

interface ComponentProducer {


    void addComponentCreationListener(ComponentCreationListener listener);

    Class<?> getResultClass();

}
