package one.xis.context;

import lombok.Getter;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;
import org.tinylog.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class CollectionDependencyField implements DependencyField, MultiValueConsumer {

    private final SingletonWrapper parent;
    private final Field field;
    private final List<Object> values = new ArrayList<>();

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);
    private final Class<?> elementType;

    CollectionDependencyField(Field field, SingletonWrapper parent) {
        this.field = field;
        this.parent = parent;
        this.elementType = FieldUtil.getGenericTypeParameter(field);
    }

    @Override
    public void assignValueIfMatching(Object o) {
        if (elementType.isAssignableFrom(o.getClass())) {
            Logger.debug("assigning value {} to field {}", o, field);
            values.add(o);
        }
        if (producerCount.decrementAndGet() == 0) {
            Logger.debug("all producers for field {} assigned", field);
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
        return producerCount.get() <= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doInject() {
        var fieldValue = CollectionUtils.convertCollectionClass(values, (Class<? extends Collection<?>>) field.getType());
        FieldUtil.setFieldValue(parent.getBean(), field, fieldValue);
    }

    @Override
    public String toString() {
        return "CollectionDependencyField{" +
                "field=" + field +
                '}';
    }

    @Override
    public void notifyParent() {
        parent.fieldValueAssigned(this);
    }
}
