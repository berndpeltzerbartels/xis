package one.xis.context2;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ArrayParam implements Param {
    private final Class<?> componentType;
    private final SingletonProducer parentProducer;
    private final List<Object> values = new ArrayList<>();
    private final AtomicInteger producerCount = new AtomicInteger(0);


    ArrayParam(Parameter parameter, SingletonProducer parentProducer) {
        this.componentType = parameter.getType().getComponentType();
        this.parentProducer = parentProducer;
    }

    @Override
    public void assignValue(Object o) {
        values.add(o);
        parentProducer.doNotify();
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return componentType.isAssignableFrom(c);
    }

    @Override
    public boolean isValuesAssigned() {
        return values.size() >= producerCount.get();
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
        return values.toArray();
    }
}
