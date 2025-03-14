package one.xis.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public abstract class ProxyMethodHandler implements InvocationHandler {

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) {
        var o = preHandle(proxy, method, args); // preHandle
        if (o != null) {
            return o;
        }
        if (args == null) {
            args = new Object[0];
        }
        return doInvoke(proxy, method, args); // handle
    }

    protected abstract Object doInvoke(Object proxy, Method method, Object[] args);

    private Object preHandle(Object proxy, Method method, Object[] args) {
        if (method.getParameterCount() == 0) {
            if (method.getName().equals("toString")) {
                return proxy.getClass().getSimpleName();
            }
            if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            }
        }
        return null;
    }
}
