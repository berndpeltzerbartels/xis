package one.xis.context.proxy;

import one.xis.context.ProxyFactory;
import one.xis.context.Component;

import java.lang.reflect.Proxy;


@Component
class TestProxyFactory implements ProxyFactory<TestInterface> {

    @Override
    public TestInterface createProxy(Class<TestInterface> interf) {
        return (TestInterface) Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{TestInterface.class}, new TestInvocationHandler());
    }
}
