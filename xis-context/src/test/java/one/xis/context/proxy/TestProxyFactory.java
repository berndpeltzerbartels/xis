package one.xis.context.proxy;

import one.xis.context.ProxyFactory;
import one.xis.context.XISComponent;

import java.lang.reflect.Proxy;


@XISComponent
class TestProxyFactory implements ProxyFactory<TestInterface> {

    @Override
    public TestInterface createProxy(Class<TestInterface> interf) {
        return (TestInterface) Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{TestInterface.class}, new TestInvocationHandler());
    }
}
