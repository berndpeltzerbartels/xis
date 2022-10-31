package one.xis.controller;

import lombok.Builder;
import lombok.Data;
import one.xis.GetModelResult;
import one.xis.InvocationResult;
import one.xis.Model;
import one.xis.dto.ModelRequest;
import one.xis.dto.Request;
import one.xis.dto.RequestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Builder
public class ControllerWrapper {
    private final Object contoller;
    private final Map<String, ControllerMethodWrapper> methodsByActions;
    private final List<ControllerMethodWrapper> modelMethodWrappers;


    public GetModelResult invokeGetModel(Request request) {
        var result = new GetModelResult();
        modelMethodWrappers.forEach(controllerMethodWrapper -> {
            InvocationResult methodResult = controllerMethodWrapper.invoke(request);
            result.getModel().putAll(methodResult.getModel());
            result.getClientState().putAll(methodResult.getClientState());
            if (methodResult.getReturnValue() != null) {
                result.getModel().put(getKey(controllerMethodWrapper.getMethod(), controllerMethodWrapper.getMethod().getAnnotation(Model.class)), methodResult.getReturnValue());
            }
        }
        return result;
    }

    private String getKey(Method method, Model annotation) {
        return annotation.value().isEmpty() ? method.getReturnType().getSimpleName() : annotation.value();
    }

    public Class<?> invokeAction(RequestContext context) {
        Object actionResult = invoke(context.getAction(), context);
        if (actionResult == null) {
            return null;
        }
        if (actionResult instanceof Class) {
            return (Class<?>) actionResult;
        }
        throw new IllegalStateException();
    }

    public Object invoke(String action, RequestContext context) {
        return invoke(methodsByActions.get(action), context, argumentMappers.get(action));
    }

    private Object invoke(Method method, ModelRequest request, Function<RequestContext, Object[]> argumentMapper) {
        try {
            var returnValue = method.invoke(contoller, prepareArgs(method, request));
            var result = new InvocationResult();
            result.setNext(result);
            result.setClientState();

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] prepareArgs(Method method, ModelRequest modelRequest) {
        return Arrays.stream(method.getParameters()).map(parameter -> prepareArg(parameter, modelRequest)).toArray();
    }

    private Map<String, Object> clientState()

    private Object prepareArg(Parameter parameter, ModelRequest modelRequest) {
        return null;
    }

    private interface Param {
        Object getValue();
    }

    @Data
    private static class StateParameter implements Param {
        private final int index;
        private final String key;
        private Object value;

        void setValue(Request request) {
            value = request.getClientState().get(key);
        }

    }

    @Data
    private static class ModelParameter implements Param {
        private final int index;
        private Object value;

        void setValue(Request request) {
            value = request.getComponentModel();
        }
    }


}
