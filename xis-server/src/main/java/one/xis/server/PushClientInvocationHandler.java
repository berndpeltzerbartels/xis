package one.xis.server;

import one.xis.context.MethodHandler;
import one.xis.context.Component;

import java.lang.reflect.Method;

@Component
class PushClientInvocationHandler extends MethodHandler {

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
