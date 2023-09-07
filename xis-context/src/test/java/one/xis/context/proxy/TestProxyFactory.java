package one.xis.context.proxy;

import one.xis.context.ProxyFactory;

import java.lang.reflect.Proxy;

class TestProxyFactory implements ProxyFactory<TestInterface> {

    @Override
    public TestInterface createProxy(Class<TestInterface> interf) {
        return (TestInterface) Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{TestInterface.class}, new TestInvocationHandler());
    }
}
