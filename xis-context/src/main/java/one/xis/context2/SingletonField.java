package one.xis.context2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
class SingletonField implements SingletonConsumer {

    private final Field field;
    private final SingletonWrapper parent;

    @Getter
    @Setter
    private SingletonProducer producer;
    private Object value;

    @Override
    public void assignValue(Object o) {
        this.value = o;
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return field.getType().isAssignableFrom(c);
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
        return List.of();
    }

    public boolean isAssigned() {
        return value != null;
    }
}
