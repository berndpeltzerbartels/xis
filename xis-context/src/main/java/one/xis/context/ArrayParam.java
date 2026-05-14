package one.xis.context;

import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ArrayParam implements Param, MultiValueConsumer {
    private final Class<?> componentType;
    private final SingletonProducer parentProducer;
    private final List<Object> values = new ArrayList<>();

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);


    ArrayParam(Parameter parameter, SingletonProducer parentProducer) {
        this.componentType = parameter.getType().getComponentType();
        this.parentProducer = parentProducer;
    }

    @Override
    public void assignValueIfMatching(Object o) {
        if (componentType.isAssignableFrom(o.getClass())) {
            values.add(o);
        }
        if (producerCount.decrementAndGet() == 0) {
            parentProducer.doNotify();
        }
    }

    @Override
    public void decrementProducerCount() {
        if (producerCount.decrementAndGet() == 0) {
            parentProducer.doNotify();
        }
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return componentType.isAssignableFrom(c);
    }

    @Override
    public boolean isValuesAssigned() {
        return producerCount.get() <= 0;
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        this.producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return componentType;
    }

    @Override
    public Object getValue() {
        var array = (Object[]) Array.newInstance(componentType, values.size());
        for (var i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    @Override
    public void notifyParent() {
        parentProducer.doNotify();
    }
}
