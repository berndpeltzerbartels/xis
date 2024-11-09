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
    private final SingletonProducer parent;
    private Object value;
    private SingletonProducer producer;

    @Override
    public void assignValue(@NonNull Object o) {
        this.value = o;
    }

    @Override
    public void onProducerCreated(SingletonProducer producer) {
        if (parameter.getType().isAssignableFrom(producer.getSingletonClass())) {
            if (this.producer != null) throw new IllegalStateException("too many candidates for " + parameter);
            this.producer = producer;
        }
    }

    @Override
    public boolean isSatisfied() {
        return value != null;
    }
}
