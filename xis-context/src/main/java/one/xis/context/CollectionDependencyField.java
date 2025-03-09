package one.xis.context;

import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class CollectionDependencyField implements DependencyField {

    private final SingletonWrapper parent;
    private final Field field;
    private final List<Object> values = new ArrayList<>();
    private final AtomicInteger producerCount = new AtomicInteger(0);
    private final Class<?> elementType;

    CollectionDependencyField(Field field, SingletonWrapper parent) {
        this.field = field;
        this.parent = parent;
        this.elementType = FieldUtil.getGenericTypeParameter(field);
    }

    @Override
    public void assignValue(Object o) {
        values.add(o);
        if (values.size() == producerCount.get()) {
            FieldUtil.setFieldValue(parent.getBean(), field, values);
            parent.doNotify();
        }
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return elementType.isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        this.producerCount.incrementAndGet();
        producer.addConsumer(this);
    }

    @Override
    public Class<?> getConsumedClass() {
        return elementType;
    }
}
