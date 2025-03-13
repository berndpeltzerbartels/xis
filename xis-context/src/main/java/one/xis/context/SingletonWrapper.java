package one.xis.context;

import lombok.Getter;
import lombok.NonNull;
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
    private final List<DependencyField> singletonFields;
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
        for (var singletonField : new ArrayList<>(singletonFields)) {
            if (singletonField.isValueAssigned()) {
                doSetFieldValue(singletonField);
            }
        }
        doNotify();
    }

    void addProxyCreationMethodCall(ProxyCreationMethodCall proxyCreationMethodCall) {
        proxyCreationMethodCalls.add(proxyCreationMethodCall);
    }

    void doNotify() {
        if (bean == null) {
            return;
        }
        if (singletonFields.isEmpty()) {
            notifyInitMethods();
            if (initMethods.isEmpty()) {
                notifyBeanMethods();
                notifyProxyCreationMethodCalls();
            }
        }
    }

    void fieldValueAssigned(@NonNull DependencyField field) {
        doSetFieldValue(field);
    }

    private void doSetFieldValue(@NonNull DependencyField field) {
        if (bean != null) {
            singletonFields.remove(field);
            field.doInject();
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

    private void notifyProxyCreationMethodCalls() {
        var proxyCreationMethodCalls = new ArrayList<>(this.proxyCreationMethodCalls);
        for (var i = 0; i < proxyCreationMethodCalls.size(); i++) {
            var proxyCreationMethodCall = proxyCreationMethodCalls.get(i);
            proxyCreationMethodCall.doNotify();
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

    void removeField(SimpleDependencyField field) {
        singletonFields.remove(field);
    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return beanClass.isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        // do nothing
    }

    @Override
    public Class<?> getConsumedClass() {
        return beanClass;
    }

    @Override
    public boolean isSingleValueConsumer() {
        return true;
    }

    @Override
    public String toString() {
        return "SingletonWrapper{" + beanClass.getSimpleName() + "}";
    }
}
