package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class SimpleDependencyField implements DependencyField {

    private final Field field;
    private final SingletonWrapper parent;
    private Object fieldValue;

    @Getter
    private boolean valueAssigned;

    @Override
    public void assignValue(Object o) {
        fieldValue = o;
        valueAssigned = true;
        parent.fieldValueAssigned(this);
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return field.getType().isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        if (field.getType().isAssignableFrom(producer.getSingletonClass())) {
            producer.addConsumer(this);
        }
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
