package one.xis.context;

import one.xis.utils.lang.ClassUtils;
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
        if (isValueAssigned()) {
            parent.fieldValueAssigned(this);
        }
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        if (field.isAnnotationPresent(XISInject.class)) {
            var componentAnnotation = field.getAnnotation(XISInject.class).annotatedWith();
            if (!componentAnnotation.equals(None.class) && !ClassUtils.isAnnotationPresentInHierarchy(c, componentAnnotation)) {
                return false;

            }
        }
        return elementType.isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        this.producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return elementType;
    }

    @Override
    public boolean isValueAssigned() {
        return values.size() == producerCount.get();
    }

    @Override
    public void doInject() {
        FieldUtil.setFieldValue(parent.getBean(), field, values);
    }


    @Override
    public boolean isSingleValueConsumer() {
        return false;
    }

    @Override
    public String toString() {
        return "CollectionDependencyField{" +
                "field=" + field +
                '}';
    }
}
