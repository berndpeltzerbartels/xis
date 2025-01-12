package one.xis.context2;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.lang.reflect.Parameter;

@Getter
@RequiredArgsConstructor
class SimpleParam implements Param {

    @Delegate
    private final Parameter parameter;
    private final SingletonProducer parentProducer;
    private Object value;
    private SingletonProducer paramValueProducer;

    @Override
    public void assignValue(@NonNull Object o) {
        this.value = o;
        parentProducer.doNotify();
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return false;
    }

    @Override
    public boolean isValuesAssigned() {
        return value != null;
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        this.paramValueProducer = producer;
    }

    @Override
    public Class<?> getConsumedClass() {
        return parameter.getType();
    }
}
