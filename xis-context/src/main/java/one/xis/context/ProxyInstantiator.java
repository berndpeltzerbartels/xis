package one.xis.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ProxyInstantiator implements SingletonInstantiator {
    private final Class<?> interf;
    private final List<Class<InvocationHandler>> invocationHandlerClasses;
    private final InvocationHandler[] invocationHandlers;
    private final AtomicInteger missingHandlers;

    ProxyInstantiator(Class<?> interf, List<Class<InvocationHandler>> invocationHandlerClasses) {
        this.interf = interf;
        this.invocationHandlerClasses = invocationHandlerClasses;
        this.invocationHandlers = new InvocationHandler[invocationHandlerClasses.size()];
        this.missingHandlers = new AtomicInteger(invocationHandlerClasses.size());
    }

    @Override
    public void onComponentCreated(Object o) {
        var index = invocationHandlerClasses.indexOf(o.getClass()); // TODO this fails for proxies e.g. from spring
        if (index != -1) {
            invocationHandlers[index] = (InvocationHandler) o;
            missingHandlers.decrementAndGet();
        }
    }

    @Override
    public boolean isParameterCompleted() {
        return missingHandlers.get() < 1;
    }

    @Override
    public Object createInstance() {
        return Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{interf}, new CompoundInvocationHandler(Arrays.asList(invocationHandlers)));
    }
    
    private static class CompoundInvocationHandler implements InvocationHandler {
        private final List<InvocationHandler> invocationHandlers;

        private CompoundInvocationHandler(List<InvocationHandler> invocationHandlers) {
            this.invocationHandlers = invocationHandlers;
            if (invocationHandlers.isEmpty()) {
                throw new IllegalStateException("no handlers");
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object rv = null;
            for (var hanlder : invocationHandlers) {
                rv = hanlder.invoke(proxy, method, args);
            }
            return rv;
        }
    }
}
