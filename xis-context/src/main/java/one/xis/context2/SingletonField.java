package one.xis.context2;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class SingletonField implements SingletonConsumer {

    private final Field field;
    private final Singleton parent;

    private SingletonProducer producer;
    private Object value;

    @Override
    public void assignValue(Object o) {
        this.value = o;
    }

    @Override
    public void onProducerCreated(SingletonProducer producer) {
        if (field.getType().isAssignableFrom(producer.getSingletonClass())) {
            if (this.producer != null) throw new IllegalStateException(field + ": too many candidates");
            this.producer = producer;
            producer.addListener(this);
        }
    }

    public boolean isAssigned() {
        return value != null;
    }
}
