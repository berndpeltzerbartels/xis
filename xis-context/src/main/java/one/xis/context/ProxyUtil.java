package one.xis.context;

import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
class ProxyUtil {
    
    static List<Class<InvocationHandler>> invocationHandlerClasses(Class<?> interf) {
        var handlers = new ArrayList<Class<InvocationHandler>>();
        var clazz = interf;
        while (clazz != null) {
            handlers.addAll(declaredInvocationHandlerClasses(clazz));
            clazz = clazz.getSuperclass();
        }
        return handlers;
    }

    private static List<Class<InvocationHandler>> declaredInvocationHandlerClasses(Class<?> interf) {
        validateInterface(interf);
        return Arrays.stream(interf.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class))
                .map(annotation -> annotation.annotationType().getAnnotation(XISProxy.class))
                .map(ProxyUtil::handlerClass)
                .toList();
    }

    private static Class<InvocationHandler> handlerClass(XISProxy xisProxy) {
        if (!xisProxy.handlerName().isEmpty()) {
            return (Class<InvocationHandler>) ClassUtils.classForName(xisProxy.handlerName());
        }
        if (xisProxy.handlerClass() != NoInvocationHandler.class) {
            return (Class<InvocationHandler>) xisProxy.handlerClass();
        }
        throw new IllegalStateException("@XISProxy must assign a handler");
    }

    private static void validateInterface(Class<?> interf) {
        if (!interf.isInterface()) {
            throw new IllegalArgumentException(interf + " is not an interface");
        }
    }


}
