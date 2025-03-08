package one.xis.context;

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
    private final List<SimpleField> singletonFields;
    private final Collection<InitMethod> initMethods = new HashSet<>();
    private final Collection<BeanCreationMethod> beanCreationMethods = new HashSet<>();
    private final Collection<ProxyCreationMethodCall> proxyCreationMethodCalls = new HashSet<>();

    SingletonWrapper(Class<?> c, Annotations annotations) {
        beanClass = c;
        this.singletonFields = FieldUtil.getFields(beanClass, annotations::isDependencyField).stream()
                .map(field -> Fields.createField(field, this)).collect(Collectors.toList());
    }

    SingletonWrapper(Object bean, Annotations annotations) {
        this.bean = bean;
        beanClass = bean.getClass();
        this.singletonFields = FieldUtil.getFields(beanClass, annotations::isDependencyField).stream()
                .map(field -> Fields.createField(field, this)).collect(Collectors.toList());
    }


    @Override
    public void assignValue(Object o) {
        this.bean = o;
        doNotify();
    }

    void addProxyCreationMethodCall(ProxyCreationMethodCall proxyCreationMethodCall) {
        proxyCreationMethodCalls.add(proxyCreationMethodCall);
    }

    void doNotify() {
        if (singletonFields.isEmpty()) {
            notifyInitMethods();
            if (initMethods.isEmpty()) {
                notifyBeanMethods();
            }
        }

    }

    private void notifyInitMethods() {
        var initMethods = new ArrayList<>(this.initMethods);
        for (var i = 0; i < initMethods.size(); i++) {
            var initMethod = initMethods.get(i);
            initMethod.doNotify();
        }
    }

    private void notifyBeanMethods() {
        var beanMethods = new ArrayList<>(this.beanCreationMethods);
        for (var i = 0; i < beanMethods.size(); i++) {
            var beanMethod = beanMethods.get(i);
            beanMethod.doNotify();
        }
    }


    void addInitMethod(InitMethod method) {
        initMethods.add(method);
    }

    void addBeanMethod(BeanCreationMethod method) {
        beanCreationMethods.add(method);
    }

    void removeInitMethod(InitMethod method) {
        initMethods.remove(method);
        if (initMethods.isEmpty()) {
            beanCreationMethods.forEach(SingletonProducer::doNotify);
        }
    }


    void removeBeanMethod(BeanCreationMethod method) {
        beanCreationMethods.remove(method);
    }

    void removeField(SimpleField field) {
        singletonFields.remove(field);
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
