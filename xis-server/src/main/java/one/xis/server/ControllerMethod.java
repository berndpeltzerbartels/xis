package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;

import java.lang.reflect.Method;

@Data
@Slf4j
class ControllerMethod {

    protected final Method method;
    protected final String key;
    protected final MainDeserializer deserializer;
    protected final ControllerMethodResultMapper controllerMethodResultMapper;
    protected final ControllerMethodParameter[] controllerMethodParameters;

    ControllerMethod(@NonNull Method method, @NonNull String key, @NonNull MainDeserializer deserializer, @NonNull ControllerMethodResultMapper controllerMethodResultMapper) {
        this.method = method;
        this.key = key;
        this.deserializer = deserializer;
        this.controllerMethodResultMapper = controllerMethodResultMapper;
        this.controllerMethodParameters = new ControllerMethodParameter[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            controllerMethodParameters[i] = new ControllerMethodParameter(method, method.getParameters()[i], deserializer);
        }
    }

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller) throws Exception {
        var postProcessingResults = new PostProcessingResults();
        var args = prepareArgs(method, request, postProcessingResults);
        if (postProcessingResults.authenticate()) {
            // TODO
        }
        var controllerMethodResult = new ControllerMethodResult();
        controllerMethodResultMapper.mapRequestToResult(request, controllerMethodResult);
        if (postProcessingResults.reject()) {
            controllerMethodResultMapper.mapValidationErrors(controllerMethodResult, postProcessingResults.getResults());
            return controllerMethodResult;
        }
        var returnValue = method.invoke(controller, args);
        // let parameters override request values
        controllerMethodResultMapper.mapMethodParameterToResultAfterInvocation(controllerMethodResult, controllerMethodParameters, args);
        // let return values override parameters
        controllerMethodResultMapper.mapReturnValueToResult(controllerMethodResult, method, returnValue);
        return controllerMethodResult;
    }

    @Override
    public String toString() {
        return "ControllerMethod(" + method.getName() + ")";
    }

    protected Object[] prepareArgs(Method method, ClientRequest request, PostProcessingResults postProcessingResults) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, postProcessingResults);
        }
        return args;
    }

}
