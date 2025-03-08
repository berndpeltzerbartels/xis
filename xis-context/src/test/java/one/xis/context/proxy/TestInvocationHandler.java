package one.xis.context.proxy;

import java.lang.reflect.Method;

public class TestInvocationHandler extends MethodHandler {

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) {
        var i1 = (int) args[0];
        var i2 = (int) args[1];
        return i1 + i2;
    }
}
