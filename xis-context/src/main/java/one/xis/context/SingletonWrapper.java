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
class SingletonWrapper implements SingletonConsumer, Finalizable {
    private Object bean;
    private final Class<?> beanClass;
    @Setter
    private Collection<DependencyField> singletonFields;
    @Setter
    private Collection<ValueField> valueFields = new ArrayList<>();
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
        log.debug("{}: trying to assign bean {}", this, o);
        if (beanClass.isAssignableFrom(o.getClass())) {
            this.bean = o;
            log.debug("{}: bean assigned", this);
            
            // Inject @Value fields FIRST, before dependencies
            injectValueFields();
            
            log.debug("{}: unassigned field count: {}", this, this.singletonFields.size());
            for (var singletonField : new ArrayList<>(singletonFields)) {
                log.debug("{}: field {}, assigned={}", this, singletonField, singletonField.isValueAssigned());
                if (singletonField.isValueAssigned()) {
                    doSetFieldValue(singletonField);
                }
            }
            doNotify();
        } else {
            log.debug("{}: {} bean not assignable", o, this);
        }
    }
    
    private void injectValueFields() {
        if (!valueFields.isEmpty()) {
            log.debug("{}: injecting {} @Value fields", this, valueFields.size());
            for (ValueField valueField : valueFields) {
                valueField.inject();
            }
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
            log.debug("{}: fields not assigned: {}", this, singletonFields);
        }
    }

    @Override
    public void doFinalize() {
        log.debug("{}: finalizing bean", this);
        if (bean == null) {
            return;
        }
        if (fieldsFinalized()) {
            finalizeInitMethods();
            if (initMethods.isEmpty()) {
                finalizeBeanMethods();
                notifyProxyCreationMethodCalls();
            }
        }
    }


    private boolean fieldsFinalized() {
        for (var field : singletonFields) {
            if (field instanceof MultiValueConsumer) {
                continue;
            }
            if (field instanceof SimpleDependencyField simpleDependencyField) {
                if (!simpleDependencyField.isValueAssigned()) {
                    return false;
                }
            }
        }
        return true;
    }

    void fieldValueAssigned(@NonNull DependencyField field) {
        doSetFieldValue(field);
    }

    private void doSetFieldValue(@NonNull DependencyField field) {
        if (bean != null) {
            log.debug("{}: set field value to {}", this, field);
            singletonFields.remove(field);
            field.doInject();
            doNotify();
        } else {
            log.debug("{}: can not set field value. bean is null", this);
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

    private void finalizeInitMethods() {
        if (log.isDebugEnabled()) {
            log.debug("{}: notify {} init methods ", this, this.initMethods.size());
        }
        var initMethods = new ArrayList<>(this.initMethods);
        for (var i = 0; i < initMethods.size(); i++) {
            var initMethod = initMethods.get(i);
            if (initMethod.isFinalizable()) {
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

    private void finalizeBeanMethods() {
        if (log.isDebugEnabled()) {
            log.debug("{}: notify {} bean methods ", this, this.beanCreationMethods.size());
        }
        var beanMethods = new ArrayList<>(this.beanCreationMethods);
        for (var i = 0; i < beanMethods.size(); i++) {
            var beanMethod = beanMethods.get(i);
            if (beanMethod.isFinalizable()) {
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
        return "SingletonWrapper" + hashCode() + "{" + beanClass.getSimpleName() + "}";
    }
}
