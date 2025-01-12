package one.xis.context2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
class SingletonWrapper implements SingletonConsumer {
    private Object bean;
    private final Class<?> beanClass;
    private final List<SingletonField> singletonFields;
    private final Collection<InitMethod> initMethods = new HashSet<>();
    private final Collection<BeanMethod> beanMethods = new HashSet<>();

    SingletonWrapper(Class<?> c, Annotations annotations) {
        beanClass = c;
        this.singletonFields = FieldUtil.getFields(beanClass, annotations::isDependencyField).stream()
                .map(field -> Fields.createField(field, this)).collect(Collectors.toList());
    }


    @Override
    public void assignValue(Object o) {
        this.bean = o;
        doNotify();
    }

    void doNotify() {
        notifyInitMethods();
        if (initMethods.isEmpty()) {
            notifyBeanMethods();
        }
    }

    protected void notifyInitMethods() {
        var initMethods = new ArrayList<>(this.initMethods);
        for (var i = 0; i < initMethods.size(); i++) {
            var initMethod = initMethods.get(i);
            initMethod.doNotify();
        }
    }

    protected void notifyBeanMethods() {
        var beanMethods = new ArrayList<>(this.beanMethods);
        for (var i = 0; i < beanMethods.size(); i++) {
            var beanMethod = beanMethods.get(i);
            beanMethod.doNotify();
        }
    }

    void addInitMethod(InitMethod method) {
        initMethods.add(method);
    }

    void addBeanMethod(BeanMethod method) {
        beanMethods.add(method);
    }

    void removeInitMethod(InitMethod method) {
        initMethods.remove(method);
        if (initMethods.isEmpty()) {
            beanMethods.forEach(SingletonProducer::doNotify);
        }
    }

    void removeBeanMethod(BeanMethod method) {
        beanMethods.remove(method);
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return beanClass.isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        if (beanClass.isAssignableFrom(producer.getSingletonClass())) {
            producer.addConsumer(this);
        }
    }

    @Override
    public Class<?> getConsumedClass() {
        return beanClass;
    }
}
