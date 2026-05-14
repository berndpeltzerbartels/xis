package one.xis.spring;

import one.xis.context.ProxyFactory;
import one.xis.utils.lang.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

class SpringProxyFactoryBean<I> implements FactoryBean<I>, BeanFactoryAware {

    private final String proxyInterfaceName;
    private final String proxyFactoryClassName;
    private BeanFactory beanFactory;
    private Class<I> proxyInterface;
    private Class<? extends ProxyFactory<I>> proxyFactoryClass;
    private I proxy;

    SpringProxyFactoryBean(String proxyInterfaceName, String proxyFactoryClassName) {
        this.proxyInterfaceName = proxyInterfaceName;
        this.proxyFactoryClassName = proxyFactoryClassName;
    }

    @Override
    public I getObject() {
        if (proxy == null) {
            proxy = createProxy();
        }
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return proxyInterface();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private I createProxy() {
        return createFactory().createProxy(proxyInterface());
    }

    @SuppressWarnings("unchecked")
    private ProxyFactory<I> createFactory() {
        if (beanFactory instanceof AutowireCapableBeanFactory autowireCapableBeanFactory) {
            return (ProxyFactory<I>) autowireCapableBeanFactory.createBean(proxyFactoryClass());
        }
        return instantiateFactoryDirectly();
    }

    private ProxyFactory<I> instantiateFactoryDirectly() {
        try {
            return proxyFactoryClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not instantiate proxy factory " + proxyFactoryClassName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<I> proxyInterface() {
        if (proxyInterface == null) {
            proxyInterface = (Class<I>) ClassUtils.classForName(proxyInterfaceName);
        }
        return proxyInterface;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ProxyFactory<I>> proxyFactoryClass() {
        if (proxyFactoryClass == null) {
            proxyFactoryClass = (Class<? extends ProxyFactory<I>>) ClassUtils.classForName(proxyFactoryClassName);
        }
        return proxyFactoryClass;
    }
}
