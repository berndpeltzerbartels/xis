package one.xis.context;

@FunctionalInterface
public interface ComponentCreationListener {

    void componentCreated(Object o, ComponentProducer producer);
}
