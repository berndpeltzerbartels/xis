package one.xis.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@RequiredArgsConstructor
class SimpleParam implements Param {

    @Delegate
    private final Parameter parameter;
    private final SingletonProducer parentProducer;
    private Object value;

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);

    @Override
    public void assignValueIfMatching(@NonNull Object o) {
        if (parameter.getType().isAssignableFrom(o.getClass())) {
            this.value = o;
            parentProducer.doNotify();
        }
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return parameter.getType().isAssignableFrom(c);
    }

    @Override
    public boolean isValuesAssigned() {
        return value != null;
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return parameter.getType();
    }
    
    @Override
    public String toString() {
        return "SimpleParam{" +
                "parameter=" + parameter +
                '}';
    }
}
