package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.ProxyFactory;
import one.xis.context.XISComponent;

import java.lang.reflect.Proxy;

@XISComponent
@RequiredArgsConstructor
class PushClientProxyFactory implements ProxyFactory<Object> {

    private final PushClientInvocationHandler pushClientInvocationHandler;
    private final ClientConfigService clientConfigService;

    @Override
    public Object createProxy(Class<Object> proxyInterface) {
        clientConfigService.setUseWebsockets(true);
        return Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[]{proxyInterface}, pushClientInvocationHandler);
    }
}
