package one.xis.context.proxy;

import one.xis.context.ProxyMethodHandler;

import java.lang.reflect.Method;

public class TestInvocationHandler extends ProxyMethodHandler {

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("expected 2 arguments");
        }
        var i1 = (int) args[0];
        var i2 = (int) args[1];
        return i1 + i2;
    }
}
