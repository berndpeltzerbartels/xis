package one.xis.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;


@Getter
class SingletonWrapper implements SingletonConsumer {
    private Object bean;
    private final Class<?> beanClass;
    @Setter
    private Collection<DependencyField> singletonFields;
    private final LinkedList<InitMethod> initMethods = new LinkedList<>();
    private final LinkedList<BeanCreationMethod> beanCreationMethods = new LinkedList<>();
    private final LinkedList<ProxyCreationMethodCall> proxyCreationMethodCalls = new LinkedList<>();
    private final boolean additionalClass;

    @Getter
    private final AtomicInteger producerCount = new AtomicInteger(0);

    SingletonWrapper(Class<?> c, boolean additionalClass) {
        beanClass = c;
        this.additionalClass = additionalClass;
    }

    SingletonWrapper(Class<?> c) {
        this(c, false);
    }

    SingletonWrapper(Object bean, boolean additionalClass) {
        this.bean = bean;
        this.additionalClass = additionalClass;
        beanClass = bean.getClass();
    }


    @Override
    public void assignValueIfMatching(Object o) {
        Logger.debug("{}: trying to assign bean {}", this, o);
        if (beanClass.isAssignableFrom(o.getClass())) {
            this.bean = o;
            Logger.debug("{}: bean assigned", this);
            Logger.debug("{}: unassigned field count: {}", this, this.singletonFields.size());
            for (var singletonField : new ArrayList<>(singletonFields)) {
                Logger.debug("{}: field {}, assigned={}", this, singletonField, singletonField.isValueAssigned());
                if (singletonField.isValueAssigned()) {
                    doSetFieldValue(singletonField);
                }
            }
            doNotify();
        } else {
            Logger.debug("{}: {} bean not assignable", o, this);
        }
    }

    void addProxyCreationMethodCall(ProxyCreationMethodCall proxyCreationMethodCall) {
        proxyCreationMethodCalls.add(proxyCreationMethodCall);
    }

    void doNotify() {
        Logger.debug("{}: notify", this);
        if (bean == null) {
            Logger.debug("{}: bean is null", this);
            return;
        }
        Logger.debug("{}: notify fields", this);
        if (singletonFields.isEmpty()) {
            Logger.debug("{}: notify init methods", this);
            notifyInitMethods();
            if (initMethods.isEmpty()) {
                Logger.debug("{}: notify bean methods", this);
                notifyBeanMethods();
                notifyProxyCreationMethodCalls();
            } else {
                Logger.debug("{}: init methods not executed: {}", this, initMethods.size());
            }
        } else {
            Logger.debug("{}: fields not assigned: {}", this, singletonFields);
        }
    }

    void fieldValueAssigned(@NonNull DependencyField field) {
        doSetFieldValue(field);
    }

    private void doSetFieldValue(@NonNull DependencyField field) {
        if (bean != null) {
            Logger.debug("{}: set field value to {}", this, field);
            singletonFields.remove(field);
            field.doInject();
            doNotify();
        } else {
            Logger.debug("{}: can not set field value. bean is null", this);
        }
    }

    private void notifyInitMethods() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("{}: notify {} init methods ", this, this.initMethods.size());
        }
        var initMethods = new ArrayList<>(this.initMethods);
        for (var i = 0; i < initMethods.size(); i++) {
            var initMethod = initMethods.get(i);
            if (initMethod.isInvocable()) {
                this.initMethods.remove(initMethod);
                initMethod.invoke();
            }
        }
    }

    private void notifyBeanMethods() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("{}: notify {} bean methods ", this, this.beanCreationMethods.size());
        }
        var beanMethods = new ArrayList<>(this.beanCreationMethods);
        for (var i = 0; i < beanMethods.size(); i++) {
            var beanMethod = beanMethods.get(i);
            if (beanMethod.isInvocable()) {
                this.beanCreationMethods.remove(beanMethod);
                beanMethod.invoke();
            }
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
        Logger.debug("add init method {} to {}", method, this);
        initMethods.add(method);
    }

    void addBeanMethod(BeanCreationMethod method) {
        Logger.debug("add bean method {} to {}", method, this);
        beanCreationMethods.add(method);
    }

    void removeInitMethod(InitMethod method) {
        Logger.debug("remove init method {} from {}", method, this);
        initMethods.remove(method);
    }


    @Override
    public boolean isConsumerFor(Class<?> c) {
        return beanClass.isAssignableFrom(c);
    }

    @Override
    public void mapProducer(SingletonProducer producer) {
        producerCount.incrementAndGet();
    }

    @Override
    public Class<?> getConsumedClass() {
        return beanClass;
    }

    @Override
    public String toString() {
        return "SingletonWrapper" + hashCode() + "{" + beanClass.getSimpleName() + "}";
    }
}
