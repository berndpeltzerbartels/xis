package one.xis.context2;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class SingletonField implements SingletonConsumer {

    private final Field field;
    private final SingletonWrapper parent;

    @Override
    public void assignValue(Object o) {
        FieldUtil.setFieldValue(parent.getBean(), field, o);
        parent.doNotify();
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

}
