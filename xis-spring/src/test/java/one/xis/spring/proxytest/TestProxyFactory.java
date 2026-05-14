package one.xis.spring.proxytest;

import one.xis.context.ProxyFactory;

public class TestProxyFactory implements ProxyFactory<TestRepository> {

    private final TestDependency dependency;

    public TestProxyFactory(TestDependency dependency) {
        this.dependency = dependency;
    }

    @Override
    public TestRepository createProxy(Class<TestRepository> interf) {
        return dependency::value;
    }
}
