package one.xis.context;

import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Array;
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
        producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return elementType;
    }

    @Override
    public boolean isValueAssigned() {
        return values.size() >= producerCount.get();
    }

    @Override
    public void doInject() {
        var array = Array.newInstance(elementType, values.size());
        for (var i = 0; i < values.size(); i++) {
            Array.set(array, i, values.get(i));
        }
        FieldUtil.setFieldValue(parent.getBean(), field, array);
    }


    @Override
    public boolean isSingleValueConsumer() {
        return false;
    }

    @Override
    public String toString() {
        return "ArrayDependencyField{" +
                "field=" + field +
                '}';
    }
}
