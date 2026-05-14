package one.xis.context;

import java.util.concurrent.atomic.AtomicInteger;

interface MultiValueConsumer {

    AtomicInteger getProducerCount();

    void notifyParent();

    void decrementProducerCount();
}
