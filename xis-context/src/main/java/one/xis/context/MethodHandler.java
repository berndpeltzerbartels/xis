package one.xis.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class MethodHandler implements InvocationHandler {
    private final Object obj;
    private final String str;

    public MethodHandler() {
        obj = new Object();
        str = this.toString();


    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("hashCode") && method.getParameterCount() == 0) {
            return obj.hashCode();
        }
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return str;
        }
        if (method.getName().equals("equals") && method.getParameterCount() == 1) {
            return this.equals(args[0]);
        }
        return doInvoke(proxy, method, args);
    }

    public abstract Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;


    private boolean isEqual(Object o) {
        if (o instanceof MethodHandler methodHandler) {
            return methodHandler.obj.equals(obj);
        }
        return false;
    }


}
