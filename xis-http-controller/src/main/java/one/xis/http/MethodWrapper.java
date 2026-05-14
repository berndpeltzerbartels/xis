package one.xis.http;

import lombok.RequiredArgsConstructor;
import one.xis.context.MethodHandler;

import java.lang.reflect.Method;
import java.util.List;

@RequiredArgsConstructor
class MethodWrapper {

    private final Object controller;
    private final Method method;
    private final List<MethodHandler> beforeMethodHandler;

    public Object invoke(Object... args) throws Throwable {
        return method.invoke(controller, args);
    }
}
