package one.xis.invocation;


import lombok.RequiredArgsConstructor;
import one.xis.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ControllerMethod {
    private final Object controller;
    private final Method method;
    private final List<MethodParameter> methodParameters;

    Object invoke(Request request) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(controller, getArgs(request));
    }

    private Object[] getArgs(Request request) {
        return methodParameters.stream().map(methodParameter -> methodParameter.value(request)).toArray();
    }


    Set<String> getRequiredModelIds() {
        return methodParameters.stream()
                .filter(ModelParameter.class::isInstance)
                .map(ModelParameter.class::cast)
                .map(ModelParameter::getId)
                .collect(Collectors.toSet());
    }

}
