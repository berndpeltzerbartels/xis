package one.xis.context2;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
class SimpleParam implements Param {

    @Delegate
    private final Parameter parameter;
    private final SingletonProducer parent;
    private Object value;
    private SingletonProducer producer;

    @Override
    public void assignValue(@NonNull Object o) {
        this.value = o;
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return false;
    }

    @Override
    public boolean isProducersComplete() {
        return producer != null;
    }

    @Override
    public boolean isValuesAssigned() {
        return value != null;
    }

    @Override
    public Collection<Class<?>> getUnsatisfiedDependencies() {
        return value == null ? List.of(parameter.getType()) : List.of();
    }

    @Override
    public void setProducer(SingletonProducer producer) {
        this.producer = producer;
    }
}
