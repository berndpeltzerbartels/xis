package one.xis.controller;

import lombok.Data;
import one.xis.dto.ModelRequest;
import one.xis.dto.Request;
import one.xis.dto.RequestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
class ControllerMethodWrapper {
    private final Object controller;
    private final Method method;
    private final List<MethodParameter> methodParameters;

    Object invoke(Method method, ModelRequest request, Function<RequestContext, Object[]> argumentMapper) {
        try {
            return method.invoke(controller, prepareArgs(request));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    Map<String, Object> getClientStateMap() {
        return methodParameters.stream()
                .filter(StateParameter.class::isInstance)
                .map(StateParameter.class::cast)
                .collect(Collectors.toMap(StateParameter::getKey, StateParameter::getValue));
    }

    Map<String, Object> getComponentModels() {
        return methodParameters.stream()
                .filter(ModelParameter.class::isInstance)
                .map(ModelParameter.class::cast)
                .collect(Collectors.toMap(ModelParameter::getParamName, ModelParameter::getValue));
    }
    
    private Object[] prepareArgs(Request request) {
        return methodParameters.stream().map(param -> param.valueFromRequest(request)).toArray();
    }

}
