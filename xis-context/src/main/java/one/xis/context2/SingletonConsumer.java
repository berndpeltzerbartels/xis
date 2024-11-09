package one.xis.context2;

interface SingletonConsumer {

    void assignValue(Object o);


    void onProducerCreated(SingletonProducer producer);

}
