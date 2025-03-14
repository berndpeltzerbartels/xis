package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
class SimpleDependencyField implements DependencyField {

    private final Field field;
    private final SingletonWrapper parent;
    private Object fieldValue;

    @Getter
    private boolean valueAssigned;

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);

    @Override
    public void assignValueIfMatching(Object o) {
        if (field.getType().isAssignableFrom(o.getClass())) {
            fieldValue = o;
            valueAssigned = true;
            parent.fieldValueAssigned(this);
        }
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return field.getType().isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return field.getType();
    }


    @Override
    public void doInject() {
        FieldUtil.setFieldValue(parent.getBean(), field, fieldValue);
    }

    @Override
    public boolean isSingleValueConsumer() {
        return true;
    }

    @Override
    public String toString() {
        return "SimpleDependencyField{" +
                "field=" + field +
                '}';
    }
}
