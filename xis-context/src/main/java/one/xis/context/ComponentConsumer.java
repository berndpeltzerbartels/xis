package one.xis.context;

import java.util.Collection;

interface ComponentConsumer {

    void mapProducers(Collection<ComponentProducer> producers);

    void mapInitialComponents(Collection<Object> components);

}
