package one.xis.context;

import lombok.Getter;
import lombok.experimental.Delegate;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.ParameterUtil;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


class CollectionParam implements Param, MultiValueConsumer {

    @Delegate
    private final Parameter parameter;
    private final Class<?> actualTypeParameter;
    private final SingletonProducer parentProducer;
    private Collection<Object> values;

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);
    private final Class<?> elementType;

    @SuppressWarnings("unchecked")
    CollectionParam(Parameter parameter, SingletonProducer parentProducer) {
        this.parameter = parameter;
        this.parentProducer = parentProducer;
        this.actualTypeParameter = ParameterUtil.getGenericTypeParameter(parameter);
        this.values = CollectionUtils.emptyInstance((Class<? extends Collection<Object>>) parameter.getType());
        this.elementType = ParameterUtil.getGenericTypeParameter(parameter);
    }

    @Override
    public void assignValueIfMatching(Object o) {
        if (elementType.isAssignableFrom(o.getClass())) {
            this.values.add(o);
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
        return actualTypeParameter.isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        this.producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return actualTypeParameter;
    }

    @Override
    public boolean isValuesAssigned() {
        return producerCount.get() <= 0;
    }

    @Override
    public Object getValue() {
        return values;
    }

    @Override
    public void notifyParent() {
        parentProducer.doNotify();
    }
}
