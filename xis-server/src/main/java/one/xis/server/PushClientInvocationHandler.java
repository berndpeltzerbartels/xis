package one.xis.server;

import one.xis.context.MethodHandler;
import one.xis.context.XISComponent;

import java.lang.reflect.Method;

@XISComponent
class PushClientInvocationHandler extends MethodHandler {

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
