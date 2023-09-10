package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.ProxyFactory;
import one.xis.context.XISComponent;

import java.lang.reflect.Proxy;

@XISComponent
@RequiredArgsConstructor
class PushClientProxyFactory implements ProxyFactory<Object> {

    private final PushClientInvocationHandler pushClientInvocationHandler;

    @Override
    public Object createProxy(Class<Object> interf) {
        return Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{interf}, pushClientInvocationHandler);
    }
}
