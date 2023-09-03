package one.xis.context.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TestInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var i1 = (int) args[0];
        var i2 = (int) args[2];
        return i1 + i2;
    }
}
