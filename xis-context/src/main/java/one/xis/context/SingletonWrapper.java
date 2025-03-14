package one.xis.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
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
        log.debug("{}: trying to assign value {}", this, o);
        if (beanClass.isAssignableFrom(o.getClass())) {
            this.bean = o;
            log.debug("{}: bean assigned", this);
            for (var singletonField : new ArrayList<>(singletonFields)) {
                if (singletonField.isValueAssigned()) {
                    doSetFieldValue(singletonField);
                }
            }
            doNotify();
        }
    }

    void addProxyCreationMethodCall(ProxyCreationMethodCall proxyCreationMethodCall) {
        proxyCreationMethodCalls.add(proxyCreationMethodCall);
    }

    void doNotify() {
        log.debug("{}: notify", this);
        if (bean == null) {
            log.debug("{}: bean is null", this);
            return;
        }
        log.debug("{}: notify fields", this);
        if (singletonFields.isEmpty()) {
            log.debug("{}: notify init methods", this);
            notifyInitMethods();
            if (initMethods.isEmpty()) {
                log.debug("{}: notify bean methods", this);
                notifyBeanMethods();
                notifyProxyCreationMethodCalls();
            } else {
                log.debug("{}: init methods not executed: {}", this, initMethods.size());
            }
        } else {
            log.debug("{}: fields not assigned: {}", this, singletonFields.size());
        }
    }

    void fieldValueAssigned(@NonNull DependencyField field) {
        doSetFieldValue(field);
        doNotify();
    }

    private void doSetFieldValue(@NonNull DependencyField field) {
        if (bean != null) {
            singletonFields.remove(field);
            field.doInject();
        }
    }

    private void notifyInitMethods() {
        if (log.isDebugEnabled()) {
            log.debug("{}: notify {} init methods ", this, this.initMethods.size());
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
        if (log.isDebugEnabled()) {
            log.debug("{}: notify {} bean methods ", this, this.beanCreationMethods.size());
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
        log.debug("add init method {} to {}", method, this);
        initMethods.add(method);
    }

    void addBeanMethod(BeanCreationMethod method) {
        log.debug("add bean method {} to {}", method, this);
        beanCreationMethods.add(method);
    }

    void removeInitMethod(InitMethod method) {
        log.debug("remove init method {} from {}", method, this);
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
        return "SingletonWrapper{" + beanClass.getSimpleName() + "}";
    }
}
