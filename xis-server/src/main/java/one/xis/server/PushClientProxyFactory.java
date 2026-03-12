package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.ProxyFactory;

import java.lang.reflect.Proxy;

@Component
@RequiredArgsConstructor
class PushClientProxyFactory implements ProxyFactory<Object> {

    private final PushClientInvocationHandler pushClientInvocationHandler;

    @Override
    public Object createProxy(Class<Object> proxyInterface) {
        return Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[]{proxyInterface}, pushClientInvocationHandler);
    }
}
