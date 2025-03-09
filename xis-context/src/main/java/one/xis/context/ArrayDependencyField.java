package one.xis.context;

import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ArrayDependencyField implements DependencyField {

    private final Field field;
    private final SingletonWrapper parent;
    private final Class<?> elementType;
    private final List<Object> values = new ArrayList<>();
    private final AtomicInteger producerCount = new AtomicInteger(0);

    ArrayDependencyField(Field field, SingletonWrapper parent) {
        this.field = field;
        this.parent = parent;
        this.elementType = field.getType().getComponentType();
    }

    @Override
    public void assignValue(Object o) {
        values.add(o);
        if (values.size() == producerCount.get()) {
            FieldUtil.setFieldValue(parent.getBean(), field, values.toArray());
            parent.doNotify();
        }
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return false;
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return elementType;
    }
}
