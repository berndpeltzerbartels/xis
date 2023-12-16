package one.xis.server;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import one.xis.ModelData;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@SuperBuilder
abstract class ControllerMethod {

    protected Method method;
    protected String key;
    protected ControllerMethodParameterFactory parameterFactory;

    ControllerMethodResult invoke(ClientRequest request, Object controller) throws Exception {
        var args = prepareArgs(method, request);
        var returnValue = method.invoke(controller, args);
        return new ControllerMethodResult(returnValue, modelParameterData(args));
    }

    @Override
    public String toString() {
        return "ControllerMethod(" + method.getName() + ")";
    }

    protected Object[] prepareArgs(Method method, ClientRequest request) throws Exception {
        return parameterFactory.prepareArgs(method, request);
    }

    private Map<String, Object> modelParameterData(Object[] args) {
        var rv = new HashMap<String, Object>();
        for (var i = 0; i < method.getParameterCount(); i++) {
            var parameter = method.getParameters()[i];
            if (parameter.isAnnotationPresent(ModelData.class)) {
                rv.put(parameter.getAnnotation(ModelData.class).value(), args[i]);
            }
        }
        return rv;
    }


}
