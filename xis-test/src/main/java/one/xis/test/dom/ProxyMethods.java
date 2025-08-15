package one.xis.test.dom;

import lombok.NonNull;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface ProxyMethods {

    ProxyMethods get(String name);

    Method getMethod();

    default ProxyExecutable asProxyExecutable(Object base) {
        var method = getMethod();
        return arguments -> {
            var args = prepareArgs(method, arguments);
            try {
                return MethodUtils.invoke(base, method, args);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException());
            }
        };
    }

    default Object invoke(Object o, Method method, Value[] values) {
        var args = prepareArgs(method, values);
        try {
            return MethodUtils.invoke(o, method, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    default Object[] prepareArgs(@NonNull Method method, @NonNull Value[] values) {
        if (values.length != method.getParameterCount()) {
            throw new IllegalArgumentException("Number of provided arguments does not match method parameter count.");
        }
        var args = new Object[method.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = convertForParameter(method, values[i]);
        }
        return args;
    }
    
    default Object convertForParameter(Method method, Value value) {
        var parameterType = method.getParameterTypes()[0];
        return ProxyUtils.convertValue(parameterType, value);
    }

}
