package one.xis.context;

import lombok.Getter;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ArrayDependencyField implements DependencyField, MultiValueConsumer {

    private final Field field;
    private final SingletonWrapper parent;
    private final Class<?> elementType;
    private final List<Object> values = new ArrayList<>();

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);

    ArrayDependencyField(Field field, SingletonWrapper parent) {
        this.field = field;
        this.parent = parent;
        this.elementType = field.getType().getComponentType();
    }

    @Override
    public void assignValueIfMatching(Object o) {
        if (elementType.isAssignableFrom(o.getClass())) {
            values.add(o);
        }
        if (producerCount.decrementAndGet() == 0) {
            parent.fieldValueAssigned(this);
        }
    }

    @Override
    public void decrementProducerCount() {
        if (producerCount.decrementAndGet() == 0) {
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
        return producerCount.get() <= 0;
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
    public String toString() {
        return "ArrayDependencyField{" +
                "field=" + field +
                '}';
    }

    @Override
    public void notifyParent() {
        parent.fieldValueAssigned(this);
    }


}
