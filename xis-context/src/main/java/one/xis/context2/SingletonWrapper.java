package one.xis.context2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
class SingletonWrapper implements SingletonConsumer {
    private Object bean;
    private final Class<?> beanClass;
    private SingletonProducer beanProducer;
    private final Collection<SingletonField> singletonFields;


    SingletonWrapper(Class<?> c, Annotations annotations) {
        beanClass = c;
        this.singletonFields = FieldUtil.getFields(beanClass, annotations::isDependencyField).stream()
                .map(field -> new SingletonField(field, this)).toList();
    }

    @Override
    public void assignValue(Object o) {
        this.bean = o;
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        if (beanClass.isAssignableFrom(c)) {
            return true;
        }
        for (SingletonField singletonField : singletonFields) {
            if (singletonField.isConsumerFor(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isProducersComplete() {
        for (SingletonField singletonField : singletonFields) {
            if (!singletonField.isProducersComplete()) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isValuesAssigned() {
        if (bean == null) {
            return false;
        }
        for (SingletonField singletonField : singletonFields) {
            if (!singletonField.isValuesAssigned()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<Class<?>> getUnsatisfiedDependencies() {
        return List.of();
    }

    @Override
    public SingletonProducer getProducer() {
        return null;
    }

    @Override
    public void setProducer(SingletonProducer producer) {

    }

}
