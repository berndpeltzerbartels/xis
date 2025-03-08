package one.xis.context;

import lombok.experimental.Delegate;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.ParameterUtil;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


class CollectionParam implements Param {

    @Delegate
    private final Parameter parameter;
    private final Class<?> actualTypeParameter;
    private final SingletonProducer parentProducer;
    private Collection<Object> values;
    private final AtomicInteger producerCount = new AtomicInteger(0);

    @SuppressWarnings("unchecked")
    CollectionParam(Parameter parameter, SingletonProducer parentProducer) {
        this.parameter = parameter;
        this.parentProducer = parentProducer;
        this.actualTypeParameter = ParameterUtil.getGenericTypeParameter(parameter);
        this.values = CollectionUtils.emptyInstance((Class<? extends Collection<Object>>) parameter.getType());
    }

    @Override
    public void assignValue(Object o) {
        this.values.add(o);
        parentProducer.doNotify();
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
        return values.size() >= producerCount.get();
    }

    @Override
    public Object getValue() {
        return values;
    }
}
