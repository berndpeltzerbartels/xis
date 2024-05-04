package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import one.xis.ModelData;
import one.xis.validation.Validated;
import one.xis.validation.Validation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@SuperBuilder
class ControllerMethod {

    protected Method method;
    protected String key;
    protected ParameterPreparer parameterPreparer;
    protected Validation validation;

    ControllerMethodResult invoke(ClientRequest request, Object controller) throws Exception {
        var errors = new HashMap<String, ValidationError>();
        var args = prepareArgs(method, request, errors);
        validateArgs(args, method, request, errors);
        var returnValue = method.invoke(controller, args);
        return new ControllerMethodResult(returnValue, modelParameterData(args), errors);
    }

    @Override
    public String toString() {
        return "ControllerMethod(" + method.getName() + ")";
    }

    protected Object[] prepareArgs(Method method, ClientRequest request, Map<String, ValidationError> errors) throws Exception {
        return parameterPreparer.prepareParameters(method, request, errors);
    }


    protected void validateArgs(@NonNull Object[] args, @NonNull Method method, @NonNull ClientRequest request, Map<String, ValidationError> errors) {
        for (var i = 0; i < args.length; i++) {
            var parameter = method.getParameters()[i];
            if (!parameter.isAnnotationPresent(Validated.class)) {
                continue;
            }
            var arg = args[i];
            //var validationResult = validation.v(arg);
        }
        // TODO
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
